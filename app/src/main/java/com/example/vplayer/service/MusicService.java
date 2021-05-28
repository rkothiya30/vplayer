package com.example.vplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.vplayer.Notification.NotificationReceiver;
import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.ActionPlaying;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.ui.activity.EmptyActivity;
import com.example.vplayer.ui.activity.PlayingSongActivity;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static com.example.vplayer.fragment.utils.Constant.ACTION_NEXT;
import static com.example.vplayer.fragment.utils.Constant.ACTION_PLAY;
import static com.example.vplayer.fragment.utils.Constant.ACTION_PREVIOUS;
import static com.example.vplayer.service.MusicDataService.audioList;
import static com.example.vplayer.ui.activity.PlayingSongActivity.IS_SHUFFLED;
import static com.example.vplayer.ui.activity.PlayingSongActivity.activityName;
import static com.example.vplayer.ui.activity.PlayingSongActivity.intent1;
import static com.example.vplayer.ui.activity.PlayingSongActivity.songsList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    private static final String CHANNEL_ID2 = "n_channel";
    IBinder mBinder = new MyBinder();
    public static MediaPlayer mediaPlayer;
    // List<AudioModel> myList = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.e("Bind", "Service");
        return mBinder;
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int myPosition = intent.getIntExtra("ServicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1) {
            playMedia(myPosition);
        }

        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    //Toast.makeText(this, "PlayPause", Toast.LENGTH_LONG).show();
                    if (actionPlaying != null) {
                        Log.e("Msg", "PlayPause");
                        actionPlaying.playPauseButtonClick();
                    }
                    break;

                case "next":
                    //Toast.makeText(this, "Next", Toast.LENGTH_LONG).show();
                    if (actionPlaying != null) {
                        Log.e("Msg", "PlayPause");
                        actionPlaying.nextButtonClick();
                    }
                    break;

                case "previous":
                    //Toast.makeText(this, "Previous", Toast.LENGTH_LONG).show();
                    if (actionPlaying != null) {
                        Log.e("Msg", "PlayPause");
                        actionPlaying.previousButtonClick();
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {

        /*if(activityName.equals("Album"))
        {
            myList = albumSongs;
        }
        else if (activityName.equals("Artist"))
        {
            myList = artistSongs;
        }
        else if (activityName.equals("Playlist"))
        {
            myList = playlist;
        }
        else if (activityName.equals("Genre"))
        {
            myList = genreSongs;
        }
        else if (activityName.equals("Folder"))
        {
            myList = folderSongsList;
        }
        else {
            myList = audioList;
        }*/

        position = startPosition;
        if (mediaPlayer != null) {


            mediaPlayer.release();
            if (songsList != null) {

                createMediaPlayer(position);
                mediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }


    public void start() {
        mediaPlayer.start();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void release() {
        mediaPlayer.release();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public void createMediaPlayer(int positionInner) {
        position = positionInner;
        if (IS_SHUFFLED) {
            //uri = Uri.parse(shuffledList.get(position).getPath());
        } else {
            songsList = audioList;

            if (activityName.equals("MusicFragment")) {
                songsList = audioList;
                //position = getIntent().getIntExtra("Position", -1);
            } else if (activityName.equals("PlayPlayListActivity")) {
                List<AudioModel> a = new ArrayList<>();
                a = Parcels.unwrap(intent1.getParcelableExtra("Audio"));

                //   a.add(audio1);
                songsList = a;
                //position = getIntent().getIntExtra("Position", -1);
            } else if (activityName.equals("PlayListItemAdapter")) {
                List<AudioModel> a = new ArrayList<>();
                a = Parcels.unwrap(intent1.getParcelableExtra("Audio"));

                //   a.add(audio1);
                songsList = a;
                //position = getIntent().getIntExtra("Position", -1);
            } else if (activityName.equals("OnMenuFragment")) {
                songsList = audioList;

            }
            if (position >= songsList.size())
                return;
            uri = Uri.parse(songsList.get(position).getPath());
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
    }

    int lastPosition;

    public int getCurrentPosition() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            lastPosition = mediaPlayer.getCurrentPosition();
            return mediaPlayer.getCurrentPosition();
        } else {
            return lastPosition;
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void reset() {
        mediaPlayer.reset();
    }

    public void prepare() {
        mediaPlayer.prepareAsync();
    }

    public void onComplete() {
        mediaPlayer.setOnCompletionListener(this);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (actionPlaying != null) {
            actionPlaying.nextButtonClick();
            if (mediaPlayer != null) {
                createMediaPlayer(position);
                start();
                onComplete();
            }
        }

        /*createMediaPlayer(position);
        start();*/
    }

    public void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    public void removeNotification(){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
    }




    public void showNotification(int playPauseButton) {

        // tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
        Intent intent = new Intent(this, PlayingSongActivity.class);
        //intent.putExtra("ActivityName","MusicFragment");
        //intent.putExtra("Position", position);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture = null;
        picture = getAlbumArt(songsList.get(position).getPath());
        Bitmap thumb = null;

        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_icon_foreground);
        }
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification;
        //NotificationChannel mChannel = notificationManager.getNotificationChannel(CHANNEL_ID2);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID2, CHANNEL_ID2, NotificationManager.IMPORTANCE_LOW);
            channel2.setDescription("Channel 2 desc...");
            channel2.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =(NotificationManager) getSystemService(NotificationManager.class);
// notificationManager.createNotificationChannel(channel1);
            notificationManager.createNotificationChannel(channel2);

            notification = new Notification.Builder(this, CHANNEL_ID2)
                    .setSmallIcon(R.drawable.ic_play)
                    .setLargeIcon(thumb)
                    .setContentTitle(songsList.get(position).getName())
                    .setContentText(songsList.get(position).getArtist())
                    .addAction(R.drawable.ic_previous, "previous", prevPending)
                    .addAction(playPauseButton, "Pause", pausePending)
                    .addAction(R.drawable.ic_next, "Next", nextPending)

                    .setOnlyAlertOnce(true)
                    .setContentIntent(contentIntent)
                    .build();
        } else {


            notification = new NotificationCompat.Builder(this, CHANNEL_ID2)
                    .setSmallIcon(R.drawable.ic_play)
                    .setLargeIcon(thumb)
                    .setContentTitle(songsList.get(position).getName())
                    .setContentText(songsList.get(position).getArtist())
                    .addAction(R.drawable.ic_previous, "previous", prevPending)
                    .addAction(playPauseButton, "Pause", pausePending)
                    .addAction(R.drawable.ic_next, "Next", nextPending)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(contentIntent)
                    .build();
        }

      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // You only need to create the channel on API 26+ devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager
                        mNotificationManager =
                        (NotificationManager) this
                                .getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel mChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID2);
                if (mChannel != null) {
                    mNotificationManager.deleteNotificationChannel(CHANNEL_ID2);
                    createChannel();
                } else {
                    createChannel();
                }
            }
        }*/



       /* NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);*/


        startForeground(2, notification);

        if (!isPlaying()) {
            stopForeground(false);
            // stopSelf();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = "VPlayer Service";
        // The user-visible description of the channel.
        String description = "VPlayer";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID2, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setSound(null, null);
        mChannel.setShowBadge(false);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(mChannel);
    }

    @Override
    public void onDestroy() {
       stopForeground(true);

        super.onDestroy();
    }

    public byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

}
