package com.example.postdownload.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.example.postdownload.app.core.PostDownloadTaskFragment;
import com.example.postdownload.app.core.PostDto;
import com.example.postdownload.app.core.PostItem;
import com.example.postdownload.app.core.SongDto;
import com.example.postdownload.app.lib.FragmentHelper;
import com.example.postdownload.app.lib.SubscriptionHelper;
import rx.android.observables.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func0;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
{
    public static int REQUEST_CODE_OPEN_ACTIVITY = 0;
    public static String INTENT_EXTRA_POST_DTO = "url";
    private PostDto mPostDto;
    private LinearLayout mList;
    private List<PostItem> mPostItems = new ArrayList<PostItem>();
    private PostDownloadTaskFragment mDownloader;
    private SubscriptionHelper mSubscriptionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Intent intent = getIntent();

        mList = (LinearLayout)findViewById(R.id.songs_list);

        mPostDto = getPost(intent);
        ((TextView)findViewById(R.id.post_title)).setText(mPostDto.title);

        mDownloader = FragmentHelper.createOrRestore(getSupportFragmentManager(), "downloader", new Func0<PostDownloadTaskFragment>()
        {
            @Override
            public PostDownloadTaskFragment call()
            {
                return PostDownloadTaskFragment.create();
            }
        });

        mSubscriptionHelper = new SubscriptionHelper();

        mSubscriptionHelper.manage(
            ViewObservable
                .clicks(((Button)findViewById(R.id.download)), false)
                .subscribe(new Action1<Button>()
                {
                    @Override
                    public void call(Button button)
                    {
                        mDownloader.start(mPostItems);
                    }
                })
        );

        fillList();
    }

    private void fillList()
    {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int size = mPostDto.songs.size();
        for (int i = 0; i < size; i++)
        {
            final SongDto songDto = mPostDto.songs.get(i);
            final PostItem postItem = new PostItem();
            postItem.songDto = songDto;
            postItem.isSelected = true;

            View view = inflater.inflate(R.layout.songs_list_item, mList, false);

            ((TextView)view.findViewById(R.id.song_list_item_title)).setText(songDto.title);

            ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.song_list_item_progress);
            postItem.progressView = progressBar;

            CheckBox checkBox = (CheckBox)view.findViewById(R.id.song_list_item_is_selected);
            checkBox.setChecked(postItem.isSelected);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    postItem.isSelected = isChecked;
                }
            });

            mList.addView(view);
            mPostItems.add(postItem);
        }
    }

    public static PostDto getPost(Intent intent)
    {
        return (PostDto)intent.getSerializableExtra(INTENT_EXTRA_POST_DTO);
    }

    @Override
    protected void onDestroy()
    {
        mSubscriptionHelper.unsubscribe();

        super.onDestroy();
    }
}

