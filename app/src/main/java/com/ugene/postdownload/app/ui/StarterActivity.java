package com.ugene.postdownload.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import com.ugene.postdownload.app.R;
import com.ugene.postdownload.app.core.ClipboardMonitorService;

public class StarterActivity extends FragmentActivity
{
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.starter);

        if (Debug.isDebuggerConnected())
        {
            mServiceIntent = new Intent(this, ClipboardMonitorService.class);
            startService(mServiceIntent);

//            finish();
        }
    }
}

