package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayer.ActionPlaying;
import com.example.musicplayer.Adapter.CreatedPlaylistAdapter;
import com.example.musicplayer.DataBase.DbHelper;
import com.example.musicplayer.ModelClass.AudioModel;
import com.example.musicplayer.MusicService;
import com.example.musicplayer.R;
import com.example.musicplayer.Utilities;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.musicplayer.Adapter.CreatedPlaylistSongsAdapter.PlaylistSongs;
import static com.example.musicplayer.Adapter.PlaylistAdapter.playlist;
import static com.example.musicplayer.GetAllSongs.audioList;
import static com.example.musicplayer.MusicService.mediaPlayer;
import static com.example.musicplayer.MyApplication.ACTION_NEXT;
import static com.example.musicplayer.MyApplication.ACTION_PLAY;
import static com.example.musicplayer.MyApplication.ACTION_PREVIOUS;
import static com.example.musicplayer.MyApplication.CHANNEL_ID2;
import static com.example.musicplayer.activities.AlbumSongsActivity.albumSongs;
import static com.example.musicplayer.activities.ArtistSongsActivity.artistSongs;
import static com.example.musicplayer.activities.FolderSongsActivity.folderSongsList;
import static com.example.musicplayer.activities.GenresSongsActivity.genreSongs;
import static com.example.musicplayer.activities.LibraryActivity.actName;
import static com.example.musicplayer.activities.LibraryActivity.ivPlayingSongImage;
import static com.example.musicplayer.activities.LibraryActivity.pos;
import static com.example.musicplayer.activities.LibraryActivity.tvPlayingSongArtist;
import static com.example.musicplayer.activities.LibraryActivity.tvPlayingSongName;
import static com.example.musicplayer.activities.PlaylistSongsActivity.myPlaylist;
import static com.example.musicplayer.activities.SplashScreenActivity.FIRST_OPEN;

public class PlayingSongActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection/*, MediaPlayer.OnCompletionListener*/ {

    private ImageView ivBAck, ivFavouritesSong, ivAddToPlaylist, ivEqualizer, ivShuffleSong;
    private String songTitle, artistName, songPath, songAlbum, songDuration, songId;
    private Uri songAlbumArt;
    private boolean isJustOpen = false;
    public static int position, currentSongIndex;
    private CircularImageView ivSongImage;
    private TextView tvSongName, tvArtistName, tvDuration, tvSongPlayedTime;
   // public static MediaPlayer mp;
    public static ImageView ivPlayPause;
    private ImageView ivPrevious, ivNext;
    private SeekBar seekBar;
    Handler mHandler;
    private Utilities utils;
    public static List<AudioModel> songsList = new ArrayList<>();
    public static List<AudioModel> shuffledList = new ArrayList<>();
    private List<AudioModel> recentList;
    private SharedPreferences pref;
    SharedPreferences.Editor editor;
    DbHelper DB;
    public static List<String> createdPlaylist;
    private CreatedPlaylistAdapter createdPlaylistAdapter;
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
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        setContentView(R.layout.activity_playing_song);

        if(Build.VERSION.SDK_INT >= 21)
        {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.StatusBar));
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
        ivEqualizer = findViewById(R.id.ivEqualizerSong);
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
                overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
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

        ivEqualizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

                if ((intent.resolveActivity(getPackageManager()) != null)) {
                    startActivityForResult(intent, 0);
                }
                else {
                    Toast.makeText(PlayingSongActivity.this, "Equalizer is not available for your device", Toast.LENGTH_SHORT).show();
                }*/

                if(musicService != null)
                {
                    Intent equalizerIntent = new Intent(PlayingSongActivity.this, EqualizerActivity.class);
                    startActivity(equalizerIntent);
                }
                else
                {
                    Toast.makeText(PlayingSongActivity.this, "Media player is not started", Toast.LENGTH_LONG).show();
                }
            }
        });

        /*ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if next song is there or not
                if (currentSongIndex < (songsList.size() - 1)) {
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    if (songsList.size() > 0) {
                        playSong(0);
                    }
                    currentSongIndex = 0;
                }

            }
        });*/

        /*ivPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    if (songsList.size() > 0) {
                        playSong(songsList.size() - 1);
                        currentSongIndex = songsList.size() - 1;
                    }
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
                    ivShuffleSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
                }
                else
                {
                    ivShuffleSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
                    IS_SHUFFLED = false;
                    Toast.makeText(PlayingSongActivity.this, "Playing in original sequence", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int x = (int) Math.ceil(i / 1000f);

                if (x == 0 && musicService != null && !musicService.isPlaying()) {
                    // clearMediaPlayer();
                    // fab.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    seekBar.setProgress(x);
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

        /*mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                afterSongCompletion();
            }
        });*/

        ivFavouritesSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor checkUser = DB.getFavouritesFromId(songsList.get(position).getName());

                if(checkUser.getCount() <= 0) {
                    boolean checkAdd = DB.addToFavourites(songPath, songTitle, artistName, songAlbum, songDuration, songId, songAlbumArt.toString());
                    if(checkAdd)
                    {
                        Toast.makeText(PlayingSongActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                }
                else
                {
                    boolean checkDelFav = DB.removeFromFavourites(songsList.get(position).getName());
                    if(checkDelFav) {
                        Toast.makeText(PlayingSongActivity.this, "Remove from Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.countTxt), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                }
            }
        });

        ivAddToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
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
        position = getIntent().getIntExtra("Position", -1);
        activityName = getIntent().getStringExtra("ActivityName");
        if(activityName.equals("Album"))
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
        }

        if(songsList != null)
        {
            uri = Uri.parse(songsList.get(position).getPath());
        }

        if(currentSongIndex == position && FIRST_OPEN)
        {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("ServicePosition", position);
            startService(intent);
            ivPlayPause.setImageResource(R.drawable.pause);
        }
        else if(currentSongIndex != position) {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("ServicePosition", position);
            startService(intent);
            ivPlayPause.setImageResource(R.drawable.pause);
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
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
        }

        /*if(musicService != null) {
            musicService.stop();
            musicService.release();
        }
        musicService.createMediaPlayer(position);
        musicService.start();*/

        /*Uri albumArtUri = songsList.get(position).getAlbumArt();
        bitmap = null;
        //ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), albumArtUri);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), albumArtUri);
            // bitmap = ImageDecoder.decodeBitmap(source);
        } catch (FileNotFoundException exception) {
        } catch (IOException e) {

            e.printStackTrace();
        }

        if(tvPlayingSongArtist != null && tvPlayingSongName != null && ivPlayingSongImage != null) {
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
        }

        songTitle = songsList.get(position).getName();
        songPath = songsList.get(position).getPath();
        songAlbum = songsList.get(position).getAlbum();
        songId = songsList.get(position).getId();
        songDuration = songsList.get(position).getDuration();
        songAlbumArt = songsList.get(position).getAlbumArt();
        artistName = songsList.get(position).getArtist();

        tvSongName.setText(songTitle);

        if(bitmap != null) {
            ivSongImage.setImageBitmap(bitmap);
        }
        else
        {
            ivSongImage.setImageResource(R.drawable.music_photo);
        }
        currentSongIndex = position;

        tvArtistName.setText(artistName);

        ivPlayPause.setImageResource(R.drawable.pause);*/

        //setBottomLayout();

        /*seekBar.setProgress(0);
        seekBar.setMax(100);

        Cursor checkFavUser = DB.getFavouritesFromId(songsList.get(position).getName());
        if(checkFavUser.getCount() > 0)
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.countTxt), android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        Cursor checkUser = DB.getRecentSongFromId(songsList.get(position).getId());

        if(checkUser.getCount() == 0) {
            boolean checkAdd = DB.insertRecentSongs(songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songsList.get(position).getAlbumArt().toString());
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Name", songTitle);
        editor.putString("Artist", artistName);
        // editor.putString("Duration", String.valueOf(musicService.getDuration()));
        editor.putString("ActivityName", activityName);
        editor.putInt("Position", position);
        editor.putString("AlbumArt", songsList.get(position).getAlbumArt().toString());
        editor.apply();*/
    }

    public void setBottomLayout()
    {
        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
        Uri albumArtUri = songsList.get(position).getAlbumArt();
        bitmap = null;
        //ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), albumArtUri);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), albumArtUri);
            // bitmap = ImageDecoder.decodeBitmap(source);
        } catch (FileNotFoundException exception) {
        } catch (IOException e) {

            e.printStackTrace();
        }

        if(tvPlayingSongArtist != null && tvPlayingSongName != null && ivPlayingSongImage != null) {
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
        }

        songTitle = songsList.get(position).getName();
        songPath = songsList.get(position).getPath();
        songAlbum = songsList.get(position).getAlbum();
        songId = songsList.get(position).getId();
        songDuration = songsList.get(position).getDuration();
        songAlbumArt = songsList.get(position).getAlbumArt();
        artistName = songsList.get(position).getArtist();

        tvSongName.setText(songTitle);

        if(bitmap != null) {
            ivSongImage.setImageBitmap(bitmap);
        }
        else
        {
            ivSongImage.setImageResource(R.drawable.music_photo);
        }
        currentSongIndex = position;

        tvArtistName.setText(artistName);

        //ivPlayPause.setImageResource(R.drawable.pause);

        seekBar.setProgress(0);
        seekBar.setMax(100);

        Cursor checkFavUser = DB.getFavouritesFromId(songsList.get(position).getName());
        if(checkFavUser.getCount() > 0)
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.countTxt), android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        Cursor checkUser = DB.getRecentSongFromId(songsList.get(position).getId());

        if(checkUser.getCount() == 0) {
            boolean checkAdd = DB.insertRecentSongs(songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songsList.get(position).getAlbumArt().toString());
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Name", songTitle);
        editor.putString("Artist", artistName);
        // editor.putString("Duration", String.valueOf(musicService.getDuration()));
        editor.putString("ActivityName", activityName);
        editor.putInt("Position", position);
        editor.putString("AlbumArt", songsList.get(position).getAlbumArt().toString());
        editor.apply();
    }

    public void showBottomSheetDialog() {

        BottomSheetDialog dialog = new BottomSheetDialog(PlayingSongActivity.this, R.style.AppBottomSheetDialogTheme);
        dialog.setContentView(R.layout.bottom_sheet_dialog);
        dialog.setCancelable(true);

        RelativeLayout rlCreateNewPlaylist = dialog.findViewById(R.id.rlCreateNewPlaylist);
        RelativeLayout rlFavouriteLayout = dialog.findViewById(R.id.rlFavouriteLayout);
        RecyclerView rvNewCreatedPlaylist = dialog.findViewById(R.id.rvNewCreatedPlaylist);
        TextView tvFavPlaylist = dialog.findViewById(R.id.tvFavPlaylist);

        Cursor checkUser = DB.getFavouritesFromId(songsList.get(position).getName());
        if(checkUser.getCount() > 0)
        {
            tvFavPlaylist.setText("UnFavourites");
        }
        else
        {
            tvFavPlaylist.setText("Favourites");
        }

        rlFavouriteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor checkUser = DB.getFavouritesFromId(songsList.get(position).getName());

                if(checkUser.getCount() <= 0) {
                    boolean checkAdd = DB.addToFavourites(songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songsList.get(position).getAlbumArt().toString());
                    if(checkAdd)
                    {

                        Toast.makeText(PlayingSongActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                }
                else
                {

                    boolean deleteFav = DB.removeFromFavourites(songTitle);
                    if(deleteFav) {
                        Toast.makeText(PlayingSongActivity.this, "Removed from Favourites", Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            }
        });

        rlCreateNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreatePlaylistDialog();
                dialog.dismiss();
            }
        });

        Cursor c = DB.getAllPlaylist();
        createdPlaylist.clear();
        if(c.getCount() > 0)
        {
            while (c.moveToNext())
            {
                createdPlaylist.add(c.getString(c.getColumnIndex("name")));
            }
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        RecyclerView.LayoutManager layoutManager = linearLayoutManager;
        rvNewCreatedPlaylist.setLayoutManager(layoutManager);

        createdPlaylistAdapter = new CreatedPlaylistAdapter(this, createdPlaylist, new CreatedPlaylistAdapter.addToPlaylistInterface() {
            @Override
            public void addToPlaylist(int pos, String playlistName) {
                Cursor checkUser = DB.getAlreadyAddedSongs(songsList.get(position).getName(), playlistName);

                if(checkUser.getCount() <= 0) {
                    boolean checkAdd = DB.insetIntoCreatedTable(playlistName, songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songsList.get(position).getAlbumArt().toString());
                    if(checkAdd)
                    {
                        Toast.makeText(PlayingSongActivity.this, "Added to Playlist", Toast.LENGTH_SHORT).show();
                        //ivFavouritesSong.setColorFilter(ContextCompat.getColor(PlayingSongActivity.this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                }
                else
                {
                    Toast.makeText(PlayingSongActivity.this, "Added to Favourites", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        rvNewCreatedPlaylist.setAdapter(createdPlaylistAdapter);

        dialog.show();

    }

    public void showCreatePlaylistDialog()
    {
        Dialog dialog = new Dialog(PlayingSongActivity.this);
        dialog.setContentView(R.layout.new_playlist_name_dialog);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView tvCreate = dialog.findViewById(R.id.tvCreate);
        TextView tvCancel = dialog.findViewById(R.id.tvCancel);
        EditText edtNewPlaylist = dialog.findViewById(R.id.edtPlaylistName);

        edtNewPlaylist.setFocusable(true);

        tvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.createPlaylist(edtNewPlaylist.getText().toString());
                DB.createTable(edtNewPlaylist.getText().toString());
                DB.insetIntoCreatedTable(edtNewPlaylist.getText().toString(), songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songsList.get(position).getAlbumArt().toString());
                Toast.makeText(PlayingSongActivity.this, "Playlist created", Toast.LENGTH_SHORT).show();
                InputMethodManager inputMethodManager = (InputMethodManager) PlayingSongActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dialog.dismiss();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                InputMethodManager inputMethodManager = (InputMethodManager) PlayingSongActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        dialog.show();
    }

    public void  playSong(int songIndex) {

        songsList = new ArrayList<>();
        if(activityName.equals("Album"))
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
        }

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

        //showNotification();
        /*Uri albumArtUri = songsList.get(songIndex).getAlbumArt();
        bitmap = null;
        //ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), albumArtUri);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), albumArtUri);
            // bitmap = ImageDecoder.decodeBitmap(source);
        } catch (FileNotFoundException exception) {
        } catch (IOException e) {

            e.printStackTrace();
        }

        if(tvPlayingSongArtist != null && tvPlayingSongName != null && ivPlayingSongImage != null) {
            tvPlayingSongArtist.setText(songsList.get(songIndex).getArtist());
            tvPlayingSongName.setText(songsList.get(songIndex).getName());
            pos = songIndex;
            actName = activityName;
            if(bitmap != null) {
                ivPlayingSongImage.setImageBitmap(bitmap);
            }
            else
            {
                ivPlayingSongImage.setImageResource(R.drawable.music_photo);
            }
        }

        songTitle = songsList.get(songIndex).getName();
        songPath = songsList.get(songIndex).getPath();
        songAlbum = songsList.get(songIndex).getAlbum();
        songId = songsList.get(songIndex).getId();
        songDuration = songsList.get(songIndex).getDuration();
        songAlbumArt = songsList.get(songIndex).getAlbumArt();
        artistName = songsList.get(songIndex).getArtist();

        tvSongName.setText(songTitle);

        if(bitmap != null) {
            ivSongImage.setImageBitmap(bitmap);
        }
        else
        {
            ivSongImage.setImageResource(R.drawable.music_photo);
        }
        currentSongIndex = songIndex;

        tvArtistName.setText(artistName);

        ivPlayPause.setImageResource(R.drawable.pause);

        seekBar.setProgress(0);
        seekBar.setMax(100);

        Cursor checkFavUser = DB.getFavouritesFromId(songsList.get(position).getName());
        if(checkFavUser.getCount() > 0)
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.SelectedTab), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else
        {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(this, R.color.countTxt), android.graphics.PorterDuff.Mode.MULTIPLY);
        }

        Cursor checkUser = DB.getRecentSongFromId(songsList.get(songIndex).getId());

        if(checkUser.getCount() == 0) {
            boolean checkAdd = DB.insertRecentSongs(songsList.get(songIndex).getPath(), songTitle, artistName, songsList.get(songIndex).getAlbum(), songsList.get(songIndex).getDuration(), songsList.get(songIndex).getId(), songsList.get(songIndex).getAlbumArt().toString());
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Name", songTitle);
        editor.putString("Artist", artistName);
       // editor.putString("Duration", String.valueOf(musicService.getDuration()));
        editor.putString("ActivityName", activityName);
        editor.putInt("Position", songIndex);
        editor.putString("AlbumArt", songsList.get(songIndex).getAlbumArt().toString());
        editor.apply();*/
      //  showNotification();
       // musicService.onComplete();
        //updateProgressBar();
    }

    /*public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = musicService.getDuration();
            long currentDuration = musicService.getCurrentPosition();

            tvDuration.setText(""+utils.milliSecondsToTimer(totalDuration));

            tvSongPlayedTime.setText(""+utils.milliSecondsToTimer(currentDuration));

            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            seekBar.setProgress(progress);
            musicService.onComplete();
            mHandler.postDelayed(this, 100);
        }
    };*/

  //  @Override
   /* public void finish() {
        super.finish();

        overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
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
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
            ivPlayPause.setImageResource(R.drawable.play);
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
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
            ivPlayPause.setImageResource(R.drawable.pause);
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
            ivPlayPause.setImageResource(R.drawable.pause);
            musicService.start();
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
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
            ivPlayPause.setImageResource(R.drawable.pause);
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
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
            ivPlayPause.setImageResource(R.drawable.pause);
            musicService.start();
            musicService.showNotification(R.drawable.ic_baseline_pause_24);
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
            ivPlayPause.setImageResource(R.drawable.pause);
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder)iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
       // Toast.makeText(this, "Connected :" + musicService, Toast.LENGTH_LONG).show();
        musicService.showNotification(R.drawable.ic_baseline_pause_24);
        musicService.onComplete();
        FIRST_OPEN = false;
        if(mediaPlayer.isPlaying())
        {
            ivPlayPause.setImageResource(R.drawable.pause);
        }
        else
        {
            ivPlayPause.setImageResource(R.drawable.play);
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