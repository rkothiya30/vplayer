package com.example.vplayer.model;

import java.util.Comparator;
import java.util.List;

public class FolderInfo  {

    public final String folderName;
    public final String folderPath;
    public final int videoCount;
    public int layoutType;
    public boolean isParent;
    List<String> folderList;
    public int folderCount;
    List<ParentFolder> multiFolderList;

    public FolderInfo() {
        this.folderName = "";
        this.folderPath = "";
        this.videoCount = -1;
        this.layoutType = 2;
    }

    public FolderInfo(String folderName, String folderPath, int videoCount, int layoutType, boolean isParent, List<String> folderList) {
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.videoCount = videoCount;
        this.layoutType = layoutType;
        this.isParent = isParent;
        this.folderList = folderList;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFolderPath() {
        return folderPath;
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

    public List<ParentFolder> getMultiFolderList() {
        return multiFolderList;
    }

    public void setMultiFolderList(List<ParentFolder> multiFolderList) {
        this.multiFolderList = multiFolderList;
    }

    /*@Override
    public int compare(hdvideo.player.videoplayer.mvp.model.FolderInfo folderInfo, hdvideo.player.videoplayer.mvp.model.FolderInfo folderInfo1) {
        return folderInfo.folderName.compareTo(folderInfo1.folderName);
    }*/

    @Override
    public boolean equals(Object o) {
        return false;
    }
}
