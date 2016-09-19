package com.coltan.keralaattractions;

import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coltan.keralaattractions.data.PhotoContract;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CircleImageView mImageView;
        public TextView mTvAuthorName;
        public TextView mTvComment;
        public TextView mTvTime;

        public CommentViewHolder(View v) {
            super(v);
            mImageView = (CircleImageView) v.findViewById(R.id.authorImage);
            mTvAuthorName = (TextView) v.findViewById(R.id.authorName);
            mTvComment = (TextView) v.findViewById(R.id.comment);
            mTvTime = (TextView) v.findViewById(R.id.time);
        }
    }

    private static final String TAG = "DetailActivity";

    private static final int PHOTO_LOADER_ID = 0;

    private static final String MESSAGE_SENT_EVENT = "message_sent";
    public static final String PHOTOS_CHILD = "photos";
    public static final String COMMENT_CHILD = "comments";
    public static final String LIKE_CHILD = "likes";

    private String mUsername;
    private String mUserId;
    private String mPhotoUrl;

    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<Comment, CommentViewHolder> mFirebaseAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private GoogleApiClient mGoogleApiClient;
    private MaterialDialog materialDialog;

    private ImageButton btnSendComment;
    private Context mContext;
    private boolean isPressed;

    private String photoKey;
    private Boolean likePhotoExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = this;

        likePhotoExist = false;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //Firebase Database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        if (mFirebaseUser != null) {
            mUsername = mFirebaseUser.getDisplayName();
            mUserId = mFirebaseUser.getUid();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        final Photo photo = getIntent().getExtras().getParcelable("photo");
        photoKey = getIntent().getExtras().getString("key");
        //Log.d(TAG, "onCreate: " + photoKey);

        getSupportLoaderManager().initLoader(PHOTO_LOADER_ID, null, this);

        final ImageView imgPhoto = (ImageView) findViewById(R.id.backdrop);
        TextView tvTitle = (TextView) findViewById(R.id.title);
        TextView tvPlace = (TextView) findViewById(R.id.place);
        TextView tvDescription = (TextView) findViewById(R.id.description);
        TextView tvAuthor = (TextView) findViewById(R.id.author);
        ImageView imgAuthor = (ImageView) findViewById(R.id.authorPic);
        final Button btnLike = (Button) findViewById(R.id.action_like);
        isPressed = false;
        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Changing the favorite button
                if (isPressed) {
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.ic_action_favorite_outline, 0, 0);
                } else {
                    btnLike.setCompoundDrawablesWithIntrinsicBounds(0,
                            R.drawable.ic_action_favorite, 0, 0);
                }
                isPressed = !isPressed;
                if (mFirebaseUser != null) {
                    Like like = new Like(mUserId);
                    mFirebaseDatabaseReference.child(PHOTOS_CHILD).child(photoKey).child(LIKE_CHILD).push().setValue(like);
                }

                //Added to database only if previous not added
                if (!likePhotoExist) {
                    ContentValues photoInfoValues = new ContentValues();
                    photoInfoValues.put(PhotoContract.PhotoEntry._ID, photoKey);
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_AUTHOR_NAME, photo.getAuthorName());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_AUTHOR_ID, photo.getAuthorId());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_AUTHOR_PHOTO_URL, photo.getAuthorPhotoUrl());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_TITLE, photo.getTitle());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_PLACE, photo.getPlace());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_DESCRIPTION, photo.getDescription());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_PHOTO, photo.getPhoto());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_PHOTO_REF, photo.getPhotoRef());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_DATE, photo.getDate());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_TIMESTAMP, photo.getTimestamp());
                    photoInfoValues.put(PhotoContract.PhotoEntry.COLUMN_INVERTED_TIMESTAMP, photo.getInvertedTimestamp());
                    Log.d(TAG, "onClick: " + PhotoContract.PhotoEntry.CONTENT_URI);
                    getContentResolver()
                            .insert(PhotoContract.PhotoEntry.CONTENT_URI, photoInfoValues);
                    Log.d(TAG, "Content Provider added photo info to database");
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

        Glide.with(this)
                .load(photo.getPhoto())
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imgPhoto);

        tvTitle.setText(photo.getTitle());
        tvPlace.setText(photo.getPlace());
        tvDescription.setText(photo.getDescription());
        tvAuthor.setText(photo.getAuthorName());
        Glide.with(this)
                .load(photo.getAuthorPhotoUrl())
                .dontAnimate()
                .into(imgAuthor);

        mRecyclerView = (RecyclerView) findViewById(R.id.commentRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);


        final EditText edComment = (EditText) findViewById(R.id.edNewComment);
        edComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    btnSendComment.setEnabled(true);
                } else {
                    btnSendComment.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnSendComment = (ImageButton) findViewById(R.id.sendCommentButton);
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strComment = edComment.getText().toString();
                String datetime = new Date().toString();
                String millis = String.valueOf(System.currentTimeMillis());
                if (mFirebaseUser != null) {
                    Comment comment = new Comment(mUsername, mUserId, mPhotoUrl, strComment, datetime, millis);
                    mFirebaseDatabaseReference.child(PHOTOS_CHILD).child(photoKey).child(COMMENT_CHILD).push().setValue(comment);
                    mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);
                } else {
                    Toast.makeText(mContext, getString(R.string.msg_sign_comment), Toast.LENGTH_SHORT).show();
                }
                edComment.setText("");

            }
        });

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(
                Comment.class,
                R.layout.detail_comment,
                CommentViewHolder.class,
                mFirebaseDatabaseReference.child(PHOTOS_CHILD).child(photoKey).child(COMMENT_CHILD)) {

            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, Comment comment, int position) {
                viewHolder.mTvAuthorName.setText(comment.getCommenterName());
                viewHolder.mTvComment.setText(comment.getComment());
                //viewHolder.messengerTextView.setText(friendlyMessage.getName());
                if (comment.getCommenterPhotoUrl() != null) {
                    Glide.with(DetailActivity.this)
                            .load(comment.getCommenterPhotoUrl())
                            .into(viewHolder.mImageView);
                }
                viewHolder.mTvTime.setText(
                        DateUtils.getRelativeTimeSpanString(Long.parseLong(comment.getMillisecond()),
                                System.currentTimeMillis(),
                                DateUtils.SECOND_IN_MILLIS).toString().toLowerCase()
                );
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int commentMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (commentMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);
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
                        .into(1920, 1080)// Width and height
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
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == PHOTO_LOADER_ID) {
            CursorLoader loader = new CursorLoader(this,
                    PhotoContract.PhotoEntry.CONTENT_URI,
                    null,
                    PhotoContract.PhotoEntry._ID + " = ?",
                    new String[]{photoKey},
                    null);
            return loader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PHOTO_LOADER_ID) {
            if (data.getCount() == 0) {
                likePhotoExist = false;
            } else {
                likePhotoExist = true;
            }
            Log.d(TAG, "onLoadFinished: Finished");
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
