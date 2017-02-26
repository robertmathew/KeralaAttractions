package com.coltan.keralaattractions;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ActionMenuView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coltan.keralaattractions.ui.GridMarginDecoration;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 123;

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private TextView tvConnection;
    private Toolbar toolbar;

    private ArrayList<Photo> photoList;
    private ArrayList<String> keyList;
    private PhotoAdapter photoAdapter;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private SharedPreferences sharedPrefAuth;
    private SharedPreferences.Editor editor;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        //Auth shared preference
        sharedPrefAuth = getSharedPreferences(getString(R.string.prefs_auth_file), Context.MODE_PRIVATE);
        //editor = sharedPref.edit();
        //editor.putBoolean(getString(R.string.prefs_is_auth), false);
        //editor.commit();

        mRecyclerView = (RecyclerView) findViewById(R.id.image_grid);
        mProgressBar = (ProgressBar) findViewById(R.id.empty);
        tvConnection = (TextView) findViewById(R.id.noConnection);

        if (!isNetworkConnected()) {
            tvConnection.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        photoList = new ArrayList<Photo>();
        keyList = new ArrayList<>();
        readDataFromFirebase();

        if (savedInstanceState == null) {
            animateToolbar();
        }

        setupRecyclerView();
        populateGrid();

        firebaseAuth();
    }

    private void firebaseAuth() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                editor = sharedPrefAuth.edit();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:");
                    editor.putBoolean(getString(R.string.prefs_is_auth), true);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    editor.putBoolean(getString(R.string.prefs_is_auth), false);
                }
                editor.apply();
            }
        };
    }

    private void readDataFromFirebase() {
        mDatabase.child("photos").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // A new photo has been added, add it to the displayed list
                keyList.add(dataSnapshot.getKey());
                Photo photo = dataSnapshot.getValue(Photo.class);
                photoList.add(photo);

                photoAdapter.notifyDataSetChanged();
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (photoAdapter != null) {
            mProgressBar.setVisibility(View.GONE);
            tvConnection.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(
                new GridMarginDecoration(getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        mRecyclerView.setHasFixedSize(true);
    }

    private void populateGrid() {
        photoAdapter = new PhotoAdapter(photoList, keyList, mContext, MainActivity.this);
        mRecyclerView.setAdapter(photoAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem firebaseLogin = menu.findItem(R.id.signin);
        if (firebaseLogin != null) {
            boolean isAuth = sharedPrefAuth.getBoolean(getString(R.string.prefs_is_auth), false);
            if (isAuth) {
                firebaseLogin.setTitle(R.string.sign_out);
            } else {
                firebaseLogin.setTitle(R.string.sign_in);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                Intent intent = new Intent(this, UploadPhotoActivity.class);
                startActivity(intent);
                return true;
            case R.id.signin:
                boolean isAuth = sharedPrefAuth.getBoolean(getString(R.string.prefs_is_auth), false);
                if (isAuth) {
                    AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            Toast.makeText(mContext, getString(R.string.msg_sign_out), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setLogo(R.drawable.icon)
                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                            .setTheme(R.style.SignInTheme)
                            .build(), RC_SIGN_IN);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                Log.d(TAG, "onActivityResult: User signed in");
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
                Log.d(TAG, "onActivityResult:  User didn't signed in");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void animateToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            // this is gross but toolbar doesn't expose it's children to animate them :(
            View t = toolbar.getChildAt(0);
            if (t != null && t instanceof TextView) {
                TextView title = (TextView) t;

                // fade in and space out the title.  Animating the letterSpacing performs horribly so
                // fake it by setting the desired letterSpacing then animating the scaleX ¯\_(ツ)_/¯
                title.setAlpha(0f);
                title.setScaleX(0.8f);

                title.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .setStartDelay(300)
                        .setDuration(900)
                        .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this));
            }
            View amv = toolbar.getChildAt(1);
            if (amv != null & amv instanceof ActionMenuView) {
                ActionMenuView actions = (ActionMenuView) amv;
                popAnim(actions.getChildAt(0), 500, 200); // filter
                popAnim(actions.getChildAt(1), 700, 200); // overflow
            }
        }
    }

    private void popAnim(View v, int startDelay, int duration) {
        if (v != null) {
            v.setAlpha(0f);
            v.setScaleX(0f);
            v.setScaleY(0f);

            v.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(AnimationUtils.loadInterpolator(this, android.R.interpolator.overshoot));
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }
}
