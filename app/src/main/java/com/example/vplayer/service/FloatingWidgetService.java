package com.example.vplayer.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.VideoPlayerActivity;


public class FloatingWidgetService extends Service {

    WindowManager windowManager;
    View floatingView;
    WindowManager.LayoutParams params;
    VideoView videoView;
    ImageView playPause, closeWindow, floatingWindow;

    PreferencesUtility preferencesUtility;
    Video video;

    public FloatingWidgetService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null);

        preferencesUtility = PreferencesUtility.getInstance(this);
        video = preferencesUtility.getLastPlayVideos();

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        windowManager.addView(floatingView, params);

        playPause = floatingView.findViewById(R.id.playPause);
        closeWindow = floatingView.findViewById(R.id.closeWindow);
        floatingWindow = floatingView.findViewById(R.id.floatingWindow);

        videoView = floatingView.findViewById(R.id.videoView);
        if (video != null) {
            videoView.setVideoPath(video.getFullPath());
            videoView.seekTo(video.getVideoLastPlayPosition());
            videoView.start();
        }

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                preferencesUtility.setIsFloatingVideo(false);
                stopSelf();
            }
        });

        closeWindow.setImageResource(R.drawable.ic_close_white_24dp);
        closeWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferencesUtility.setIsFloatingVideo(false);
                stopSelf();
            }
        });

        playPause.setImageResource(R.drawable.hplib_ic_pause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playPause.setImageResource(R.drawable.hplib_ic_play_download);
                } else {
                    videoView.start();
                    playPause.setImageResource(R.drawable.hplib_ic_pause);
                }
            }
        });

        floatingWindow.setImageResource(R.drawable.floating_window);
        floatingWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferencesUtility.setIsFloatingVideo(false);
                startActivity(VideoPlayerActivity.getInstance(getApplicationContext(), videoView.getCurrentPosition(), true).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                stopSelf();
            }
        });

        floatingView.findViewById(R.id.mainParentRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
            int X_Axis, Y_Axis;
            float TouchX, TouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        X_Axis = params.x;
                        Y_Axis = params.y;
                        TouchX = event.getRawX();
                        TouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        params.x = X_Axis + (int) (event.getRawX() - TouchX);
                        params.y = Y_Axis + (int) (event.getRawY() - TouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
    }

}
