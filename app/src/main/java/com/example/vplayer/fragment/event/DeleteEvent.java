package com.example.vplayer.fragment.event;

import java.io.File;

public class DeleteEvent {
    long id;

    public DeleteEvent(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
