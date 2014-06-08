package com.example.postdownload.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.example.postdownload.app.core.PostDownloadTaskFragment;
import com.example.postdownload.app.core.PostDto;
import com.example.postdownload.app.core.PostItem;
import com.example.postdownload.app.core.TrackDto;
import com.example.postdownload.app.lib.FragmentHelper;
import com.example.postdownload.app.lib.SubscriptionHelper;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;
import rx.functions.Func0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends FragmentActivity implements DirectoryChooserFragment.OnFragmentInteractionListener
{
    public static int REQUEST_CODE_OPEN_ACTIVITY = 0;
    public static String INTENT_EXTRA_POST_DTO = "postDto";
    private PostDto mPostDto;
    private LinearLayout mList;
    private HashMap<String, ProgressBar> mProgressBars = new HashMap<>();
    private HashMap<String, PostItemState> mListState = new HashMap<>();
    private List<PostItem> mPostItems = new ArrayList<>();
    private PostDownloadTaskFragment mDownloader;
    private SubscriptionHelper mSubscriptionHelper;
    private Button mDownloadButton;

    @Override
    public void onSelectDirectory(@NonNull String path)
    {

    }

    @Override
    public void onCancelChooser()
    {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FragmentHelper.replaceOrRestore(getSupportFragmentManager(), R.id.outer, "trackListFragment", new Func0<Fragment>()
        {
            @Override
            public Fragment call()
            {
                return new TrackListFragment();
            }
        });

//        Intent intent = getIntent();
//
//        mSubscriptionHelper = new SubscriptionHelper();
//        mList = (LinearLayout)findViewById(R.id.songs_list);
//        mDownloadButton = (Button)findViewById(R.id.controls_start_download);
//
//        mPostDto = getPost(intent);
//        ((TextView)findViewById(R.id.title)).setText(mPostDto.title);
//
//        mDownloader = FragmentHelper.createOrRestore(getSupportFragmentManager(), "downloader", new Func0<PostDownloadTaskFragment>()
//        {
//            @Override
//            public PostDownloadTaskFragment call()
//            {
//                return PostDownloadTaskFragment.create();
//            }
//        });
//
//        fillList();

        //        final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);
        //
        //        // Optional: Allow users to create a new directory with a fixed name.
        //        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME,
        //            "DirChooserSample");
        //
        //        // REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
        //        startActivityForResult(chooserIntent, 0);
    }


    private void fillList()
    {
        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int size = mPostDto.songs.size();
        for (int i = 0; i < size; i++)
        {
            final TrackDto trackDto = mPostDto.songs.get(i);
            final String postItemKey = trackDto.url.toString();
            final PostItem postItem = new PostItem();
            postItem.mTrackDto = trackDto;
            postItem.isSelected = true;

            View view = inflater.inflate(R.layout.songs_list_item, mList, false);

            ((TextView)view.findViewById(R.id.song_list_item_title)).setText(trackDto.title);

            ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.song_list_item_progress);
            mProgressBars.put(postItemKey, progressBar);

            CheckBox checkBox = (CheckBox)view.findViewById(R.id.song_list_item_is_selected);
            checkBox.setChecked(postItem.isSelected);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    postItem.isSelected = mListState.get(postItemKey).isChecked = isChecked;
                }
            });

            PostItemState defaultState = new PostItemState();
            defaultState.progress = 0;
            defaultState.isChecked = true;

            mListState.put(postItemKey, defaultState);
            mList.addView(view);

            mPostItems.add(postItem);
        }
    }

    public static PostDto getPost(Intent intent)
    {
        return (PostDto)intent.getSerializableExtra(INTENT_EXTRA_POST_DTO);
    }

//    @Override
    protected void onPause1()
    {
        super.onPause();

        mSubscriptionHelper.unsubscribe();
    }
}

