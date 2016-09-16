package com.coltan.keralaattractions;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

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

    private static final String MESSAGE_SENT_EVENT = "message_sent";
    public static final String PHOTOS_CHILD = "photos";
    public static final String COMMENT_CHILD = "comments";

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

    private ImageButton btnSendComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (mFirebaseUser != null) {
            mUsername = mFirebaseUser.getDisplayName();
            mUserId = mFirebaseUser.getUid();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        Photo photo = getIntent().getExtras().getParcelable("photo");
        final String photoKey = getIntent().getExtras().getString("key");
        //Log.d(TAG, "onCreate: " + photoKey);

        ImageView imgPhoto = (ImageView) findViewById(R.id.backdrop);
        TextView tvTitle = (TextView) findViewById(R.id.title);
        TextView tvPlace = (TextView) findViewById(R.id.place);
        TextView tvDescription = (TextView) findViewById(R.id.description);
        TextView tvAuthor = (TextView) findViewById(R.id.author);
        ImageView imgAuthor = (ImageView) findViewById(R.id.authorPic);

        Glide.with(this)
                .load(photo.getPhoto())
                .priority(Priority.HIGH)
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
                Comment comment = new Comment(mUsername, mUserId, mPhotoUrl, strComment, datetime, millis);
                mFirebaseDatabaseReference.child(PHOTOS_CHILD).child(photoKey).child(COMMENT_CHILD).push().setValue(comment);
                edComment.setText("");
                mFirebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null);

            }
        });

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
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
}
