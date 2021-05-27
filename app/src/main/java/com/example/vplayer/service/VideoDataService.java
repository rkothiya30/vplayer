package com.example.vplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.FolderInfo;
import com.example.vplayer.model.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static com.example.vplayer.fragment.utils.Constant.CHANNEL_ID;
import static com.example.vplayer.fragment.utils.VideoPlayerUtils.getVideoFrameRate;


public class VideoDataService extends Service {

    public static boolean isImageComplate = false;
    public static int videoCount = 0;
    //public static LinkedHashMap<String, ArrayList<Video>> videoDataHashMap = new LinkedHashMap<>();
    public static LinkedHashMap<String, ArrayList<Video>> videobucketimagesDataHashMap = new LinkedHashMap<>();
    public static ArrayList<Video> videoList = new ArrayList();
    public static ArrayList<FolderInfo> videobuckets = new ArrayList<>();
    static List<String> folderNameList = new ArrayList<>();
    public static boolean isParent = false;
    ArrayList<String> listkeys = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager
                    mNotificationManager =
                    (NotificationManager) this
                            .getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (mChannel != null) {
                mNotificationManager.deleteNotificationChannel(CHANNEL_ID);
                createChannel();
            } else {
                createChannel();
            }

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
            stopForeground(STOP_FOREGROUND_REMOVE);
        }*/

        isImageComplate = false;
        //videoDataHashMap = new LinkedHashMap<>();
        videoList = new ArrayList();

        new AsyncTaskRunner().execute();

        return super.onStartCommand(intent, flags, startId);
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            videobuckets = new ArrayList();
            videobucketimagesDataHashMap = new LinkedHashMap<>();
            //videoDataHashMap = new LinkedHashMap<>();
            videoList = new ArrayList();
        }

        @Override
        protected String doInBackground(String... params) {
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection1 = {"_id", MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATE_TAKEN, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, "duration", MediaStore.Video.Media.SIZE, MediaStore.Video.Media.RESOLUTION};

            Cursor cursor = getContentResolver().query(uri, projection1, null, null, MediaStore.Video.VideoColumns.DATE_TAKEN + " DESC");

            if (cursor != null) {
                File file;
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    String path = cursor.getString(cursor.getColumnIndex(projection1[1]));
                    String title = cursor.getString(cursor.getColumnIndex(projection1[2]));
                    String date = cursor.getString(cursor.getColumnIndex(projection1[3]));
                    String bucketName = cursor.getString(cursor.getColumnIndex(projection1[4]));
                    int duration = cursor.getInt(5);
                    long size = cursor.getLong(6);
                    String resolution = cursor.getString(7);
                    //int frameRate = getVideoFrameRate(path);
                    file = new File(path);
                    Video video = new Video();
                    video.setFullPath(path);

                    if (file.exists()) {

                        video = new Video();
                        video.setId(id);
                        video.setFullPath(path);
                        video.setTitle(title);
                        video.setLayoutType(2);
                        if (date != null && !TextUtils.isEmpty(date)) {
                            video.setModifiyDate(date);
                        } else {
                            video.setModifiyDate("0");
                        }
                        video.setVideoDuration(duration);
                        video.setSize(size);
                        if (resolution != null && !TextUtils.isEmpty(resolution)) {
                            video.setResolution(resolution);
                        } else {
                            video.setResolution("0");
                        }
                        //video.setFrameRate(frameRate);

                        File folderFile = new File(VideoPlayerUtils.getParentPath(path));
                        getFolderName(folderFile);

                        videoList.add(video);

                        if (videobucketimagesDataHashMap.containsKey(bucketName)) {

                            ArrayList<Video> imagesData1 = videobucketimagesDataHashMap.get(bucketName);
                            imagesData1.add(video);
                            videobucketimagesDataHashMap.put(bucketName, imagesData1);

                        } else {

                            ArrayList<Video> imagesData1 = new ArrayList<>();
                            imagesData1.add(video);
                            videobucketimagesDataHashMap.put(bucketName, imagesData1);

                        }
                    }
                }

                cursor.close();
            }
            Set<String> keys = videobucketimagesDataHashMap.keySet();
            listkeys = new ArrayList<>();
            listkeys.addAll(keys);

            for (int i = 0; i < listkeys.size(); i++) {
                isParent = false;
                folderNameList = new ArrayList<>();

                ArrayList<Video> imagesData = videobucketimagesDataHashMap.get(listkeys.get(i));

                File folderFile = new File(imagesData.get(0).getFullPath());
                getFolderName(folderFile);

                videoCount = videoCount + imagesData.size();
                videobuckets.add(new FolderInfo(listkeys.get(i), VideoPlayerUtils.getParentPath(imagesData.get(0).getFullPath()), imagesData.size(), 2, isParent, folderNameList));
            }
            return "";
        }


        @Override
        protected void onPostExecute(String result) {
            isImageComplate = true;


        }

    }

    public static void getFolderName(File file) {
        if (file.getParent() != null) {
            if (!file.getParent().equals("/storage/emulated/0")) {
                folderNameList.add(file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1));
                file = new File(file.getParent());
                getFolderName(file);
                isParent = true;
            } else {
                isParent = false;
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = "VideoPlayer Service";
        // The user-visible description of the channel.
        String description = "VideoPlayer";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setSound(null, null);
        mChannel.setShowBadge(false);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(mChannel);
    }


}
