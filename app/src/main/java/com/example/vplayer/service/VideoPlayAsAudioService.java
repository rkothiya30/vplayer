package com.example.vplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.VideoPlayerActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.vplayer.fragment.utils.Constant.EXTRA_VIDEO_POSITION;


public class VideoPlayAsAudioService extends Service {

    static final String CHANNEL_ID = "video_player_channel";

    public static final String PAUSE_ACTION = "vplayer.pause";
    public static final String STOP_ACTION = "vplayer.stop";
    public static final String PREVIOUS_ACTION = "vplayer.previous";
    public static final String NEXT_ACTION = "vplayer.next";
    public static final String PLAY_ACTION = "vplayer.play";
    public static final String TOGGLEPAUSE_ACTION = "vplayer.togglepause";
    public static final String NOTIFICATION_CLICK_ACTION = "notification_click";

    private NotificationManagerCompat mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private long mNotificationPostTime = 0;
    int notificationId;
    boolean isRefresh = false;

    MediaSessionCompat mediaSessionCompat;
    PreferencesUtility preferencesUtility;
    List<Video> videoList = new ArrayList<>();
    private static int videoPosition;
    MediaPlayer mediaPlayer;
    boolean isUpdate = false;
    Handler handler = new Handler();
    Runnable runnable;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            handleCommandIntent(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");

        mNotificationManager = NotificationManagerCompat.from(this);
        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.stop();
        mediaPlayer.setDisplay(null);

        preferencesUtility = PreferencesUtility.getInstance(this);
        videoList = preferencesUtility.getVideoList();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // You only need to create the channel on API 26+ devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            }
        }

        final IntentFilter filter = new IntentFilter();
        filter.addAction(PAUSE_ACTION);
        filter.addAction(STOP_ACTION);
        filter.addAction(NEXT_ACTION);
        filter.addAction(PREVIOUS_ACTION);
        registerReceiver(mIntentReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null && intent.getAction().equals(NOTIFICATION_CLICK_ACTION)) {
                preferencesUtility.setFloatingVideoPosition(mediaPlayer.getCurrentPosition());
                handler.removeCallbacks(runnable);
                stopForeground(true);
                stopSelf();
            } else {
                if (!isUpdate) {
                    videoPosition = intent.getIntExtra(EXTRA_VIDEO_POSITION, 0);
                    videoPlay();
                    isUpdate = true;
                }
                handleCommandIntent(intent);
                managePlayVideo();
            }
        }
        return START_STICKY;
    }

    public void managePlayVideo() {

        if (videoList == null) {
            return;
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (videoPosition != videoList.size()) {
                    videoPosition = videoPosition + 1;
                    if (videoList.get(videoPosition).getLayoutType() == 1) {
                        videoPosition = videoPosition + 1;
                    }
                    isRefresh = true;
                    handler.removeCallbacks(runnable);
                    videoPlay();
                }
            }
        });
    }

    public void videoPlay() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(videoList.get(videoPosition).getFullPath());
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mediaPlayer.seekTo(videoList.get(videoPosition).getVideoLastPlayPosition());
                    mediaPlayer.start();
                    updateNotification();
                    runnable.run();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();

        if (NEXT_ACTION.equals(action)) {
            if(videoList.size()==1){
                //handler.removeCallbacks(runnable);
                videoPlay();
                //return;
            }
            else if (videoPosition != videoList.size()) {
                videoPosition = videoPosition + 1;
                if (videoList.get(videoPosition).getLayoutType() == 1) {
                    videoPosition = videoPosition + 1;
                }
                isRefresh = true;
                handler.removeCallbacks(runnable);
                videoPlay();
            }
        } else if (PREVIOUS_ACTION.equals(action)) {
            if (videoPosition != 0) {
                videoPosition = videoPosition - 1;
                if (videoList.get(videoPosition).getLayoutType() == 1) {
                    videoPosition = videoPosition - 1;
                }
                isRefresh = true;
                handler.removeCallbacks(runnable);
                videoPlay();
            }
        } else if (PAUSE_ACTION.equals(action)) {
            mediaPlayer.pause();
            isRefresh = true;
            updateNotification();

        } else if (PLAY_ACTION.equals(action)) {
            mediaPlayer.start();
            isRefresh = true;
            updateNotification();

        } else if (STOP_ACTION.equals(action)) {
            mediaPlayer.stop();
            isRefresh = false;
            handler.removeCallbacks(runnable);
            stopForeground(true);
            stopSelf();
            mNotificationManager.cancelAll();
            mNotificationManager.cancel(notificationId);
        }
    }

    private void updateNotification() {
        notificationId = hashCode();
        mNotificationManager.cancelAll();

        if (!isRefresh) {
            startForeground(notificationId, buildNotification());
        } else {
            mNotificationManager.notify(notificationId, buildNotification());
        }
    }

    private Notification buildNotification() {

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.layout_notification_collapse);
        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.layout_notification);

        final String albumName = videoList.get(videoPosition).getTitle();
        final boolean isPlaying = mediaPlayer.isPlaying();

        String text = VideoPlayerUtils.makeShortTimeString(this, mediaPlayer.getCurrentPosition() / 1000) + " / " + VideoPlayerUtils.makeShortTimeString(this, videoList.get(videoPosition).getVideoDuration() / 1000);

        int playButtonResId = isPlaying
                ? R.drawable.ic_pause : R.drawable.ic_play;

        Intent nowPlayingIntent = new Intent(VideoPlayerActivity.getIntent(this, videoList, videoPosition, true));
        PendingIntent clickIntent = PendingIntent.getActivity(this, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        File file = new File(videoList.get(videoPosition).getFullPath());
        RequestOptions options = new RequestOptions()
                .signature(new ObjectKey(file.getAbsolutePath() + file.lastModified()))
                .priority(Priority.LOW)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        Glide.with(this)
                .load(videoList.get(videoPosition).getFullPath()).apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        contentView.setImageViewBitmap(R.id.songImage, drawableToBitmap(resource));
                        expandedView.setImageViewBitmap(R.id.songImage, drawableToBitmap(resource));
                    }
                });

//        ContentResolver crThumb = getContentResolver();
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, videoList.get(videoPosition).getId(), MediaStore.Video.Thumbnails.MICRO_KIND, options);
//        contentView.setImageViewBitmap(R.id.songImage, curThumb);
//        expandedView.setImageViewBitmap(R.id.songImage, curThumb);

        if (mNotificationPostTime == 0) {
            mNotificationPostTime = System.currentTimeMillis();
        }

        contentView.setTextViewText(R.id.songName, albumName);
        contentView.setTextViewText(R.id.artistName, text);
        contentView.setImageViewResource(R.id.play_pause, playButtonResId);

        expandedView.setTextViewText(R.id.songName, albumName);
        expandedView.setTextViewText(R.id.artistName, text);
        expandedView.setImageViewResource(R.id.play_pause, playButtonResId);

        notificationBuilder.setSmallIcon(R.drawable.vplayer)
                .setContentIntent(clickIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setCustomContentView(contentView)
                .setCustomBigContentView(expandedView);

        if (isPlaying) {
            contentView.setOnClickPendingIntent(R.id.play_pause, retrievePlaybackAction(PAUSE_ACTION));
            expandedView.setOnClickPendingIntent(R.id.play_pause, retrievePlaybackAction(PAUSE_ACTION));
        } else {
            contentView.setOnClickPendingIntent(R.id.play_pause, retrievePlaybackAction(PLAY_ACTION));
            expandedView.setOnClickPendingIntent(R.id.play_pause, retrievePlaybackAction(PLAY_ACTION));
        }

        contentView.setOnClickPendingIntent(R.id.next, retrievePlaybackAction(NEXT_ACTION));
        contentView.setOnClickPendingIntent(R.id.close, retrievePlaybackAction(STOP_ACTION));

        expandedView.setOnClickPendingIntent(R.id.next, retrievePlaybackAction(NEXT_ACTION));
        expandedView.setOnClickPendingIntent(R.id.previous, retrievePlaybackAction(PREVIOUS_ACTION));
        expandedView.setOnClickPendingIntent(R.id.close, retrievePlaybackAction(STOP_ACTION));

        runnable = new Runnable() {
            @Override
            public void run() {
                isRefresh = true;
                updateNotification();
                handler.postDelayed(runnable, 1000);
            }
        };

        return notificationBuilder.build();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(this, VideoPlayAsAudioService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        intent.putExtra(EXTRA_VIDEO_POSITION, videoPosition);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        mediaPlayer.stop();
        stopSelf();
        stopForeground(true);
        mNotificationManager.cancelAll();
        unregisterReceiver(mIntentReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = "VideoPlayer Service";
        // The user-visible description of the channel.
        String description = "VideoPlayer";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setSound(null, null);
        mChannel.setShowBadge(false);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(mChannel);
    }
}
