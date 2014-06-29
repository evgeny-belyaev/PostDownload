package com.ugene.postdownload.app.core;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.ugene.postdownload.app.R;
import com.ugene.postdownload.app.lib.SubscriptionHelper;
import com.ugene.postdownload.app.ui.MainActivity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.util.async.Async;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

public class ClipboardMonitorService extends Service
{
    private static final String TAG = "ClipboardMonitorService";

    private ClipboardManager mClipboardManager;
    private PublishSubject<URL> mTrigger;
    private SubscriptionHelper mSubscriptionHelper = new SubscriptionHelper();
    private NotificationManager mNotificationManager;
    private int mNotificationId = 0;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);

        mTrigger = PublishSubject.create();

        mSubscriptionHelper.manage(
            mTrigger
                .filter(new Func1<URL, Boolean>()
                {
                    @Override
                    public Boolean call(URL url)
                    {
                        return isOnline(ClipboardMonitorService.this);
                    }
                })
                .doOnNext(new Action1<URL>()
                {
                    @Override
                    public void call(URL url)
                    {
                        mNotificationId++;
                    }
                })
                .doOnNext(new Action1<URL>()
                {
                    @Override
                    public void call(URL url)
                    {
                        showNotification(mNotificationId);
                    }
                })
                .flatMap(new Func1<URL, Observable<PostDto>>()
                {
                    @Override
                    public Observable<PostDto> call(URL url)
                    {
                        Log.d("ClipboardMonitorService", url.toString());

                        return
                            downloadPost(url)
                                .map(new Func1<Document, PostDto>()
                                {
                                    @Override
                                    public PostDto call(Document document)
                                    {
                                        return parsePost(document);
                                    }
                                });
                    }
                })
                .doOnError(new Action1<Throwable>()
                {
                    @Override
                    public void call(Throwable throwable)
                    {
                        cancelNotification(mNotificationId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(
                    new Action1<PostDto>()
                    {
                        @Override
                        public void call(PostDto postDto)
                        {
                            updateNotification(mNotificationId, postDto);
                        }
                    },
                    new Action1<Throwable>()
                    {
                        @Override
                        public void call(Throwable throwable)
                        {
                            int i = 5;
                        }
                    }
                )
        );

        mSubscriptionHelper.manage(
            mTrigger
                .filter(new Func1<URL, Boolean>()
                {
                    @Override
                    public Boolean call(URL url)
                    {
                        return !isOnline(ClipboardMonitorService.this);
                    }
                })
                .subscribe(new Action1<URL>()
                {
                    @Override
                    public void call(URL url)
                    {
                        Toast
                            .makeText(ClipboardMonitorService.this, getString(R.string.device_is_offline), Toast.LENGTH_LONG)
                            .show();
                    }
                })
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

        mSubscriptionHelper.unsubscribe();
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
                    // ignore all errors
                }
            }
        };

    private void cancelNotification(int notificationId)
    {
        mNotificationManager.cancel(notificationId);
    }

    private void showNotification(int id)
    {
        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_post_is_downloading))
                .setProgress(0, 0, true);

        mNotificationManager.notify(id, builder.build());
    }

    private void updateNotification(int id, PostDto postDto)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(MainActivity.INTENT_EXTRA_POST_DTO, postDto);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            MainActivity.REQUEST_CODE_OPEN_ACTIVITY,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        );

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(postDto.title)
                .setProgress(0, 0, false)
                .setContentIntent(pendingIntent);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, builder.build());
    }

    private static Observable<Document> downloadPost(final URL url)
    {
        return Async.fromCallable(new Callable<Document>()
        {
            @Override
            public Document call() throws Exception
            {
                return Jsoup.connect(url.toString())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
                    .get();
            }
        }, Schedulers.io());
    }

    private static PostDto parsePost(Document document)
    {
        /*
        *   post title - document.select("div.wi_author").first().text()
            post body - document.select("div.wi_body").first().text()
            song - document.select("div.audio input")
            title - document.select("div.audio td.info span.title")
            artist - document.select("div.audio td.info b")
            song duration - document.select("div.audio td.info .duration")
        * */

        PostDto postDto = new PostDto();

        postDto.title = document.select("div.fw_post_name").first().text();
        postDto.body = document.select("div.wall_post_text").first().text();

        Elements urls = document.select("div.audio input");
        Elements titles = document.select("div.audio td.info span.title");
        Elements artists = document.select("div.audio td.info b");
        Elements durations = document.select("div.audio td.info .duration");

        int size = urls.size();

        for (int i = 0; i < size; i++)
        {
            TrackDto trackDto = new TrackDto();

            trackDto.title = titles.get(i).text();
            trackDto.artist = artists.get(i).text();
            trackDto.duration = durations.get(i).text();

            try
            {
                trackDto.url = new URL(urls.get(i).val());
            }
            catch (MalformedURLException e)
            {
                continue;
            }

            postDto.songs.add(trackDto);
        }

        return postDto;
    }

    public static boolean isOnline(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }
}
