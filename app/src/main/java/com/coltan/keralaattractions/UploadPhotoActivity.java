package com.coltan.keralaattractions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.Date;

public class UploadPhotoActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "UploadPhotoActivity";

    private static final int READ_REQUEST_CODE = 42;
    private static final int RC_SIGN_IN = 123;

    private String mUsername;
    private String mUsernameId;
    private String mPhotoUrl;

    private Photo photo;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabaseReference;
    private StorageReference storageRef;

    private ImageButton imageView;
    private EditText edTitle, edPlace, edDescription;

    private Uri imageUri;
    String photoPath;
    long millis;
    String datetime;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mContext = this;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                            .setProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                            .setTheme(R.style.SignInTheme)
                            .build(),
                    RC_SIGN_IN);
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
        // Create a storage reference from our app
        storageRef = storage.getReferenceFromUrl("gs://keralaattractions-fd4fe.appspot.com");

        edTitle = (EditText) findViewById(R.id.etTitle);
        edPlace = (EditText) findViewById(R.id.etPlace);
        edDescription = (EditText) findViewById(R.id.etDescription);
        imageView = (ImageButton) findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        FloatingActionButton fabUpload = (FloatingActionButton) findViewById(R.id.fabUpload);
        fabUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitPhoto();
            }
        });
    }

    private void submitPhoto() {
        if (TextUtils.isEmpty(edTitle.getText().toString())) {
            edTitle.setError(getString(R.string.required));
            return;
        }
        if (TextUtils.isEmpty(edPlace.getText().toString())) {
            edPlace.setError(getString(R.string.required));
            return;
        }

        uploadImage();
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */

    public void performFileSearch() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            imageUri = null;
            if (resultData != null) {
                imageUri = resultData.getData();
                Log.i(TAG, "Uri: " + imageUri.toString());
                showImage(imageUri);
            }
        }

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in!
                Log.d(TAG, "onActivityResult: User signed in");
                SharedPreferences sharedPrefAuth = getSharedPreferences(getString(R.string.prefs_auth_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefAuth.edit();
                editor.putBoolean(getString(R.string.prefs_is_auth), true);
                editor.commit();
            } else {
                // user is not signed in. Maybe just wait for the user to press
                // "sign in" again, or show a message
                Log.d(TAG, "onActivityResult:  User didn't signed in");
                Toast.makeText(mContext, getString(R.string.msg_sign_required), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showImage(Uri uri) {
        Glide.with(this)
                .load(uri)
                .placeholder(R.color.placeholder)
                .into(imageView);
    }

    //Uploading image to Firebase Storage
    private void uploadImage() {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(mContext)
                .title(R.string.progress_upload)
                .content(R.string.please_wait)
                .cancelable(false)
                .progress(true, 0)
                .show();

        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        //Unique filename for images with author name, date and millisecond
        millis = System.currentTimeMillis();
        datetime = new Date().toString();
        String datetimeFile = datetime;
        datetimeFile = datetimeFile.replace(" ", "");
        datetimeFile = datetimeFile.replace(":", "");
        String filename = String.format("%s_%s_%s", mUsername.replace(" ", ""), datetimeFile, millis);

        // Create a reference to "mountains.jpg"
        StorageReference mountainsRef = storageRef.child("images/" + filename + ".jpg");

        // Upload file and metadata to the path 'images/mountains.jpg'
        UploadTask uploadTask = mountainsRef.putFile(imageUri, metadata);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                progressDialog.dismiss();
                Log.d(TAG, "onFailure: Failed to upload");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                photoPath = taskSnapshot.getMetadata().getPath();
                //Log.d(TAG, "onSuccess: " + photoPath);
                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                //Log.d(TAG, "onSuccess: " + downloadUrl);
                progressDialog.dismiss();
                uploadData(downloadUrl.toString(), photoPath);
            }
        });
    }

    private void uploadData(String downloadUri, String photoPath) {
        String title = edTitle.getText().toString();
        String place = edPlace.getText().toString();
        String description = edDescription.getText().toString();
        String invertedDate = String.valueOf(-1 * new Date().getTime());

        photo = new Photo(mUsername, mUsernameId, mPhotoUrl, title, place, description, downloadUri,
                photoPath, datetime, String.valueOf(millis), invertedDate);
        mDatabaseReference.child("photos").push().setValue(photo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d(TAG, "onComplete: " + databaseError);
                if (databaseError == null) {
                    finishActivity();
                } else {
                    Toast.makeText(mContext, getString(R.string.failed_upload), Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    public void finishActivity() {
        finish();
    }
}
