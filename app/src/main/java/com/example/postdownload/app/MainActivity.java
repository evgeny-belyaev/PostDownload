package com.example.postdownload.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.net.URL;

public class MainActivity extends Activity
{
    public static int REQUEST_CODE_OPEN_ACTIVITY = 0;
    public static String INTENT_EXTRA_POST_DTO = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Intent intent = getIntent();
        URL url = (URL)intent.getSerializableExtra(INTENT_EXTRA_POST_DTO);

        TextView tw = (TextView)findViewById(android.R.id.text1);
        tw.setText(url.toString());
    }
}

