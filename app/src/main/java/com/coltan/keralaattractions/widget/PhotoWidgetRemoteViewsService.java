package com.coltan.keralaattractions.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.Target;
import com.coltan.keralaattractions.R;
import com.coltan.keralaattractions.data.PhotoContract;
import com.coltan.keralaattractions.data.PhotoProvider;

import java.util.concurrent.ExecutionException;

/**
 * Created by robo on 18/9/16.
 */

public class PhotoWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new PhotoRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class PhotoRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "PhotoRemoteViewsFactory";
    private Context mContext;
    private int mAppWidgetId;
    private Cursor mCursor;

    public PhotoRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(PhotoContract.PhotoEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        String key = null;
        String title = null;
        String photo = null;
        Bitmap myBitmap = null;

        if (mCursor.moveToPosition(position)) {
            key = mCursor.getString(mCursor.getColumnIndex(PhotoContract.PhotoEntry._ID));
            title = mCursor.getString(mCursor.getColumnIndex(PhotoContract.PhotoEntry.COLUMN_TITLE));
            photo = mCursor.getString(mCursor.getColumnIndex(PhotoContract.PhotoEntry.COLUMN_PHOTO));
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.photo_item);
        try {
            myBitmap = Glide.with(mContext)
                    .load(photo)
                    .asBitmap()
                    .centerCrop()
                    .into(500, 500)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (myBitmap != null) {
            rv.setImageViewBitmap(R.id.photo, myBitmap);
        }
        rv.setTextViewText(R.id.author, title);

        // Set the click intent
        final Intent in = new Intent();
        Log.d(TAG, "getViewAt: " + key);
        in.putExtra("key", key);
        rv.setOnClickFillInIntent(R.id.stack_view, in);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
