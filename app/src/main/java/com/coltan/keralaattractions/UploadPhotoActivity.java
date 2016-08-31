package com.coltan.keralaattractions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UploadPhotoActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "UploadPhotoActivity";
    private String mUsername;
    private String mUsernameId;
    private String mPhotoUrl;

    private String authorId;
    private String authorName;
    private String authorPhotoUrl;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsernameId = mFirebaseUser.getUid();
            mUsername = mFirebaseUser.getDisplayName();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        final EditText edTitle = (EditText) findViewById(R.id.etTitle);
        final EditText edPlace = (EditText) findViewById(R.id.etPlace);

        Button btnUpload = (Button) findViewById(R.id.upload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                authorName = mUsername;
                authorId = mUsernameId;
                authorPhotoUrl = mPhotoUrl;
                String title = edTitle.getText().toString();
                String place = edPlace.getText().toString();
                Log.d(TAG, "Data: " + authorName + " " + authorId + " " + authorPhotoUrl);
                Log.d(TAG, "Data: " + title + " " + place);

                Photo photo = new Photo(authorName, authorId, authorPhotoUrl, title, place);
                mDatabaseReference.child("photos").push().setValue(photo);
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
}
