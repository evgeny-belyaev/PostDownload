package com.ugene.postdownload.app2.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PostDto implements Serializable
{
    public String title;
    public String body;

    public List<TrackDto> songs = new ArrayList<>();
}
