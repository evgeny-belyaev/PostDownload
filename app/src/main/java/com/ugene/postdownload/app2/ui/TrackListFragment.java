package com.ugene.postdownload.app2.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bugsense.trace.BugSenseHandler;
import com.ugene.postdownload.app2.R;
import com.ugene.postdownload.app2.core.*;
import com.ugene.postdownload.app2.ui.picker.MyDirectoryChooserFragment;
import rx.Observable;
import rx.android.observables.ViewObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class TrackListFragment extends Fragment
{
    private static final String BUNDLE_KEY_STATE = "state";
    private static final String BUNDLE_KEY_TITLE = "title";
    private static final String BUNDLE_KEY_SAVE_PATH = "savePath";
    private static final String BUNDLE_KEY_SETTINGS_EXPANDED = "settingsExpanded";
    private static final java.lang.String BUNDLE_KEY_FIRST_TRACK_ARTIST = "firstTrackArtist";
    private LinearLayout mTrackList;
    private Button mDownloadButton;
    private TextView mTitle;

    private HashMap<String, TrackModel> mListState = new HashMap<>();
    private ImageButton mChooseDir;
    private TextView mDownloadTo;
    private TextView mFreeSpace;
    private ImageButton mExpand;

    private SubscriptionHelper mSubscriptionHelper;
    private String mPostTitle;
    private MyDirectoryChooserFragment mDirectoryPicker;

    private String mSavePath;
    private DownloadManager mDownloadManager;
    private String mFirstTrackArtist;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSubscriptionHelper = new SubscriptionHelper();

        if (savedInstanceState != null)
        {
            mListState = (HashMap<String, TrackModel>)savedInstanceState.getSerializable(BUNDLE_KEY_STATE);
            mPostTitle = savedInstanceState.getString(BUNDLE_KEY_TITLE);
            mSavePath = savedInstanceState.getString(BUNDLE_KEY_SAVE_PATH);
            mFirstTrackArtist = savedInstanceState.getString(BUNDLE_KEY_FIRST_TRACK_ARTIST);
        }
        else
        {
            PostDto postDto = MainActivity.getPost(getActivity().getIntent());
            mPostTitle = postDto.title;
            mFirstTrackArtist = postDto.songs.get(0).artist;
            createListState(postDto);
            mSavePath = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                .getAbsolutePath();
        }

        mDownloadManager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        mDirectoryPicker = MyDirectoryChooserFragment.newInstance(mSavePath, mFirstTrackArtist);
    }

    private void createListState(PostDto postDto)
    {
        int size = postDto.songs.size();
        for (int i = 0; i < size; i++)
        {
            final TrackDto trackDto = postDto.songs.get(i);
            final String trackUrl = trackDto.url.toString();

            final TrackModel trackModel = new TrackModel();
            trackModel.progress = 0;
            trackModel.isChecked = true;
            trackModel.mTrackDto = trackDto;
            trackModel.index = i;

            mListState.put(trackUrl, trackModel);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.track_list_fragment, container, false);

        mTrackList = (LinearLayout)view.findViewById(R.id.songs_list);
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

        mTitle.setText(mPostTitle);

        updateDownloadTo();

        mExpand.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mExpand.isActivated())
                {
                    collapseSettings();
                }
                else
                {
                    expandSettings();
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.getBoolean(BUNDLE_KEY_SETTINGS_EXPANDED))
        {
            expandSettings();
        }
        else
        {
            collapseSettings();
        }

        fillList();
    }

    private void updateDownloadTo()
    {
        File f = new File(mSavePath);
        mDownloadTo.setText(String.format(this.getString(R.string.save_to), f.getParentFile()
            .getName(), f.getName()));

        updateFreeSpace();
    }

    private void updateFreeSpace()
    {
        StatFs stat = new StatFs(mSavePath);
        long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
        double mbFree = (double)bytesAvailable / (1024 * 1024);

        if (mbFree > 1024)
        {
            double gbFree = mbFree / 1024;
            DecimalFormat df = new DecimalFormat("0.0");
            mFreeSpace.setText(String.format(getString(R.string.free_gb), df.format(gbFree)));
        }
        else
        {
            mFreeSpace.setText(String.format(getString(R.string.free_mb), mbFree));
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        mSubscriptionHelper.manage(
            ViewObservable
                .clicks(mChooseDir, false)
                .subscribe(new Action1<ImageButton>()
                {
                    @Override
                    public void call(ImageButton imageButton)
                    {
                        mDirectoryPicker.show(getFragmentManager(), "dirpicker");
                    }
                })
        );

        mSubscriptionHelper.manage(
            mDirectoryPicker
                .observeDirectorySelected()
                .subscribe(new Action1<String>()
                {
                    @Override
                    public void call(String path)
                    {
                        mSavePath = path;
                        updateDownloadTo();
                        mDirectoryPicker.updateInitialDirectory(path, mFirstTrackArtist);
                    }
                })
        );

        mSubscriptionHelper.manage(
            ViewObservable
                .clicks(mDownloadButton, false)
                .subscribe(
                    new Action1<Button>()
                    {
                        @Override
                        public void call(Button button)
                        {
                            BugSenseHandler.sendEvent("Download button clicked");

                            TrackModel[] values = mListState.values()
                                .toArray(new TrackModel[] { });

                            Arrays.sort(values, new Comparator<TrackModel>()
                            {
                                @Override
                                public int compare(TrackModel lhs, TrackModel rhs)
                                {
                                    return lhs.index - rhs.index;
                                }
                            });

                            for (TrackModel trackModel : mListState.values())
                            {
                                if (!trackModel.isChecked)
                                {
                                    continue;
                                }

                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(trackModel.mTrackDto.url
                                    .toString()));
                                String fileName = String.format("%s - %s.mp3", trackModel.mTrackDto.artist, trackModel.mTrackDto.title);
                                request.setDestinationUri(Uri.withAppendedPath(Uri.parse("file://" + mSavePath), fileName));
                                request.setTitle(fileName);
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

                                mDownloadManager.enqueue(request);
                            }

                            Toast.makeText(getActivity(), getString(R.string.download_started), Toast.LENGTH_LONG).show();

                            getActivity().finish();
                        }
                    },
                    new Action1<Throwable>()
                    {
                        @Override
                        public void call(Throwable throwable)
                        {
                        }
                    })
        );
    }

    @Override
    public void onPause()
    {
        super.onPause();

        mSubscriptionHelper.unsubscribe();
    }

    private void fillList()
    {
        final LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Observable
            .from(mListState.values())
            .toSortedList(new Func2<TrackModel, TrackModel, Integer>()
            {
                @Override
                public Integer call(TrackModel trackModel1, TrackModel trackModel2)
                {
                    return trackModel1.index - trackModel2.index;
                }
            })
            .toBlocking()
            .forEach(new Action1<List<TrackModel>>()
            {
                @Override
                public void call(List<TrackModel> trackModels)
                {
                    for (final TrackModel trackModel :
                        trackModels.toArray(new TrackModel[] { }))
                    {
                        final String url = trackModel.mTrackDto.url.toString();

                        final CheckableRelativeLayout view = (CheckableRelativeLayout)inflater.inflate(R.layout.songs_list_item, mTrackList, false);
                        ((TextView)view.findViewById(R.id.song_list_item_title)).setText(trackModel.mTrackDto.title);
                        ((TextView)view.findViewById(R.id.song_list_item_artist)).setText(trackModel.mTrackDto.artist);

                        final CheckBox checkBox = (CheckBox)view.findViewById(R.id.song_list_item_is_selected);
                        checkBox.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                view.toggle();
                                checkBox.toggle();
                                setTrackChecked(url, view.isChecked());
                            }
                        });

                        setTrackChecked(url, trackModel.isChecked);

                        view.setChecked(trackModel.isChecked);
                        view.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                view.toggle();
                                setTrackChecked(url, view.isChecked());
                            }
                        });

                        mTrackList.addView(view);
                    }
                }
            });
    }

    private void setTrackChecked(String url, boolean isChecked)
    {
        mListState.get(url).isChecked = isChecked;
        updateDownloadButtonTitle();
    }

    private void updateDownloadButtonTitle()
    {
        int number = Observable
            .from(mListState.values())
            .filter(new Func1<TrackModel, Boolean>()
            {
                @Override
                public Boolean call(TrackModel trackModel)
                {
                    return trackModel.isChecked;
                }
            })
            .count()
            .toBlocking()
            .first();

        mDownloadButton.setText(String.format(getString(R.string.download_tracks), StringHelpers.declOfNum(number, new String[] {
            getString(R.string.track1), getString(R.string.track3), getString(R.string.track5)
        })));
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

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(BUNDLE_KEY_STATE, mListState);
        outState.putString(BUNDLE_KEY_TITLE, mPostTitle);
        outState.putString(BUNDLE_KEY_SAVE_PATH, mSavePath);
        outState.putString(BUNDLE_KEY_FIRST_TRACK_ARTIST, mFirstTrackArtist);
        outState.putBoolean(BUNDLE_KEY_SETTINGS_EXPANDED, mExpand.isActivated());
    }
}

