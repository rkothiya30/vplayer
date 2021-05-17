package com.example.vplayer.fragment.event;

public class UpdateVideoList {

   int position;

    public UpdateVideoList(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
