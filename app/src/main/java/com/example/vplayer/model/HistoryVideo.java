package com.example.vplayer.model;

import java.util.List;

public class HistoryVideo {

    List<Video> videoList;

    public HistoryVideo(List<Video> videoList) {
        this.videoList = videoList;
    }

    public List<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
    }
}
