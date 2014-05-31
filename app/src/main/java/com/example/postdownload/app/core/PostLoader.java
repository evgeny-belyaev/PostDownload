package com.example.postdownload.app.core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

import java.net.URL;
import java.util.concurrent.Callable;

public class PostLoader
{
    public static Observable<Document> downloadPost(final URL url)
    {
        return Async.fromCallable(new Callable<Document>()
        {
            @Override
            public Document call() throws Exception
            {
                return Jsoup.connect(url.toString()).get();
            }
        }, Schedulers.io());
    }

    public static PostDto parsePost(Document document)
    {
        return new PostDto();
    }
}
