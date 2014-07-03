package com.ugene.postdownload.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebView;
import com.ugene.postdownload.app.R;
import com.ugene.postdownload.app.core.ClipboardMonitorService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class StarterActivity extends FragmentActivity
{
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.starter);

        WebView help = (WebView)findViewById(R.id.help_web_view);
        InputStream inputStream = getResources().openRawResource(R.raw.help);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("utf-8")));

        StringBuilder total = new StringBuilder();
        String line;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                total.append(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        help.loadDataWithBaseURL("", total.toString(), "text/html", "utf-8", "");

        if (Debug.isDebuggerConnected())
        {
            mServiceIntent = new Intent(this, ClipboardMonitorService.class);
            startService(mServiceIntent);

            //            finish();
        }
    }
}

