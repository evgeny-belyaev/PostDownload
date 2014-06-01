package com.example.postdownload.app.core;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.example.postdownload.app.lib.SubscriptionHelper;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
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

    private class DownloadProgress
    {
        public String fileName;
        public int progress;
    }

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

        mTrigger = PublishSubject.create();
        mSubscriptionHelper = new SubscriptionHelper();

        Observable<DownloadProgress> c = Observable
            .concat(
                mTrigger
                    .flatMap(new Func1<List<PostItem>, Observable<PostItem>>()
                    {
                        @Override
                        public Observable<PostItem> call(List<PostItem> postItems)
                        {
                            return Observable.from(postItems);
                        }
                    })
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
            .doOnNext(new Action1<DownloadProgress>()
            {
                @Override
                public void call(DownloadProgress progress)
                {
                    Log.d("PostDownloadTaskFragment", progress.progress + "%" + ": " + progress.fileName);
                }
            });

        mSubscriptionHelper.manage(c.subscribe());
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

                                byte data[] = new byte[4096];
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
