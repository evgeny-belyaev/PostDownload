package com.example.postdownload.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.example.postdownload.app.core.ClipboardMonitorService;

public class StarterActivity extends FragmentActivity
{
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mServiceIntent = new Intent(this, ClipboardMonitorService.class);
        startService(mServiceIntent);

        finish();
    }
}

