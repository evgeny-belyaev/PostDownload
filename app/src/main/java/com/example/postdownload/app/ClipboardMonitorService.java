package com.example.postdownload.app;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ClipboardMonitorService extends Service
{
    private static final String TAG = "ClipboardManager";

    private ClipboardManager mClipboardManager;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        Log.d(TAG, "Service started");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mClipboardManager != null)
        {
            mClipboardManager.removePrimaryClipChangedListener(
                mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
        new ClipboardManager.OnPrimaryClipChangedListener()
        {
            @Override
            public void onPrimaryClipChanged()
            {
                Log.d(TAG, "onPrimaryClipChanged");
                ClipData clip = mClipboardManager.getPrimaryClip();

                NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(ClipboardMonitorService.this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Copied url")
                        .setContentText(clip.getItemAt(0).getText());

                NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                // mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.build());
            }
        };

}
