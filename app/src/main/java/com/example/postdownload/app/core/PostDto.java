package com.example.postdownload.app.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PostDto implements Serializable
{
    public String title;
    public String body;

    public List<SongDto> songs = new ArrayList<SongDto>();
}