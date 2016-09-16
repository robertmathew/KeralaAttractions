package com.coltan.keralaattractions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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

import com.coltan.keralaattractions.ui.GridMarginDecoration;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private Toolbar toolbar;

    private ArrayList<Photo> photoList;
    private ArrayList<String> keyList;
    private PhotoAdapter photoAdapter;

    private DatabaseReference mDatabase;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        photoList = new ArrayList<Photo>();
        keyList = new ArrayList<>();
        readDataFromFirebase();

        if (savedInstanceState == null) {
            animateToolbar();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.image_grid);
        mProgressBar = (ProgressBar) findViewById(R.id.empty);

        setupRecyclerView();
        populateGrid();
    }

    private void readDataFromFirebase() {
        mDatabase.child("photos").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // A new photo has been added, add it to the displayed list
                //Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());
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
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                /*switch (position % 6) {
                    case 5:
                        return 3;
                    case 3:
                        return 2;
                    default:
                        return 1;
                }*/
                return (position % 3 == 0 ? 2 : 1);
            }
        });
        mRecyclerView.addItemDecoration(new GridMarginDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        mRecyclerView.setHasFixedSize(true);

    }

    private void populateGrid() {
        photoAdapter = new PhotoAdapter(photoList, keyList, mContext);
        mRecyclerView.setAdapter(photoAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                Intent intent = new Intent(this, UploadPhotoActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.overshoot));
        }
    }
}
