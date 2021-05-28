package com.example.vplayer.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.vplayer.model.AudioModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;

import rx.Observable;
import rx.schedulers.Schedulers;

public class MusicDataService extends Service {

    public static boolean isComplete = false;
    public static ArrayList<AudioModel> audioList = new ArrayList<>();

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



        isComplete = false;

        Observable.fromCallable(() -> {
           // Log.e("ImageGet", "com.gallery.picture.foto.service photo getting start....");
           // Log.d("servicetime", String.valueOf(Calendar.getInstance().getTime()));
            audioList.clear();

            getAllAudioList();
            //getImages();
            return true;
        }).subscribeOn(Schedulers.io())
                .doOnError(throwable -> {
                    isComplete = true;
                    Intent intent1 = new Intent("LoardDataComplete");
                    intent1.putExtra("completed", true);
                    sendBroadcast(intent1);
                })
                .subscribe((result) -> {
                    isComplete = true;
                    Intent intent1 = new Intent("LoardDataComplete");
                    intent1.putExtra("completed", true);
                    //Log.e("ImageGet", "com.gallery.picture.foto.service photo set list....");
                    sendBroadcast(intent1);
                });

        return super.onStartCommand(intent, flags, startId);
    }

    private void getAllAudioList() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID};
        Cursor c = getContentResolver().query(uri, projection, null, null,
                "LOWER(" + MediaStore.Audio.Media.DATE_MODIFIED + ") DESC");


        if (c != null) {
            /*ArrayList<String> favList = PreferencesManager.getFavouriteList(this);
            if (favList == null) {
                favList = new ArrayList<>();
            }*/
            while (c.moveToNext()) {

                AudioModel audioModel = new AudioModel();
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);
                Long albumId = c.getLong(3);
                String id = c.getString(4);



                File file = new File(path);



                String name = path.substring(path.lastIndexOf("/") + 1);

                if (file.exists()) {
                    audioModel.setName(name);
                    audioModel.setAlbum(album);
                    audioModel.setArtist(artist);
                    audioModel.setPath(path);
                    //audioModel.setDuration(duration);
                    audioModel.setAlbumId(albumId);
                    /*if (favList.contains(path)) {
                        audioModel.setFavorite(true);
                    } else {
                        audioModel.setFavorite(false);
                    }*/
                    audioModel.setId(id);
                    audioModel.setPlay(false);
                    audioModel.setSelected(false);
                    audioModel.setCheckboxVisible(false);
                    //audioModel.setBitmap(bitmap);

                    audioList.add(audioModel);
                }
            }
            c.close();
        }



    }

}
