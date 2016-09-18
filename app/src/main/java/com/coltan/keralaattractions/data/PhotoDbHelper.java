package com.coltan.keralaattractions.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coltan.keralaattractions.data.PhotoContract.PhotoEntry;

/**
 * Created by robo on 18/9/16.
 */

public class PhotoDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "attractions.db";

    public PhotoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PHOTO_TABLE = "CREATE TABLE " + PhotoEntry.TABLE_NAME + " (" +
                PhotoEntry._ID + " TEXT PRIMARY KEY, " +
                PhotoEntry.COLUMN_AUTHOR_NAME + " TEXT, " +
                PhotoEntry.COLUMN_AUTHOR_ID + " TEXT, " +
                PhotoEntry.COLUMN_AUTHOR_PHOTO_URL + " TEXT, " +
                PhotoEntry.COLUMN_TITLE + " TEXT, " +
                PhotoEntry.COLUMN_PLACE + " TEXT, " +
                PhotoEntry.COLUMN_DESCRIPTION + " TEXT, " +
                PhotoEntry.COLUMN_PHOTO + " TEXT, " +
                PhotoEntry.COLUMN_PHOTO_REF + " TEXT, " +
                PhotoEntry.COLUMN_DATE + " TEXT, " +
                PhotoEntry.COLUMN_TIMESTAMP + " TEXT, " +
                PhotoEntry.COLUMN_INVERTED_TIMESTAMP + " TEXT " +
                ")";
        db.execSQL(SQL_CREATE_PHOTO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do for now
    }
}
