package com.example.postdownload.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity
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

