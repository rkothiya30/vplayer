package com.example.vplayer.model;


import java.util.List;

public class ParentFolder {

    public final String folderName;
    public final String folderPath;
    public final int videoCount;
    public int layoutType;
    public boolean isParent;
    List<String> folderList;
    public int folderCount;

    public ParentFolder() {
        this.folderName = "";
        this.folderPath = "";
        this.videoCount = -1;
        this.layoutType = 2;
    }

    public ParentFolder(String folderName, String folderPath, int videoCount, int layoutType, boolean isParent, List<String> folderList) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.videoCount = videoCount;
        this.layoutType = layoutType;
        this.isParent = isParent;
        this.folderList = folderList;
    }

    public List<String> getFolderList() {
        return folderList;
    }

    public void setFolderList(List<String> folderList) {
        this.folderList = folderList;
    }

    public int getFolderCount() {
        return folderCount;
    }

    public void setFolderCount(int folderCount) {
        this.folderCount = folderCount;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
