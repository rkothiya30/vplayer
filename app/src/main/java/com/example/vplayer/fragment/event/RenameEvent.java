package com.example.vplayer.fragment.event;

import java.io.File;

public class RenameEvent {
    File oldFile, newFile;

    public RenameEvent(File oldFile, File newFile) {
        this.oldFile = oldFile;
        this.newFile = newFile;
    }

    public File getOldFile() {
        return oldFile;
    }

    public void setOldFile(File oldFile) {
        this.oldFile = oldFile;
    }

    public File getNewFile() {
        return newFile;
    }

    public void setNewFile(File newFile) {
        this.newFile = newFile;
    }
}
