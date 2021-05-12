package com.example.vplayer.model;

import org.parceler.Parcel;

import java.util.Comparator;

@Parcel
public class Video{

    long id;
    String title;
    String fullPath;
    long lastModified;
    long dateTaken;
    long size;
    int type;
    int videoDuration;
    String modifiyDate = "0";
    String resolution;
    public int layoutType;
    public int videoLastPlayPosition = 0;
    int frameRate;

    public Video() {
    }

    public Video(long id, String _title, int _duration, long size, String _path, String modifiyDate, String resolution, int frameRate, int layoutType) {
        this.id = id;
        this.title = _title;
        this.videoDuration = _duration;
        this.size = size;
        this.fullPath = _path;
        this.modifiyDate = modifiyDate;
        this.resolution = resolution;
        this.layoutType = layoutType;
        this.frameRate = frameRate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(int videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getModifiyDate() {
        return modifiyDate;
    }

    public void setModifiyDate(String modifiyDate) {
        this.modifiyDate = modifiyDate;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(int layoutType) {
        this.layoutType = layoutType;
    }

    public int getVideoLastPlayPosition() {
        return videoLastPlayPosition;
    }

    public void setVideoLastPlayPosition(int videoLastPlayPosition) {
        this.videoLastPlayPosition = videoLastPlayPosition;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    /*@Override
    public int compare(hdvideo.player.videoplayer.mvp.model.Video video, hdvideo.player.videoplayer.mvp.model.Video t1) {
        return Long.compare(video.getDateTaken(), t1.getDateTaken());
    }*/
}
