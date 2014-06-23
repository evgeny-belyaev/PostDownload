package com.ugene.postdownload.app.core;

import java.io.Serializable;
import java.net.URL;

public class TrackDto implements Serializable
{
    public URL url;
    public String title;
    public String artist;
    public String duration;
}
