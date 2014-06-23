package com.ugene.postdownload.app;

import com.ugene.postdownload.app.core.TrackDto;

import java.io.Serializable;

public class TrackModel implements Serializable
{
    public int progress;
    public boolean isChecked;
    public TrackDto mTrackDto;
    public int index;
}
