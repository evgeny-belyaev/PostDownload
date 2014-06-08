package com.example.postdownload.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.postdownload.app.core.PostDto;
import com.example.postdownload.app.core.PostItem;
import com.example.postdownload.app.core.SongDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrackListFragment extends Fragment
{
    private LinearLayout mList;
    private Button mDownloadButton;
    private TextView mTitle;
    private PostDto mPostDto;

    private List<PostItem> mPostItems = new ArrayList<>();
    private HashMap<String, PostItemState> mListState = new HashMap<>();
    private ImageButton mChooseDir;
    private TextView mDownloadTo;
    private TextView mFreeSpace;
    private ImageButton mExpand;

    private boolean mExpanded = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mPostDto = MainActivity.getPost(getActivity().getIntent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.track_list_fragment, container, false);

        mList = (LinearLayout)view.findViewById(R.id.songs_list);
        mDownloadButton = (Button)view.findViewById(R.id.controls_start_download);
        mTitle = (TextView)view.findViewById(R.id.title);

        mExpand = (ImageButton)view.findViewById(R.id.controls_expand);
        mChooseDir = (ImageButton)view.findViewById(R.id.controls_change_choose_dir);
        mDownloadTo = (TextView)view.findViewById(R.id.controls_download_to);
        mFreeSpace = (TextView)view.findViewById(R.id.controls_free_space);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mTitle.setText(mPostDto.title);

        mExpand.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mExpanded)
                {
                    collapseSettings();
                }
                else
                {
                    expandSettings();
                }

                mExpanded = !mExpanded;
            }
        });

        collapseSettings();
        fillList();
    }

    private void fillList()
    {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int size = mPostDto.songs.size();
        for (int i = 0; i < size; i++)
        {
            final SongDto songDto = mPostDto.songs.get(i);
            final String postItemKey = songDto.url.toString();

            final PostItem postItem = new PostItem();
            postItem.songDto = songDto;
            postItem.isSelected = true;

            final CheckableRelativeLayout view = (CheckableRelativeLayout)inflater.inflate(R.layout.songs_list_item, mList, false);

            ((TextView)view.findViewById(R.id.song_list_item_title)).setText(songDto.title);
            ((TextView)view.findViewById(R.id.song_list_item_artist)).setText(songDto.artist);

            final CheckBox checkBox = (CheckBox)view.findViewById(R.id.song_list_item_is_selected);
            checkBox.setChecked(postItem.isSelected);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    postItem.isSelected = mListState.get(postItemKey).isChecked = isChecked;
                    view.setChecked(isChecked);
                }
            });

            PostItemState defaultState = new PostItemState();
            defaultState.progress = 0;
            defaultState.isChecked = true;
            view.setChecked(true);

            view.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    view.toggle();
                    checkBox.toggle();
                }
            });

            mListState.put(postItemKey, defaultState);

            mList.addView(view);

            mPostItems.add(postItem);
        }
    }

    private void collapseSettings()
    {
        mChooseDir.setVisibility(View.GONE);
        mDownloadTo.setVisibility(View.GONE);
        mFreeSpace.setVisibility(View.GONE);

        mExpand.setActivated(false);
    }

    private void expandSettings()
    {
        mChooseDir.setVisibility(View.VISIBLE);
        mDownloadTo.setVisibility(View.VISIBLE);
        mFreeSpace.setVisibility(View.VISIBLE);

        mExpand.setActivated(true);
    }
}
