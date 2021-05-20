package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.ActionPlaying;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.DbHelper;
import com.example.vplayer.fragment.utils.Utilities;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.service.MusicService;
import com.example.vplayer.ui.fragment.OnPlaylistMenuFragment;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.vplayer.service.MusicService.mediaPlayer;
import static com.example.vplayer.ui.fragment.MusicFragment.audioList;

public class PlayingSongActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection/*, MediaPlayer.OnCompletionListener*/ {

    private static boolean FIRST_OPEN = false;
    private ImageView ivBAck, ivFavouritesSong, ivAddToPlaylist, ivShuffleSong;
    private String songTitle, artistName, songPath, songAlbum, songDuration, songId;
    private Uri songAlbumArt;
    private boolean isJustOpen = false;
    public static int position, currentSongIndex;
    private CircularImageView ivSongImage;
    private TextView tvSongName, tvArtistName, tvDuration, tvSongPlayedTime;
   // public static MediaPlayer mp;
    public static ImageView ivPlayPause;
    private ImageView ivPrevious, ivNext;
    private AppCompatSeekBar seekBar;
    Handler mHandler;
    private Utilities utils;
    public static List<AudioModel> songsList = new ArrayList<>();
    public static List<AudioModel> shuffledList = new ArrayList<>();
    private List<AudioModel> recentList;
    private SharedPreferences pref;
    SharedPreferences.Editor editor;
    DbHelper DB;
    public static List<String> createdPlaylist;

    private Intent playIntent;
    private boolean musicBound = false;
    Bitmap bitmap = null;
    private Thread playThread, nextThread, prevThread;
    MusicService musicService;
    public static String activityName;
    public Uri uri;
    private Handler handler = new Handler();
    public static boolean IS_SHUFFLED = false;
    private String currentActivityName;

   /* @Override
    protected void onStart() {
        super.onStart();

        if(playIntent == null)
        {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        setContentView(R.layout.activity_playing_song);

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getSupportActionBar().hide();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkGray)));

        if(Build.VERSION.SDK_INT >= 21)
        {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }

        createdPlaylist = new ArrayList<>();

        mHandler = new Handler();
        utils = new Utilities();
        DB = new DbHelper(this);

        ivBAck = findViewById(R.id.ivBackSong);
        ivSongImage = findViewById(R.id.civSongPhoto);
        tvSongName = findViewById(R.id.tvSongName);
        tvArtistName = findViewById(R.id.tvArtistName);
        tvDuration = findViewById(R.id.tvSongTotalTime);
        tvSongPlayedTime = findViewById(R.id.tvSongPlayedTime);
        ivPlayPause = findViewById(R.id.ivPlayPauseButton);
        ivPrevious = findViewById(R.id.ivPreviousSong);
        ivNext = findViewById(R.id.ivNextSong);
        seekBar = findViewById(R.id.sbSongDuration);
        ivFavouritesSong = findViewById(R.id.ivFavouritesSong);
        ivAddToPlaylist = findViewById(R.id.ivAddToPlaylist);

        ivShuffleSong = findViewById(R.id.ivShuffleSong);
        //ivSearch = findViewById(R.id.ivSearch);

        pref = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = pref.edit();

        currentSongIndex = pref.getInt("Position", 0);
        currentActivityName = pref.getString("ActivityName", "");

        //playSong(position);
        getIntentMethod();

        ivBAck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
                finish();
            }
        });

        /*ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mp != null && mp.isPlaying())
                {
                    mp.pause();
                    ivPlayPause.setImageResource(R.drawable.play);
                }
                else if(!mp.isPlaying())
                {
                    mp.start();
                    ivPlayPause.setImageResource(R.drawable.pause);
                }
            }
        });*/




        ivShuffleSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!IS_SHUFFLED) {
                    shuffledList = songsList;
                    Collections.shuffle(shuffledList);
                    IS_SHUFFLED = true;
                    Toast.makeText(PlayingSongActivity.this, "Songs Shuffled", Toast.LENGTH_SHORT).show();
                    ivShuffleSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                }
                else
                {
                    ivShuffleSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    IS_SHUFFLED = false;
                    Toast.makeText(PlayingSongActivity.this, "Playing in original sequence", Toast.LENGTH_SHORT).show();
                }
            }
        });



        ivFavouritesSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor checkUser = DB.getFavouritesFromId(songsList.get(position).getName());

                if(checkUser.getCount() <= 0) {
                    boolean checkAdd = DB.addToFavourites(songPath, songTitle, artistName, songAlbum, songDuration, songId, songAlbumArt.toString());
                    if(checkAdd)
                    {
                        Toast.makeText(PlayingSongActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.tab_selected_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                }
                else
                {
                    boolean checkDelFav = DB.removeFromFavourites(songsList.get(position).getName());
                    if(checkDelFav) {
                        Toast.makeText(PlayingSongActivity.this, "Remove from Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                }
            }
        });

        ivAddToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showShortDialog(position, songsList.get(position).getName());
            }
        });

        /*if(mediaPlayer != null)
        {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    afterSongCompletion();
                }
            });
        }*/


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(musicService != null && b)
                {
                    musicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayingSongActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    tvSongPlayedTime.setText(formattedTime(mCurrentPosition));
                    tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private String formattedTime(int mCurrentPosition) {

        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1)
        {
            return totalNew;
        }
        else
        {
            return totalOut;
        }
    }

    public void getIntentMethod()
    {



        activityName = getIntent().getStringExtra("ActivityName");

        if(activityName.equals("MusicFragment"))
        {
            songsList = audioList;
            position = getIntent().getIntExtra("Position", -1);
        } else if(activityName.equals("PlayPlayListActivity")){
            List<AudioModel> a = new ArrayList<>();
            a =  Parcels.unwrap(getIntent().getParcelableExtra("Audio"));

                 //   a.add(audio1);
            songsList = a;
                    position = getIntent().getIntExtra("Position", -1);
        }

       /* if(activityName.equals("Album"))
        {
            songsList = albumSongs;
        }
        else if (activityName.equals("Artist"))
        {
            songsList = artistSongs;
        }
        else if (activityName.equals("Playlist"))
        {
            songsList = playlist;
        }
        else if (activityName.equals("Genre"))
        {
            songsList = genreSongs;
        }
        else if (activityName.equals("Folder"))
        {
            songsList = folderSongsList;
        }
        else if (activityName.equals("CreatedPlayListSongs"))
        {
            songsList = myPlaylist;
        }
        else {
            songsList = audioList;
        }*/

        if(songsList != null)
        {
            uri = Uri.parse(songsList.get(position).getPath());
        }

        if(currentSongIndex == position ) //&& FIRST_OPEN)
        {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("ServicePosition", position);
            startService(intent);
            ivPlayPause.setImageResource(R.drawable.ic_pause);
        }
        else if(currentSongIndex != position) {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("ServicePosition", position);
            startService(intent);
            ivPlayPause.setImageResource(R.drawable.ic_pause);
        }
        /*else if(!currentActivityName.equals(activityName))
        {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("ServicePosition", position);
            startService(intent);
            ivPlayPause.setImageResource(R.drawable.pause);
        }*/
        setBottomLayout();
        if(musicService != null) {
            musicService.showNotification(R.drawable.ic_pause);
        }


    }

    public void setBottomLayout()
    {
        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));

        AudioModel audioModel = songsList.get(position);
        File f = new File(audioModel.getPath());
        Uri albumArtUri = Uri.fromFile(f);
        bitmap = null;
        //ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), albumArtUri);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), albumArtUri);
            // bitmap = ImageDecoder.decodeBitmap(source);
        } catch (FileNotFoundException exception) {
        } catch (IOException e) {

            e.printStackTrace();
        }

       /* if(tvPlayingSongArtist != null && tvPlayingSongName != null && ivPlayingSongImage != null) {
            tvPlayingSongArtist.setText(songsList.get(position).getArtist());
            tvPlayingSongName.setText(songsList.get(position).getName());
            pos = position;
            actName = activityName;
            if(bitmap != null) {
                ivPlayingSongImage.setImageBitmap(bitmap);
            }
            else
            {
                ivPlayingSongImage.setImageResource(R.drawable.music_photo);
            }
        }*/

        songTitle = songsList.get(position).getName();
        songPath = songsList.get(position).getPath();
        songAlbum = songsList.get(position).getAlbum();
        songId = songsList.get(position).getId();
        songDuration = songsList.get(position).getDuration();

        AudioModel aModel = songsList.get(position);
        File f1 = new File(aModel.getPath());
        songAlbumArt = Uri.fromFile(f1);
        artistName = songsList.get(position).getArtist();

        tvSongName.setText(songTitle);

        if(bitmap != null) {
            ivSongImage.setImageBitmap(bitmap);
        }
        else
        {
            ivSongImage.setImageResource(R.drawable.ic_music_icon_round);
        }
        currentSongIndex = position;

        tvArtistName.setText(artistName);

        //ivPlayPause.setImageResource(R.drawable.pause);

        seekBar.setProgress(0);
        seekBar.setMax(100);

        Cursor checkFavUser = DB.getFavouritesFromId(songsList.get(position).getName());
        if(checkFavUser.getCount() > 0)
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.md_green_500), android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        Cursor checkUser = DB.getRecentSongFromId(songsList.get(position).getId());

        if(checkUser.getCount() == 0) {
            boolean checkAdd = DB.insertRecentSongs(songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songAlbumArt.toString());
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Name", songTitle);
        editor.putString("Artist", artistName);
        // editor.putString("Duration", String.valueOf(musicService.getDuration()));
        editor.putString("ActivityName", activityName);
        editor.putInt("Position", position);
        editor.putString("AlbumArt",songAlbumArt.toString());
        editor.apply();
    }

     public void showShortDialog ( int adapterPosition, String playlistName){
        OnPlaylistMenuFragment bottomSheetDialog = OnPlaylistMenuFragment.newInstance(adapterPosition, playlistName);
        //bottomSheetDialog.setOuterClickListener(this);
        bottomSheetDialog.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");


    }



    public void  playSong(int songIndex) {

        songsList = new ArrayList<>();
        /*if(activityName.equals("Album"))
        {
            songsList = albumSongs;
        }
        else if (activityName.equals("Artist"))
        {
            songsList = artistSongs;
        }
        else if (activityName.equals("Playlist"))
        {
            songsList = playlist;
        }
        else if (activityName.equals("Genre"))
        {
            songsList = genreSongs;
        }
        else if (activityName.equals("Folder"))
        {
            songsList = folderSongsList;
        }
        else {
            songsList = audioList;
        }*/

        if(currentSongIndex != songIndex) {
           // musicService.reset();

            if(mediaPlayer != null)
            {
                musicService.stop();
                musicService.release();
                musicService.createMediaPlayer(songIndex);
                musicService.start();
            }
            else
            {
                musicService.createMediaPlayer(songIndex);
                musicService.start();
            }
                // mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                // musicService.pause();
           /* mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mp.start();
                }
            });*/
           // mp.start();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
        finish();
    }

    public void afterSongCompletion()
     {
        if(currentSongIndex < (songsList.size() - 1)){
            playSong(currentSongIndex + 1);
            currentSongIndex = currentSongIndex + 1;
        }else{
            if (songsList.size() > 0) {
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    protected void onResume()
    {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

       // unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                ivPrevious.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        previousButtonClick();
                    }
                });
            }
        };
        prevThread.start();
    }

    private void nextThreadBtn() {
        nextThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                ivNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextButtonClick();
                    }
                });
            }
        };
        nextThread.start();
    }

    private void playThreadBtn() {

        playThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                ivPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseButtonClick();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseButtonClick() {

        if(musicService.isPlaying())
        {
            musicService.pause();
            musicService.showNotification(R.drawable.ic_play);
            ivPlayPause.setImageResource(R.drawable.ic_play);
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayingSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                    }
                    handler.postDelayed(this, 100);
                }
            });
        }
        else
        {
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause);
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayingSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                    }
                    handler.postDelayed(this, 100);
                }
            });
        }
      //  musicService.onComplete();
    }

    public void nextButtonClick()
    {
        if(musicService.isPlaying() && musicService != null)
        {
            musicService.stop();
            musicService.release();
            position = ((position + 1) % songsList.size());
            musicService.createMediaPlayer(position);
            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayingSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause);
        }
        else
        {
            musicService.stop();
            musicService.release();
            position = ((position + 1) % songsList.size());
            musicService.createMediaPlayer(position);
            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayingSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
        }
    }

    public void previousButtonClick()
    {
        /*if (currentSongIndex > 0) {
            playSong(currentSongIndex - 1);
            //currentSongIndex = currentSongIndex - 1;
        } else {
            if (songsList.size() > 0) {
                playSong(songsList.size() - 1);
                //currentSongIndex = songsList.size() - 1;
            }
        }
        musicService.onComplete();*/

        if(musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            position = ((position - 1) < 0 ? (songsList.size() - 1) : (position - 1));
            musicService.createMediaPlayer(position);
            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayingSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause);
        }
        else
        {
            musicService.stop();
            musicService.release();
            position = ((position - 1) < 0 ? (songsList.size() - 1) : (position - 1));
            musicService.createMediaPlayer(position);
            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayingSongActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null)
                    {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder)iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
       // Toast.makeText(this, "Connected :" + musicService, Toast.LENGTH_LONG).show();
        musicService.showNotification(R.drawable.ic_pause);
        musicService.onComplete();
        FIRST_OPEN = false;
        if(mediaPlayer.isPlaying())
        {
            ivPlayPause.setImageResource(R.drawable.ic_pause);
        }
        else
        {
            ivPlayPause.setImageResource(R.drawable.ic_play);
        }
       // setBottomLayout();
        /*Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("ServicePosition", position);
        startService(intent);
        playSong(position);*/
        /*seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                *//*int x = (int) Math.ceil(i / 1000f);

                if (x == 0 && musicService != null && !musicService.isPlaying()) {
                    // clearMediaPlayer();
                    // fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    seekBar.setProgress(x);
                }*//*

                if(musicService != null && b)
                {
                    mediaPlayer.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mHandler.removeCallbacks(mUpdateTimeTask);
                int totalDuration = musicService.getDuration();
                int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

                // forward or backward to certain seconds
                musicService.seekTo(currentPosition);

                // update timer progress again
                updateProgressBar();
            }
        });*/

       // musicService.onComplete();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }



   /* @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        nextButtonClick();
        *//*if(musicService != null)
        {
            musicService.createMediaPlayer(position);
            musicService.start();
            musicService.onComplete();
        }*//*
    }*/
}