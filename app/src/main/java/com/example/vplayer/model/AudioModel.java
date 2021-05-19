package com.example.vplayer.model;

import android.graphics.Bitmap;

import org.parceler.Parcel;

@Parcel
public class AudioModel {

    String name;
    String album;
    String artist;
    String path;
    boolean isPlay;
    boolean isSelected;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    String Id;
    String Duration;

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }



    boolean isCheckboxVisible = false;
    Bitmap bitmap;
    boolean isFavorite = false;

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isCheckboxVisible() {
        return isCheckboxVisible;
    }

    public void setCheckboxVisible(boolean checkboxVisible) {
        isCheckboxVisible = checkboxVisible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
