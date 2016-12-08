package com.coltan.keralaattractions;

import android.app.Service;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "DetailActivity";

    private static final String MESSAGE_SENT_EVENT = "message_sent";
    public static final String PHOTOS_CHILD = "photos";
    public static final String COMMENT_CHILD = "comments";
    public static final String LIKE_CHILD = "likes";

    private String mUsername;
    private String mUserId;
    private String mPhotoUrl;

    private RecyclerView mRecyclerView;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleApiClient mGoogleApiClient;
    private MaterialDialog materialDialog;

    private CommentAdapter mCommentAdapter;
    private ArrayList<Comment> commentList;

    private ImageButton btnSendComment;
    private ImageView imgPlace, imgDescription;
    private TextView tvPlace, tvDescription;
    private Button btnLike;
    private EditText edComment;
    private Context mContext;
    private boolean isLiked = false;

    private String photoKey;
    private int displayHeight, displayWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = this;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Firebase Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (mFirebaseUser != null) {
            mUsername = mFirebaseUser.getDisplayName();
            mUserId = mFirebaseUser.getUid();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Photo photo = getIntent().getExtras().getParcelable("photo");
        photoKey = getIntent().getExtras().getString("key");

        /*CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(photo.getTitle());*/

        getDisplayMetrics();

        final ImageView imgPhoto = (ImageView) findViewById(R.id.backdrop);
        TextView tvTitle = (TextView) findViewById(R.id.title);

        imgPlace = (ImageView) findViewById(R.id.place_icon);
        tvPlace = (TextView) findViewById(R.id.place);
        imgDescription = (ImageView) findViewById(R.id.description_icon);
        tvDescription = (TextView) findViewById(R.id.description);
        TextView tvAuthor = (TextView) findViewById(R.id.author);
        ImageView imgAuthor = (ImageView) findViewById(R.id.authorPic);

        final CoordinatorLayout rootLayout = (CoordinatorLayout) findViewById(R.id.activity_detail);
        LinearLayout bottomSheetViewgroup = (LinearLayout) findViewById(R.id.linLayoutComment);


        btnLike = (Button) findViewById(R.id.action_like);
        final DatabaseReference globalRef = mFirebaseDatabaseReference.child(PHOTOS_CHILD).child(photoKey);

        alreadyLiked(globalRef);
        changeLikeButton();

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Changing the favorite button
                if (mFirebaseUser != null) {
                    isLiked = !isLiked;
                    changeLikeButton();
                    onLikeClicked(globalRef);
                } else {
                    Snackbar.make(rootLayout, getString(R.string.msg_sign_like), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        Button btnWallpaper = (Button) findViewById(R.id.action_set_wallpaper);
        btnWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WallpaperTask().execute(photo.getPhoto());
            }
        });

        // Create a storage reference from app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://keralaattractions-fd4fe.appspot.com");
        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(photo.getPhotoRef());

        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(pathReference)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imgPhoto);

        tvTitle.setText(photo.getTitle());

        displayPlace(photo.getPlace());
        displayDescription(photo.getDescription());
        tvAuthor.setText(photo.getAuthorName());
        Glide.with(this)
                .load(photo.getAuthorPhotoUrl())
                .dontAnimate()
                .into(imgAuthor);

        /*
        *   Comments RecyclerView   */
        mRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        commentList = new ArrayList<>();
        getCommentsFromFirebase();

        mCommentAdapter = new CommentAdapter(commentList, mContext);
        mRecyclerView.setAdapter(mCommentAdapter);


        /*
        *   Add comment    */
        if (mFirebaseUser == null) {
            bottomSheetViewgroup.setVisibility(View.GONE);
        }

        edComment = (EditText) findViewById(R.id.edNewComment);
        btnSendComment = (ImageButton) findViewById(R.id.sendCommentButton);
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strComment = edComment.getText().toString();
                if (TextUtils.getTrimmedLength(strComment) != 0) {
                    String datetime = new Date().toString();
                    String millis = String.valueOf(System.currentTimeMillis());
                    Comment comment = new Comment(mUsername, mUserId, mPhotoUrl, strComment, datetime, millis);
                    mFirebaseDatabaseReference.child(COMMENT_CHILD).child(photoKey).push().setValue(comment);
                    mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
                    edComment.setText("");
                } else {
                    edComment.setError(getString(R.string.required));
                }
            }
        });

    }

    private void changeLikeButton() {
        if (isLiked) {
            btnLike.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.ic_action_favorite, 0, 0);
        } else {
            btnLike.setCompoundDrawablesWithIntrinsicBounds(0,
                    R.drawable.ic_action_favorite_outline, 0, 0);
        }
    }

    private void alreadyLiked(DatabaseReference databaseReference) {
        databaseReference.child(LIKE_CHILD).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Map<String, Boolean> likes = (HashMap<String, Boolean>) dataSnapshot.getValue();
                    isLiked = likes.containsKey(mUserId);
                    Log.d(TAG, "onDataChange: " + isLiked);
                    changeLikeButton();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onLikeClicked(final DatabaseReference likeRef) {
        likeRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Photo p = mutableData.getValue(Photo.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(mUserId)) {
                    // Unlike the photo and remove self from likes
                    p.starCount = p.starCount - 1;
                    p.likes.remove(mUserId);
                    Log.d(TAG, "doTransaction: Unlike");
                } else {
                    // Like the photo
                    Log.d(TAG, "doTransaction: Like");
                    p.starCount = p.starCount + 1;
                    p.likes.put(mUserId, true);
                }
                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void getCommentsFromFirebase() {
        mFirebaseDatabaseReference.child("comments").child(photoKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                commentList.add(comment);

                mCommentAdapter.notifyDataSetChanged();
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
    }

    //If description text is empty then the views are gone
    private void displayDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            imgDescription.setVisibility(View.GONE);
            tvDescription.setVisibility(View.GONE);
        } else {
            tvDescription.setText(description);
        }
    }

    //If place text is empty then the views are gone
    private void displayPlace(String place) {
        if (TextUtils.isEmpty(place)) {
            imgPlace.setVisibility(View.GONE);
            tvPlace.setVisibility(View.GONE);
        } else {
            tvPlace.setText(place);
        }
    }

    // Getting width and height phone screen to set wallpaper
    private void getDisplayMetrics() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayHeight = metrics.heightPixels;
        displayWidth = metrics.widthPixels;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: Connection failed!");
    }

    private class WallpaperTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            materialDialog = new MaterialDialog.Builder(mContext)
                    .title(R.string.progress_download)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bmpImg = null;
            try {
                bmpImg = Glide.with(mContext)
                        .load(params[0])
                        .asBitmap()
                        .into(displayWidth, displayHeight)// Width and height
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return bmpImg;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
            try {
                wallpaperManager.setBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Toast.makeText(mContext, getString(R.string.msg_wallpaper_set), Toast.LENGTH_SHORT).show();
                materialDialog.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (edComment.isFocused()) {

        }
        super.onBackPressed();
    }
}
