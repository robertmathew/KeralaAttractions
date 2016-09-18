package com.coltan.keralaattractions.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by robo on 18/9/16.
 */

public class PhotoContract {

    public static final String CONTENT_AUTHORITY = "com.coltan.keralaattractions";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PHOTO = "photo";

    public static final class PhotoEntry implements BaseColumns{

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PHOTO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PHOTO;

        // Table name
        public static final String TABLE_NAME = "photo";

        // Columns
        public static final String _ID = "_id";
        public static final String COLUMN_AUTHOR_NAME = "author";
        public static final String COLUMN_AUTHOR_ID = "author_id";
        public static final String COLUMN_AUTHOR_PHOTO_URL = "author_photo_url";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_PLACE = "place";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PHOTO = "photo";
        public static final String COLUMN_PHOTO_REF = "photo_ref";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_INVERTED_TIMESTAMP = "inverted_timestamp";

        public static Uri buildPhotoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
