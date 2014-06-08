package com.example.postdownload.app;

import com.example.postdownload.app.core.TrackDto;

import java.io.Serializable;

public class TrackModel implements Serializable
{
    public int progress;
    public boolean isChecked;
    public TrackDto mTrackDto;
    public int index;
}
