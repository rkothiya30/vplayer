package com.example.vplayer.model;

import java.util.List;

public class PlayListModel {

    List<AudioModel> AudioList;
    List<Video> videoList;

    public PlayListModel() {
    }

    public List<AudioModel> getAudioList() {
        return AudioList;
    }

    public void setAudioList(List<AudioModel> audioList) {
        AudioList = audioList;
    }



    public PlayListModel(List<AudioModel> audioList, List<Video> videoList) {
        this.videoList = videoList;
        this.AudioList = audioList;
    }

    public List<Video> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
    }
}
