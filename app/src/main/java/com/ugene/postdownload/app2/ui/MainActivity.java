package com.ugene.postdownload.app2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.bugsense.trace.BugSenseHandler;
import com.ugene.postdownload.app2.R;
import com.ugene.postdownload.app2.core.FragmentHelper;
import com.ugene.postdownload.app2.core.PostDto;
import rx.functions.Func0;

public class MainActivity extends FragmentActivity
{
    public static final int REQUEST_CODE_DOWNLOAD_ALL = 1;
    public static int REQUEST_CODE_OPEN_ACTIVITY = 0;
    public static String INTENT_EXTRA_POST_DTO = "postDto";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, getString(R.string.bugsence_api_key));

        setContentView(R.layout.main);

        FragmentHelper.replaceOrRestore(getSupportFragmentManager(), R.id.outer, "trackListFragment", new Func0<Fragment>()
        {
            @Override
            public Fragment call()
            {
                return new TrackListFragment();
            }
        });
    }

    public static PostDto getPost(Intent intent)
    {
        return (PostDto)intent.getSerializableExtra(INTENT_EXTRA_POST_DTO);
    }

}

