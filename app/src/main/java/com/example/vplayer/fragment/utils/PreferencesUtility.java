package com.example.vplayer.fragment.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.vplayer.model.FolderInfo;
import com.example.vplayer.model.HashMapModel;
import com.example.vplayer.model.HistoryVideo;
import com.example.vplayer.model.PlayListModel;
import com.example.vplayer.model.Video;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.vplayer.fragment.utils.Constant.PREF_CONTINUE_WATCHING_VIDEO;
import static com.example.vplayer.fragment.utils.Constant.PREF_FLOATING_VIDEO_POSITION;
import static com.example.vplayer.fragment.utils.Constant.PREF_FOLDER;
import static com.example.vplayer.fragment.utils.Constant.PREF_HIDE_VIDEO;
import static com.example.vplayer.fragment.utils.Constant.PREF_UNHIDE_VIDEO;
import static com.example.vplayer.fragment.utils.Constant.PREF_VIDEO_LIST;
import static com.example.vplayer.fragment.utils.Constant.PROTECTED_HASH;
import static com.example.vplayer.fragment.utils.Constant.PROTECTED_TYPE;
import static com.example.vplayer.fragment.utils.Constant.PROTECTION_NONE;
import static com.example.vplayer.fragment.utils.Constant.SECURITY_ANSWER;
import static com.example.vplayer.fragment.utils.Constant.SECURITY_QUESTION;

public class PreferencesUtility {

    public static final String PREF_HISTORY_VIDEOS = "history_videos";
    public static final String PREF_LAST_PLAY_VIDEOS = "last_play_videos";
    public static final String SHARED_PREFS_DIR_LIST_GRID = "grid_view";
    public static final String PREF_SDCARD_TREE_URI = "sdcard_tree_uri";
    public static  final String PLAYLISTS = "all_playlist";

    private static PreferencesUtility sInstance;
    private static volatile SharedPreferences mPreferences;

    public PreferencesUtility(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferencesUtility getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (PreferencesUtility.class) {
                if (sInstance == null) {
                    sInstance = new PreferencesUtility(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    public static SharedPreferences getmPreferences() {
        return mPreferences;
    }

    public void setOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void setHistoryVideos(Video video) {
        List<Video> videoList = new ArrayList<>();
        if (video != null) {
            if (getHistoryVideos() != null) {
                videoList.addAll(getHistoryVideos());
                if (videoList.size() != 0) {
                    for (int i = 0; i < videoList.size(); i++) {
                        if (videoList.get(i).getFullPath().equals(video.getFullPath())) {
                            videoList.remove(i);
                        }
                    }
                }
            }
            videoList.add(0, video);
        }
        final SharedPreferences.Editor editor = mPreferences.edit();
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        editor.putString(PREF_HISTORY_VIDEOS, new Gson().toJson(historyVideo));
        editor.apply();
    }

    public void setPlaylists(LinkedHashMap<String, String> a) {
        List<PlayListModel> videoList = new ArrayList<>();



        final SharedPreferences.Editor editor = mPreferences.edit();
        HashMapModel hashMapModel = new HashMapModel(a);
        editor.putString(PLAYLISTS, new Gson().toJson(hashMapModel));
        editor.apply();
    }

    public LinkedHashMap<String, String> getPlaylists(){
        String playlists = mPreferences.getString(PLAYLISTS, "");
        HashMapModel hashMapModel = new HashMapModel();
        if(playlists != null){
            hashMapModel = new Gson().fromJson(playlists, HashMapModel.class);
            if(hashMapModel == null){
                hashMapModel = new HashMapModel();
                hashMapModel.setA(new LinkedHashMap<>());
            }
        }

        return hashMapModel.getA();
    }

    public void updateHistoryVideo(List<Video> videoList) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        editor.putString(PREF_HISTORY_VIDEOS, new Gson().toJson(historyVideo));
        editor.apply();
    }

    public List<Video> getHistoryVideos() {
        List<Video> videoList = new ArrayList<>();
        String videos = mPreferences.getString(PREF_HISTORY_VIDEOS, "");
        HistoryVideo historyVideo;
        if (videos != null) {
            historyVideo = new Gson().fromJson(videos, HistoryVideo.class);
            if (historyVideo != null)
                videoList.addAll(historyVideo.getVideoList());
        }
        return videoList;
    }

    public static String getSDCardTreeUri(Context context) {

        return mPreferences.getString(PREF_SDCARD_TREE_URI, "");
    }

    public static void setSDCardTreeUri(Context context, String treeUri) {


        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_SDCARD_TREE_URI, treeUri);
        editor.apply();
    }

    public  void saveToDirList_Grid( boolean value) {

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SHARED_PREFS_DIR_LIST_GRID, value);
        editor.commit();

    }

    public  boolean getDirList_Grid() {
        return mPreferences.getBoolean(SHARED_PREFS_DIR_LIST_GRID, false);
    }

    public void setAutoPlayVideo(boolean isShow) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constant.PREF_AUTO_PLAY, isShow);
        editor.apply();
    }

    public boolean isAutoPlayVideo() {
        return mPreferences.getBoolean(Constant.PREF_AUTO_PLAY, true);
    }

    public void setResumeVideo(boolean isResume) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constant.PREF_RESUME_VIDEO, isResume);
        editor.apply();
    }

    public boolean isResumeVideo() {
        return mPreferences.getBoolean(Constant.PREF_RESUME_VIDEO, false);
    }

    public void setShowResumeStatus(boolean isShow) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constant.PREF_RESUME_STATUS, isShow);
        editor.apply();
    }

    public boolean isShowResumeStatus() {
        return mPreferences.getBoolean(Constant.PREF_RESUME_STATUS, false);
    }

    public void setVideoLastPosition(HashMap<String, Integer> videoLastPositionList) {
        HashMap<String, Integer> videoLastPositionList1 = new HashMap<>();
        if (videoLastPositionList != null) {
            String lastPositionList = mPreferences.getString(Constant.PREF_VIDEO_LAST_POSITION, "");
            if (lastPositionList != null) {
                videoLastPositionList1 = new Gson().fromJson(lastPositionList, HashMap.class);
            }
            if (videoLastPositionList1 != null) {
                videoLastPositionList1.putAll(videoLastPositionList);
            } else {
                videoLastPositionList1 = new HashMap<>();
                videoLastPositionList1.putAll(videoLastPositionList);
            }
        }


        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(Constant.PREF_VIDEO_LAST_POSITION, new Gson().toJson(videoLastPositionList1));
        editor.apply();
    }

    public HashMap<String, Integer> getVideoLastPosition() {
        HashMap<String, Integer> videoLastPositionList = new HashMap<>();

        String lastPositionList = mPreferences.getString(Constant.PREF_VIDEO_LAST_POSITION, "");
        if (lastPositionList != null) {
            videoLastPositionList = new Gson().fromJson(lastPositionList, HashMap.class);
        }
        return videoLastPositionList;
    }

    public void setLastPlayVideos(Video video) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_LAST_PLAY_VIDEOS, new Gson().toJson(video));
        editor.apply();
    }

    public Video getLastPlayVideos() {
        String videos = mPreferences.getString(PREF_LAST_PLAY_VIDEOS, "");
        Video video = null;
        if (videos != null) {
            video = new Gson().fromJson(videos, Video.class);
        }
        return video;
    }

    public void setFloatingVideoPosition(int position) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(PREF_FLOATING_VIDEO_POSITION, position);
        editor.apply();
    }

    public int getFloatingVideoPosition() {
        int position = mPreferences.getInt(PREF_FLOATING_VIDEO_POSITION, 0);
        return position;
    }

    public void setVideoList(List<Video> videoList) {
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(PREF_VIDEO_LIST);
        editor.commit();
        editor.putString(PREF_VIDEO_LIST, new Gson().toJson(historyVideo));
        editor.apply();
    }

    public List<Video> getVideoList() {
        List<Video> videoList = new ArrayList<>();
        String videos = mPreferences.getString(PREF_VIDEO_LIST, "");
        HistoryVideo historyVideo;
        if (videos != null) {
            historyVideo = new Gson().fromJson(videos, HistoryVideo.class);
            if (historyVideo != null)
                videoList.addAll(historyVideo.getVideoList());
        }
        return videoList;
    }

    public void setIsFloatingVideo(boolean isFloatingVideo) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(Constant.PREF_IS_FLOATING_VIDEO, isFloatingVideo);
        editor.apply();
    }

    public boolean isFloatingVideo() {
        return mPreferences.getBoolean(Constant.PREF_IS_FLOATING_VIDEO, false);
    }

    public void setSortByVideo(int sortBy) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(Constant.PREF_SORT_BY_VIDEO, sortBy);
        editor.apply();
    }

    public int getSortBy() {
        return mPreferences.getInt(Constant.PREF_SORT_BY_VIDEO, 8);
    }

    public void setFolder(FolderInfo folderList) {
        Gson gson = new Gson();
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PREF_FOLDER, gson.toJson(folderList));
        editor.apply();
    }

    public FolderInfo getFolder() {
        Gson gson = new Gson();
        String folders = mPreferences.getString(PREF_FOLDER, "");
        FolderInfo folderInfo = null;
        if (folders != null) {
            folderInfo = gson.fromJson(folders, FolderInfo.class);
        }
        return folderInfo;
    }

    public void addProtectionMode(String hash, int type) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PROTECTED_HASH, hash);
        editor.putInt(PROTECTED_TYPE, type);
        editor.apply();

    }

    public String getProtectionHash() {
        String hash = mPreferences.getString(PROTECTED_HASH, "");
        return hash;
    }

    public int getProtectionType() {
        int type = mPreferences.getInt(PROTECTED_TYPE, PROTECTION_NONE);
        return type;
    }

    public boolean isProtected() {
        return getProtectionType() != PROTECTION_NONE;
    }

    public void setSecurityQuestion(int question) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SECURITY_QUESTION, question);
        editor.apply();
    }

    public int getSecurityQuestion() {
        int question = mPreferences.getInt(SECURITY_QUESTION, 0);
        return question;
    }

    public void setSecurityAnswer(String answer) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SECURITY_ANSWER, answer);
        editor.apply();
    }

    public String getSecurityAnswer() {
        String answer = mPreferences.getString(SECURITY_ANSWER, "");
        return answer;
    }

    public void setHideVideos(Video video) {
        List<Video> videoList = new ArrayList<>();
        if (video != null) {
            if (getHideVideos() != null) {
                videoList.addAll(getHideVideos());
            }
            videoList.add(0, video);
        }
        final SharedPreferences.Editor editor = mPreferences.edit();
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        editor.putString(PREF_HIDE_VIDEO, new Gson().toJson(historyVideo));
        editor.apply();
    }

    public List<Video> getHideVideos() {
        List<Video> videoList = new ArrayList<>();
        String videos = mPreferences.getString(PREF_HIDE_VIDEO, "");
        HistoryVideo historyVideo;
        if (videos != null) {
            historyVideo = new Gson().fromJson(videos, HistoryVideo.class);
            if (historyVideo != null)
                videoList.addAll(historyVideo.getVideoList());
        }
        return videoList;
    }

    public void updateHideVideos(Video video) {
        List<Video> videoList = new ArrayList<>();
        if (video != null) {
            if (getHideVideos() != null) {
                videoList.addAll(getHideVideos());
                for (int i = 0; i < videoList.size(); i++) {
                    if (videoList.get(i).getTitle().equals(video.getTitle())) {
                        videoList.remove(i);
                        break;
                    }
                }
            }
            final SharedPreferences.Editor editor = mPreferences.edit();
            HistoryVideo historyVideo = new HistoryVideo(videoList);
            editor.putString(PREF_HIDE_VIDEO, new Gson().toJson(historyVideo));
            editor.apply();
        }
    }

    public void setUnhideVideos(Video video) {
        List<Video> videoList = new ArrayList<>();
        if (video != null) {
            if (getUnhideVideos() != null) {
                videoList.addAll(getUnhideVideos());
            }
            videoList.add(0, video);
        }
        final SharedPreferences.Editor editor = mPreferences.edit();
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        editor.putString(PREF_UNHIDE_VIDEO, new Gson().toJson(historyVideo));
        editor.apply();
    }


    public List<Video> getUnhideVideos() {
        List<Video> videoList = new ArrayList<>();
        String videos = mPreferences.getString(PREF_UNHIDE_VIDEO, "");
        HistoryVideo historyVideo;
        if (videos != null) {
            historyVideo = new Gson().fromJson(videos, HistoryVideo.class);
            if (historyVideo != null)
                videoList.addAll(historyVideo.getVideoList());
        }
        return videoList;
    }

    public void setContinueWatchingVideos(Video video) {
        List<Video> videoList = new ArrayList<>();
        if (video != null) {
            if (getContinueWatchingVideos() != null) {
                videoList.addAll(getContinueWatchingVideos());
                if (videoList.size() != 0) {
                    for (int i = 0; i < videoList.size(); i++) {
                        if (videoList.get(i).getFullPath().equals(video.getFullPath())) {
                            videoList.remove(i);
                        }
                    }
                }
            }
            videoList.add(0, video);
        }
        final SharedPreferences.Editor editor = mPreferences.edit();
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        editor.putString(PREF_CONTINUE_WATCHING_VIDEO, new Gson().toJson(historyVideo));
        editor.apply();
    }

    public void updateContinueWatchingVideo(List<Video> videoList) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        HistoryVideo historyVideo = new HistoryVideo(videoList);
        editor.putString(PREF_CONTINUE_WATCHING_VIDEO, new Gson().toJson(historyVideo));
        editor.apply();
    }

    public List<Video> getContinueWatchingVideos() {
        List<Video> videoList = new ArrayList<>();
        String videos = mPreferences.getString(PREF_CONTINUE_WATCHING_VIDEO, "");
        HistoryVideo historyVideo;
        if (videos != null) {
            historyVideo = new Gson().fromJson(videos, HistoryVideo.class);
            if (historyVideo != null)
                videoList.addAll(historyVideo.getVideoList());
        }
        return videoList;
    }
}
