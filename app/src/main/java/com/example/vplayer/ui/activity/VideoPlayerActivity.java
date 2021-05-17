package com.example.vplayer.ui.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vplayer.R;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.event.HistoryEvent;
import com.example.vplayer.fragment.event.UpdateContinueWatchingEvent;
import com.example.vplayer.fragment.event.UpdateVideoStatusEvent;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.FloatingWidgetService;
import com.example.vplayer.service.VideoPlayAsAudioService;
import com.khizar1556.mkvideoplayer.MKPlayer;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.example.vplayer.fragment.utils.Constant.EXTRA_BACKGROUND_VIDEO_PLAY_POSITION;
import static com.example.vplayer.fragment.utils.Constant.EXTRA_FLOATING_VIDEO;
import static com.example.vplayer.fragment.utils.Constant.EXTRA_IS_CONTINUE_WATCHING_VIDEO;
import static com.example.vplayer.fragment.utils.Constant.EXTRA_IS_FLOATING_VIDEO;
import static com.example.vplayer.fragment.utils.Constant.EXTRA_VIDEO_POSITION;
import static com.example.vplayer.service.VideoPlayAsAudioService.NOTIFICATION_CLICK_ACTION;

public class VideoPlayerActivity extends AppCompatActivity {

    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;

    MKPlayer mkPlayer;
    List<Video> videosList;
    int videoPosition;
    boolean isResumeVideo = false;
    int videoLastProgress;
    boolean isAutoPlay = true;
    boolean isFloatingVideo = false;
    boolean isShuffleClick = false;
    boolean isContinueWatching = false;
    boolean isService = false;

    CountDownTimer sleepCountDownTimer;
    long sleepTimeMiliSecond;

    boolean isBackgroundEnable = false;
    HashMap<String, Integer> resumeVideoList = new HashMap<>();
    HashMap<String, Integer> videoPlayLastPositionList = new HashMap<>();

    AlertDialog.Builder builder;
    AlertDialog dialog, sleepDailog;

    TextView app_video_title;

    PreferencesUtility preferencesUtility;

    public static Intent getIntent(Context context,  List<Video> videoList, int position) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        /*List<Video> videoList = new ArrayList<>();
        videoList.add(video);*/
        intent.putExtra(Constant.EXTRA_VIDEO_LIST, Parcels.wrap(videoList));
        intent.putExtra(EXTRA_VIDEO_POSITION, position);
        return intent;
    }

    public static Intent getInstance(Context context, int videoLastProgress, boolean isFloatingVideo) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(EXTRA_FLOATING_VIDEO, videoLastProgress);
        intent.putExtra(EXTRA_IS_FLOATING_VIDEO, isFloatingVideo);
        return intent;
    }

    public static Intent getIntent(Context context, List<Video> videoList, int position, boolean isBackGroundService) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(Constant.EXTRA_VIDEO_LIST, Parcels.wrap(videoList));
        intent.putExtra(Constant.EXTRA_VIDEO_POSITION, position);
        intent.putExtra(EXTRA_BACKGROUND_VIDEO_PLAY_POSITION, isBackGroundService);
        return intent;
    }

    public static Intent getInstanceContinueWatching(Context context, List<Video> videoList, int position, boolean isContinueWatching) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(Constant.EXTRA_VIDEO_POSITION, position);
        intent.putExtra(Constant.EXTRA_VIDEO_LIST, Parcels.wrap(videoList));
        intent.putExtra(EXTRA_IS_CONTINUE_WATCHING_VIDEO, isContinueWatching);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getSupportActionBar().hide();

        app_video_title = findViewById(R.id.app_video_title);

        app_video_title.setTextSize(20);

        videosList = new ArrayList<>();
        preferencesUtility = PreferencesUtility.getInstance(this);
        isResumeVideo = preferencesUtility.isResumeVideo();
        isAutoPlay = preferencesUtility.isAutoPlayVideo();

        startService(new Intent(this, VideoPlayAsAudioService.class).setAction(NOTIFICATION_CLICK_ACTION));

        if (getIntent() != null) {
            videosList = Parcels.unwrap(getIntent().getParcelableExtra(Constant.EXTRA_VIDEO_LIST));
            videoPosition = getIntent().getIntExtra(EXTRA_VIDEO_POSITION, 0);
        }

        if (videosList == null) {
            videosList = preferencesUtility.getVideoList();
        }

        isFloatingVideo = getIntent().getBooleanExtra(EXTRA_IS_FLOATING_VIDEO, false);

        if (isFloatingVideo) {
            videoPosition = preferencesUtility.getFloatingVideoPosition();
            videoLastProgress = getIntent().getIntExtra(EXTRA_FLOATING_VIDEO, 0);
        }

        isContinueWatching = getIntent().getBooleanExtra(EXTRA_IS_CONTINUE_WATCHING_VIDEO, false);

        isService = getIntent().getBooleanExtra(EXTRA_BACKGROUND_VIDEO_PLAY_POSITION, false);
        if (isService) {
            videoLastProgress = preferencesUtility.getFloatingVideoPosition();
        }

        initView();
    }

    public void initView() {
        mkPlayer = new MKPlayer(this);

        setVideoResume();
        mkPlayer.onComplete(new Runnable() {
            @Override
            public void run() {
                isFloatingVideo = false;
                if (isAutoPlay) {
                    if (isShuffleClick) {
                        getRandomVideoPosition();
                    }
                    if (videoPosition != videosList.size()) {
                        videoPosition = videoPosition + 1;
                        if (videosList.size() > videoPosition) {
                            if (videosList.get(videoPosition).getLayoutType() == 1) {
                                videoPosition = videoPosition + 1;
                            }
                            if (videosList.size() > videoPosition) {
                                setVideoResume();
                            }
                        }
                    }
                    videoPlayLastPositionList.put(videosList.get(videoPosition).getFullPath(), mkPlayer.getCurrentPosition());
                    preferencesUtility.setLastPlayVideos(videosList.get(videoPosition));
                }
            }
        }).onInfo(new MKPlayer.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        //do something when buffering end
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        //download speed
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        //do something when video rendering
                        break;
                }
            }
        }).onError(new MKPlayer.OnErrorListener() {
            @Override
            public void onError(int what, int extra) {
                Toast.makeText(getApplicationContext(), "video play error", Toast.LENGTH_SHORT).show();
            }
        });


        mkPlayer.setPlayerCallbacks(new MKPlayer.playerCallbacks() {
            @Override
            public void onNextClick() {
                preferencesUtility.setContinueWatchingVideos(videosList.get(videoPosition));
                if (isShuffleClick) {
                    getRandomVideoPosition();
                }
                if (videoPosition != videosList.size()) {
                    videoPosition = videoPosition + 1;
                    if (videosList.get(videoPosition).getLayoutType() == 1) {
                        videoPosition = videoPosition + 1;
                    }
                    setVideoResume();
                }
                videoPlayLastPositionList.put(videosList.get(videoPosition).getFullPath(), mkPlayer.getCurrentPosition());
            }

            @Override
            public void onPreviousClick() {
                preferencesUtility.setContinueWatchingVideos(videosList.get(videoPosition));
                if (isShuffleClick) {
                    getRandomVideoPosition();
                }
                if (videoPosition != 0) {
                    videoPosition = videoPosition - 1;
                    if (videosList.get(videoPosition).getLayoutType() == 1) {
                        videoPosition = videoPosition - 1;
                    }
                    setVideoResume();
                }
                videoPlayLastPositionList.put(videosList.get(videoPosition).getFullPath(), mkPlayer.getCurrentPosition());
            }



            @Override
            public void onPlayPauseStatus(int status) {
                if (status == 3) {

                }
            }

            @Override
            public void onFloatingwindowClick() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    setValue();
                    startService(new Intent(VideoPlayerActivity.this, FloatingWidgetService.class));
                    finish();
                } else if (Settings.canDrawOverlays(VideoPlayerActivity.this)) {
                    setValue();
                    startService(new Intent(VideoPlayerActivity.this, FloatingWidgetService.class));
                    finish();
                } else {
                    askForSystemOverlayPermission();
                    Toast.makeText(VideoPlayerActivity.this, "System Alert Window Permission Is Required For Floating Widget.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onBackClick() {
//                onBackPressed();
                mkPlayer.pause();
                showLeaveDialog();
            }

            @Override
            public void onDeleteCallBack() {
                long[] videoId = {videosList.get(videoPosition).getId()};
                VideoPlayerUtils.showDeleteDialog(VideoPlayerActivity.this, videosList.get(videoPosition).getTitle(), videoId);
                mkPlayer.pause();
            }

            @Override
            public void onShareCallBack() {
                VideoPlayerUtils.shareVideo(videosList.get(videoPosition).getId(), VideoPlayerActivity.this);
            }

            @Override
            public void onRenameCallBack() {
                RenameDialog.getInstance(VideoPlayerActivity.this, videosList.get(videoPosition).getTitle(), videosList.get(videoPosition).getId(), videosList.get(videoPosition).getFullPath())
                        .show(getSupportFragmentManager(), "");
            }

            @Override
            public void onSleepTimeCallBack() {
                showSleepTimeDailog();
            }

            @Override
            public void onPropertiesCallBack() {
                VideoDetailsDialog.getInstance(videosList.get(videoPosition))
                        .show(getSupportFragmentManager(), "");
            }

            @Override
            public void onShuffleClick(boolean isShuffle) {
                isShuffleClick = isShuffle;
            }

            @Override
            public void onEqulizerClick() {
            }

            @Override
            public void onBackgroundEnable(boolean isEnable) {
                isBackgroundEnable = isEnable;
            }
        });

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mkPlayer != null) {
            if (videosList.get(videoPosition).getVideoDuration() != mkPlayer.getCurrentPosition()) {
                Video video = videosList.get(videoPosition);
                video.setVideoLastPlayPosition(mkPlayer.getCurrentPosition());
                preferencesUtility.setContinueWatchingVideos(video);
            }
            videoPlayLastPositionList.put(videosList.get(videoPosition).getFullPath(), mkPlayer.getCurrentPosition());
            preferencesUtility.setVideoLastPosition(videoPlayLastPositionList);
            RxBus.getInstance().post(new UpdateVideoStatusEvent());
            RxBus.getInstance().post(new HistoryEvent());
            RxBus.getInstance().post(new UpdateContinueWatchingEvent());
            mkPlayer.onDestroy();
        }
    }

    public void setVideoResume() {
        videoLastProgress = 0;
        if (videosList.size() > videoPosition) {
            resumeVideoList = preferencesUtility.getVideoLastPosition();
            if (isResumeVideo) {
                if (resumeVideoList.containsKey(videosList.get(videoPosition).getFullPath())) {
                    double progress = Double.parseDouble(String.valueOf(resumeVideoList.get(videosList.get(videoPosition).getFullPath())));
                    videoLastProgress = (int) progress;
                }
            } else if (isContinueWatching) {
                double progress = Double.parseDouble(String.valueOf(videosList.get(videoPosition).getVideoLastPlayPosition()));
                videoLastProgress = (int) progress;
            }
            mkPlayer.play(videosList.get(videoPosition).getFullPath());
            mkPlayer.setTitle(videosList.get(videoPosition).getTitle());
            mkPlayer.seekTo(videoLastProgress, false);
        }
    }

    public void setValue() {
        Video video = videosList.get(videoPosition);
        video.setVideoLastPlayPosition(mkPlayer.getCurrentPosition());
        preferencesUtility.setLastPlayVideos(video);
        preferencesUtility.setFloatingVideoPosition(videoPosition);
        preferencesUtility.setVideoList(videosList);
        preferencesUtility.setIsFloatingVideo(true);
    }

    public void getRandomVideoPosition() {
        Random random = new Random();
        int position = random.nextInt(videosList.size());
        videoPosition = position;
    }

    public void OnSleepTimeSet() {
        sleepCountDownTimer = new CountDownTimer(sleepTimeMiliSecond, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                mkPlayer.isSleepTimeOn = false;
                mkPlayer.pause();
            }
        };
        sleepCountDownTimer.start();
    }

    public void showSleepTimeDailog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_sleep_time, null);

        TextView start = (TextView) dialogLayout.findViewById(R.id.startSleepTime);
        TextView stop = (TextView) dialogLayout.findViewById(R.id.stopSleepTime);
        EditText sleepHour = dialogLayout.findViewById(R.id.sleepHour);
        EditText sleepMinutes = dialogLayout.findViewById(R.id.sleepMinutes);
        ImageView close = dialogLayout.findViewById(R.id.close_sleep);

        start.setOnClickListener(view -> {
            mkPlayer.isSleepTimeOn = true;
            int hour = 0, minutes = 0;
            if (!sleepHour.getText().toString().isEmpty()) {
                hour = Integer.parseInt(sleepHour.getText().toString());
            }
            if (!sleepMinutes.getText().toString().isEmpty()) {
                minutes = Integer.parseInt(sleepMinutes.getText().toString());
            }

            if (hour != 0) {
                sleepTimeMiliSecond = (hour * 60 * 60 * 1000) + (minutes * 60 * 1000);
            } else {
                sleepTimeMiliSecond = (minutes * 60 * 1000);
            }

            OnSleepTimeSet();
            sleepDailog.dismiss();
        });

        stop.setOnClickListener(view -> {
            if (sleepCountDownTimer != null) {
                sleepCountDownTimer.onFinish();
            }
        });

        close.setOnClickListener(view -> {
            sleepDailog.dismiss();
        });


        builder = new AlertDialog.Builder(this, R.style.ThemeDialogCustom);
        builder.setView(dialogLayout);
        builder.setCancelable(false);

        sleepDailog = builder.create();
        sleepDailog.show();
    }

    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available to open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    public void showEqulizer() {
        int sessionId = mkPlayer.videoView.getAudioSessionId();
        Log.d("sessionId ---->", "sessionId ---->" + sessionId);
        if (sessionId > 0) {
         /*   DialogEqualizerFragment fragment = DialogEqualizerFragment.newBuilder()
                    .setAudioSessionId(sessionId)
                    .themeColor(ContextCompat.getColor(this, R.color.black_transparent))
                    .textColor(ContextCompat.getColor(this, R.color.colorPrimaryTextWhite))
                    .accentAlpha(ContextCompat.getColor(this, R.color.colorSubTextWhite))
                    .darkColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    .setAccentColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .build();
            fragment.show(getSupportFragmentManager(), "eq");*/
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (mkPlayer != null) {
            mkPlayer.onPause();
        }
        startBackgroundVideoPlayService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mkPlayer != null) {
            mkPlayer.onResume();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video_play, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mkPlayer != null) {
            mkPlayer.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        mkPlayer.pause();
        showLeaveDialog();
    }

    public void showLeaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Are you sure you have to close this video ?")
                .setCancelable(false)
                .setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (mkPlayer != null && mkPlayer.onBackPressed()) {
                            return;
                        }
                        Random random = new Random();
                        int num = random.nextInt(4);
                        if (num == 2) {
                           // showFullNativeAd();
                        } else {
                            finish();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.action_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mkPlayer.start();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("VPlayer");
        alert.show();
    }

    public void startBackgroundVideoPlayService() {
        if (isBackgroundEnable) {
            Video video = videosList.get(videoPosition);
            video.setVideoLastPlayPosition(mkPlayer.getCurrentPosition());
            videosList.set(videoPosition, video);

            preferencesUtility.setVideoList(videosList);

            stopService(new Intent(this, VideoPlayAsAudioService.class));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, VideoPlayAsAudioService.class).putExtra(EXTRA_VIDEO_POSITION, videoPosition));
            } else {
                startService(new Intent(this, VideoPlayAsAudioService.class).putExtra(EXTRA_VIDEO_POSITION, videoPosition));
            }
        }
    }

   /* public View setDialogLayout() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_ads, null);

        ImageView closeAd = (ImageView) dialogLayout.findViewById(R.id.closeAd);
        RelativeLayout relativeLayout = (RelativeLayout) dialogLayout.findViewById(R.id.adView);

        if (isDarkTheme) {
            relativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.window_background_dark));
        } else {
            relativeLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.window_background));
        }

        closeAd.setOnClickListener(view -> {
            dialog.dismiss();
        });
        builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);
        builder.setCancelable(false);
        dialog = builder.create();
        return dialogLayout;
    }*/

}