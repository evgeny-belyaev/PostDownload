package com.ugene.postdownload.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.ugene.postdownload.app.R;
import com.ugene.postdownload.app.core.PostDto;
import com.ugene.postdownload.app.lib.FragmentHelper;
import rx.functions.Func0;

public class MainActivity extends FragmentActivity
{
    public static int REQUEST_CODE_OPEN_ACTIVITY = 0;
    public static String INTENT_EXTRA_POST_DTO = "postDto";

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

    }

    public static PostDto getPost(Intent intent)
    {
        return (PostDto)intent.getSerializableExtra(INTENT_EXTRA_POST_DTO);
    }

}

