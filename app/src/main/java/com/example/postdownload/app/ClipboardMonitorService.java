package com.example.postdownload.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

public class ClipboardMonitorService extends Service
{
    private static final String TAG = "ClipboardMonitorService";

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

                URL url;

                try
                {
                    ClipData.Item item = clip.getItemAt(0);
                    CharSequence text = item.getText();

                    if (TextUtils.isEmpty(text))
                    {
                        return;
                    }

                    url = new URL(text.toString());
                }
                catch (MalformedURLException e)
                {
                    return; // ignore all errors
                }

                showNotification(url);
            }
        };

    private void showNotification(URL url)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.INTENT_EXTRA_URL, url);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            MainActivity.REQUEST_CODE_OPEN_ACTIVITY,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        );

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Copied url")
                .setContentText(url.toString())
                .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }
}
