package com.example.postdownload.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.example.postdownload.app.core.ClipboardMonitorService;
import com.example.postdownload.app.lib.StorageUtils;

import java.io.*;
import java.util.List;

public class StarterActivity extends Activity
{
    private static final String TAG = "StarterActivity";
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mServiceIntent = new Intent(this, ClipboardMonitorService.class);
        startService(mServiceIntent);

        List<StorageUtils.StorageInfo> l = StorageUtils.getStorageList();
        finish();
    }

    private void test()
    {
        final String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            final File primaryExternalStorage = Environment.getExternalStorageDirectory();

            final String externalStorageRootDir;
            if ((externalStorageRootDir = primaryExternalStorage.getParent()) == null)
            {
                Log.d(TAG, "External Storage: " + primaryExternalStorage + "\n");
            }
            else
            {
                final File externalStorageRoot = new File(externalStorageRootDir);
                final File[] files = externalStorageRoot.listFiles();

                for (final File file : files)
                {
                    if (file.isDirectory() && file.canRead() && (file.listFiles().length > 0))
                    {  // it is a real directory (not a USB drive)...
                        Log.d(TAG, "External Storage: " + file.getAbsolutePath() + "\n");
                    }
                }
            }
        }
    }
}

