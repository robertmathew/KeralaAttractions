package com.coltan.keralaattractions.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.RemoteViews;

import com.coltan.keralaattractions.R;
import com.coltan.keralaattractions.data.PhotoContract;

/**
 * Created by robo on 18/9/16.
 */

public class PhotoCollectionWidget extends AppWidgetProvider {

    public static String CLICK_ACTION = "com.coltan.keralaatractions.photowidget.CLICK";

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static PhotoDataProviderObserver sDataObserver;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            final Intent intent = new Intent(context, PhotoWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_collection);
            views.setRemoteAdapter(R.id.stack_view, intent);

            final Intent onClickIntent = new Intent(context, PhotoCollectionWidget.class);
            onClickIntent.setAction(PhotoCollectionWidget.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.stack_view, onClickPendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        final ContentResolver r = context.getContentResolver();
        if (sDataObserver == null) {
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, PhotoCollectionWidget.class);
            sDataObserver = new PhotoDataProviderObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(PhotoContract.PhotoEntry.CONTENT_URI, true, sDataObserver);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(CLICK_ACTION)) {
            final String key = intent.getStringExtra("key");


            /*Intent i = new Intent(ctx, DetailActivity.class);
            i.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("key", key);
            ctx.startActivity(i);*/
        }
        super.onReceive(context, intent);
    }
}

class PhotoDataProviderObserver extends ContentObserver {
    private AppWidgetManager mAppWidgetManager;
    private ComponentName mComponentName;

    PhotoDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
        super(h);
        mAppWidgetManager = mgr;
        mComponentName = cn;
    }
    @Override
    public void onChange(boolean selfChange) {
        // The data has changed, so notify the widget that the collection view needs to be updated.
        // In response, the factory's onDataSetChanged() will be called which will requery the
        // cursor for the new data.
        mAppWidgetManager.notifyAppWidgetViewDataChanged(
                mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.stack_view);
    }
}


