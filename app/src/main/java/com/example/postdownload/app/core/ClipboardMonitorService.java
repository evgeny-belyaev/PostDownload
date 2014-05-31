package com.example.postdownload.app.core;

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
import com.example.postdownload.app.MainActivity;
import com.example.postdownload.app.R;
import org.jsoup.nodes.Document;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.net.MalformedURLException;
import java.net.URL;

public class ClipboardMonitorService extends Service
{
    private static final String TAG = "ClipboardMonitorService";

    private ClipboardManager mClipboardManager;
    private PublishSubject<URL> mTrigger;
    private Subscription mSubscription;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        mTrigger = PublishSubject.create();

        mSubscription = mTrigger
            .flatMap(new Func1<URL, Observable<PostDto>>()
            {
                @Override
                public Observable<PostDto> call(URL url)
                {
                    return PostLoader
                        .downloadPost(url)
                        .map(new Func1<Document, PostDto>()
                        {
                            @Override
                            public PostDto call(Document document)
                            {
                                return PostLoader.parsePost(document);
                            }
                        });
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                new Action1<PostDto>()
                {
                    @Override
                    public void call(PostDto postDto)
                    {
                        showNotification(postDto);
                    }
                }
            );
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

        mSubscription.unsubscribe();
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

                    mTrigger.onNext(new URL(text.toString()));
                }
                catch (MalformedURLException e)
                {
                    return; // ignore all errors
                }
            }
        };

    private void showNotification(PostDto postDto)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.INTENT_EXTRA_POST_DTO, postDto);

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
                .setContentText(postDto.title)
                .setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

}
