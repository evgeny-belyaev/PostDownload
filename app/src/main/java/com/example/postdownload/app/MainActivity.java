package com.example.postdownload.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
    private Button mStartService;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartService = (Button)findViewById(R.id.start_service);
        mServiceIntent = new Intent(this, ClipboardMonitorService.class);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mStartService.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startService(mServiceIntent);
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        stopService(mServiceIntent);
    }
}

