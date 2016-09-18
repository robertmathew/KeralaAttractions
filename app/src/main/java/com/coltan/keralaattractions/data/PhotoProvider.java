package com.coltan.keralaattractions.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by robo on 18/9/16.
 */

public class PhotoProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private PhotoDbHelper mOpenHelper;

    /* Codes for the UriMatcher */
    private static final int PHOTO = 100;

    private static UriMatcher buildUriMatcher() {
        // Build a UriMatcher by adding a specific code to return based on a match
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = PhotoContract.CONTENT_AUTHORITY;

        // Photo
        matcher.addURI(authority, PhotoContract.PATH_PHOTO, PHOTO);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new PhotoDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            // "photo"
            case PHOTO:
                cursor = mOpenHelper.getReadableDatabase().query(
                        PhotoContract.PhotoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return cursor;
            default:
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PHOTO: {
                return PhotoContract.PhotoEntry.CONTENT_TYPE;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case PHOTO: {
                long _id = db.insert(PhotoContract.PhotoEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PhotoContract.PhotoEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
