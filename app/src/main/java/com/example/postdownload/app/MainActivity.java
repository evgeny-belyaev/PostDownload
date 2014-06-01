package com.example.postdownload.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.example.postdownload.app.core.PostDto;
import com.example.postdownload.app.core.SongDto;
import rx.Observable;
import rx.android.observables.ViewObservable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
{
    public static int REQUEST_CODE_OPEN_ACTIVITY = 0;
    public static String INTENT_EXTRA_POST_DTO = "url";
    private PostDto mPostDto;
    private LinearLayout mList;
    private List<PostItem> mPostItems = new ArrayList<PostItem>();

    private class PostItem
    {
        public SongDto songDto;
        public boolean isSelected;
        public ProgressBar progressView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Intent intent = getIntent();

        mList = (LinearLayout)findViewById(R.id.songs_list);

        mPostDto = getPost(intent);
        ((TextView)findViewById(R.id.post_title)).setText(mPostDto.title);

        ViewObservable
            .clicks(((Button)findViewById(R.id.download)), false)
            .flatMap(new Func1<Button, Observable<PostItem>>()
            {
                @Override
                public Observable<PostItem> call(Button button)
                {
                    return Observable.from(mPostItems);
                }
            });

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

            View view = inflater.inflate(R.layout.songs_list_item, mList, false);

            ((TextView)view.findViewById(R.id.song_list_item_title)).setText(songDto.title);

            ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.song_list_item_progress);
            postItem.progressView = progressBar;

            CheckBox checkBox = (CheckBox)view.findViewById(R.id.song_list_item_is_selected);
            checkBox.setChecked(true);
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
}

