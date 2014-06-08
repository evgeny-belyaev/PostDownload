package com.example.postdownload.app.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.example.postdownload.app.lib.SubscriptionHelper;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;



public class PostDownloadTaskFragment extends Fragment
{
    private SubscriptionHelper mSubscriptionHelper;
    private PublishSubject<List<PostItem>> mTrigger;
    private BehaviorSubject<Boolean> mDownloadButtonEnabled;
    private PublishSubject<DownloadProgress> mProgressState;

    public PostDownloadTaskFragment()
    {
        setRetainInstance(true);
    }

    public static PostDownloadTaskFragment create()
    {
        return new PostDownloadTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDownloadButtonEnabled = BehaviorSubject.create(true);
        mProgressState = PublishSubject.create();
        mTrigger = PublishSubject.create();
        mSubscriptionHelper = new SubscriptionHelper();

        Observable<DownloadProgress> downloadProcess0 = mTrigger
            .doOnNext(new Action1<List<PostItem>>()
            {
                @Override
                public void call(List<PostItem> postItems)
                {
                    mDownloadButtonEnabled.onNext(false);
                }
            })
            .flatMap(new Func1<List<PostItem>, Observable<DownloadProgress>>()
            {
                @Override
                public Observable<DownloadProgress> call(List<PostItem> postItems)
                {
                    return Observable
                        .concat(
                            Observable
                                .from(postItems)
                                .filter(new Func1<PostItem, Boolean>()
                                {
                                    @Override
                                    public Boolean call(PostItem postItem)
                                    {
                                        return postItem.isSelected;
                                    }
                                })
                                .map(new Func1<PostItem, Observable<DownloadProgress>>()
                                {
                                    @Override
                                    public Observable<DownloadProgress> call(PostItem postItem)
                                    {
                                        return downloadSong(postItem).subscribeOn(Schedulers.io());
                                    }
                                })
                        )
                        .doOnCompleted(new Action0()
                        {
                            @Override
                            public void call()
                            {
                                mDownloadButtonEnabled.onNext(true);
                            }
                        });
                }
            })
            .doOnNext(new Action1<DownloadProgress>()
            {
                @Override
                public void call(DownloadProgress progress)
                {
                    mProgressState.onNext(progress);
                }
            });

        mSubscriptionHelper.manage(downloadProcess0.subscribe());
    }

    @Override
    public void onDestroy()
    {
        mSubscriptionHelper.unsubscribe();

        super.onDestroy();
    }

    public void start(List<PostItem> postItems)
    {
        mTrigger.onNext(postItems);
    }

    public Observable<Boolean> observeDownloadButtonState()
    {
        return mDownloadButtonEnabled.asObservable();
    }

    public Observable<DownloadProgress> observeProgress()
    {
        return mProgressState.asObservable();
    }

    private Observable<DownloadProgress> downloadSong(final PostItem postItem)
    {
        return Observable
            .create(new Observable.OnSubscribe<DownloadProgress>()
                    {
                        @Override
                        public void call(Subscriber<? super DownloadProgress> subscriber)
                        {
                            URL url = postItem.songDto.url;
                            HttpURLConnection connection = null;
                            InputStream input = null;
                            FileOutputStream output = null;
                            String fileName = postItem.songDto.artist + " - " + postItem.songDto.title + ".mp3";

                            try
                            {
                                connection = (HttpURLConnection)url.openConnection();
                                connection.connect();

                                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                                {
                                    subscriber.onError(new RuntimeException(connection.getResponseMessage()));
                                }

                                input = connection.getInputStream();
                                int fileLength = connection.getContentLength();

                                output = new FileOutputStream("/storage/extSdCard/test/" + fileName);

                                byte data[] = new byte[1024 * 50];
                                long total = 0;
                                int count;

                                while ((count = input.read(data)) != -1)
                                {
                                    if (subscriber.isUnsubscribed())
                                    {
                                        input.close();
                                        return;
                                    }

                                    total += count;

                                    if (fileLength > 0) // only if total length is known
                                    {
                                        DownloadProgress downloadProgress = new DownloadProgress();
                                        downloadProgress.fileName = fileName;
                                        downloadProgress.progress = (int)(total * 100 / fileLength);
                                        downloadProgress.url = url;

                                        subscriber.onNext(downloadProgress);
                                    }

                                    output.write(data, 0, count);
                                }

                                subscriber.onCompleted();
                            }
                            catch (Exception e)
                            {
                                subscriber.onError(e);
                            }
                            finally
                            {
                                try
                                {
                                    if (output != null)
                                    {
                                        output.close();
                                    }
                                    if (input != null)
                                    {
                                        input.close();
                                    }
                                }
                                catch (IOException ignored)
                                {
                                }

                                if (connection != null)
                                {
                                    connection.disconnect();
                                }
                            }
                        }
                    }
            );
    }
}
