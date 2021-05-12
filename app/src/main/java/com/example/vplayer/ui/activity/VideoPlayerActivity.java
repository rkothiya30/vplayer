package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
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
import static com.example.vplayer.service.VideoPlayAsAudioService.NOTIFICATION_CLICK_ACTION;

public class VideoPlayerActivity extends AppCompatActivity {

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
    HashMap<String, Integer> resumeVideoList = new HashMap<>();
    HashMap<String, Integer> videoPlayLastPositionList = new HashMap<>();

    TextView app_video_title;
    RelativeLayout app_video_bottom_box;

    PreferencesUtility preferencesUtility;

    public static Intent getIntent(Context context,  List<Video> videoList, int position) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        /*List<Video> videoList = new ArrayList<>();
        videoList.add(video);*/
        intent.putExtra(Constant.EXTRA_VIDEO_LIST, Parcels.wrap(videoList));
        intent.putExtra(Constant.EXTRA_VIDEO_POSITION, position);
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

        app_video_bottom_box = findViewById(R.id.app_video_bottom_box);
        app_video_title = findViewById(R.id.app_video_title);

        app_video_title.setTextSize(20);
        app_video_bottom_box.setBackgroundColor(getResources().getColor(R.color.white_transparent));

        videosList = new ArrayList<>();
        preferencesUtility = PreferencesUtility.getInstance(this);
        isResumeVideo = preferencesUtility.isResumeVideo();
        isAutoPlay = preferencesUtility.isAutoPlayVideo();

        startService(new Intent(this, VideoPlayAsAudioService.class).setAction(NOTIFICATION_CLICK_ACTION));

        if (getIntent() != null) {
            videosList = Parcels.unwrap(getIntent().getParcelableExtra(Constant.EXTRA_VIDEO_LIST));
            videoPosition = getIntent().getIntExtra(Constant.EXTRA_VIDEO_POSITION, 0);
        }

        if (videosList == null) {
            videosList = preferencesUtility.getVideoList();
        }

        isFloatingVideo = getIntent().getBooleanExtra(Constant.EXTRA_IS_FLOATING_VIDEO, false);

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


            /*@Override
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
            }*/
        });

       /* runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showNativeAd();
                showNativeFullAd();
            }
        });*/
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

    public void getRandomVideoPosition() {
        Random random = new Random();
        int position = random.nextInt(videosList.size());
        videoPosition = position;
    }

}