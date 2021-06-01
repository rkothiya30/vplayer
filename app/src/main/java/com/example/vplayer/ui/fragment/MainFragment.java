package com.example.vplayer.ui.fragment;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.ActionPlaying;
import com.example.vplayer.fragment.utils.DbHelper;
import com.example.vplayer.fragment.utils.Utilities;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.service.MusicDataService;
import com.example.vplayer.service.MusicService;
import com.example.vplayer.ui.activity.PlayingSongActivity;
import com.example.vplayer.ui.activity.VideoPlayerActivity;
import com.google.android.material.tabs.TabLayout;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.example.vplayer.fragment.adapter.PlayListItemAdapter.aList;
import static com.example.vplayer.service.MusicDataService.audioList;
import static com.example.vplayer.service.MusicService.mediaPlayer;

public class MainFragment extends Fragment implements ActionPlaying, ServiceConnection {

    private static final int PAGE_COUNT = 3;

    View view;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    TabLayout tabLayout;
    
    View videoView, musicView, playlistView;
    TextView videoText, musicText, playlistText;
    ImageView videoImage, musicImage, playlistImage;

    static SlidingUpPanelLayout sliding_layout;
    static LinearLayout ll_root;
    RelativeLayout rl_slide, playing_song_layout;

    private static boolean FIRST_OPEN = false;
    private ImageView ivBAck, ivFavouritesSong, ivAddToPlaylist, ivShuffleSong;
    private String songTitle, artistName, songPath, songAlbum, songDuration, songId;
    private Uri songAlbumArt;
    private boolean isJustOpen = false;
    public static int position, currentSongIndex;
    private CircularImageView ivSongImage, civDiskPhoto;
    private TextView tvSongName, tvArtistName, tvDuration, tvSongPlayedTime;
    // public static MediaPlayer mp;
    public static ImageView ivPlayPause;
    private ImageView ivPrevious, ivNext;
    private ImageView ivShareSong;
    private AppCompatSeekBar seekBar;
    public static int position1 = 0 ;
    public static String ActivityName = "";

    Handler mHandler;
    private Utilities utils;
    public static List<AudioModel> songsList = new ArrayList<>();
    public static List<AudioModel> shuffledList = new ArrayList<>();
    private List<AudioModel> recentList;
    private SharedPreferences pref;
    SharedPreferences.Editor editor;
    DbHelper DB;
    public static List<String> createdPlaylist;
    Animation rotation;
    private ObjectAnimator anim;


    private Intent playIntent;
    private boolean musicBound = false;
    Bitmap bitmap = null;

    public static MusicService musicService;
    public static String activityName;
    public Uri uri;
    private Handler handler = new Handler();
    public static boolean IS_SHUFFLED = false;
    public static Intent intent1;
    private String currentActivityName;
    ImageView playPause, iv_close;

    AppCompatTextView txt_authors, txt_music_name;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*return super.onCreateView(inflater, container, savedInstanceState);*/
        view = inflater.inflate( R.layout.fragment_main, container, false);
        
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        sliding_layout = view.findViewById(R.id.sliding_layout);
        ll_root = (LinearLayout) view.findViewById(R.id.ll_root);
        rl_slide = view.findViewById(R.id.rl_slide);
        playing_song_layout = view.findViewById(R.id.playing_song_layout);

        MainFragment.sliding_layout.setPanelHeight(0);
        MainFragment.sliding_layout.addPanelSlideListener(onSlideListener());

        createdPlaylist = new ArrayList<>();

        mHandler = new Handler();
        utils = new Utilities();
        DB = new DbHelper(getActivity());

        ivBAck = view.findViewById(R.id.ivBackSong);
        ivSongImage = view.findViewById(R.id.civSongPhoto);
        civDiskPhoto = view.findViewById(R.id.civDiskPhoto);
        tvSongName =view. findViewById(R.id.tvSongName);
        tvArtistName = view.findViewById(R.id.tvArtistName);
        tvDuration = view.findViewById(R.id.tvSongTotalTime);
        tvSongPlayedTime = view.findViewById(R.id.tvSongPlayedTime);
        ivPlayPause = view.findViewById(R.id.ivPlayPauseButton);
        ivPrevious = view.findViewById(R.id.ivPreviousSong);
        ivNext = view.findViewById(R.id.ivNextSong);
        seekBar = view.findViewById(R.id.sbSongDuration);
        ivFavouritesSong = view.findViewById(R.id.ivFavouritesSong);
        ivAddToPlaylist = view.findViewById(R.id.ivAddToPlaylist);
        ivShareSong = view.findViewById(R.id.ivShareSong);

        ivShuffleSong = view.findViewById(R.id.ivShuffleSong);
        playPause = view.findViewById(R.id.playPause);
        iv_close = view.findViewById(R.id.iv_close);
        txt_authors = view.findViewById(R.id.txt_authors);
        txt_music_name = view.findViewById(R.id.txt_music_name);

        //ivSearch = findViewById(R.id.ivSearch);

        pref = getActivity().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        editor = pref.edit();

        currentSongIndex = pref.getInt("Position", 0);
        currentActivityName = pref.getString("ActivityName", "");

        view.setFocusableInTouchMode(true);
        view.requestFocus();



                return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();

        new Thread(this::getIntentMethod).start();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if(sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)
                        {

                            MainFragment.sliding_layout.setPanelHeight(0);
                            musicService.pause();
                            musicService.showNotification(R.drawable.ic_play);

                            ivPlayPause.setImageResource(R.drawable.ic_play_outlined);
                            playPause.setImageResource(R.drawable.ic_play);
                            seekBar.setMax(musicService.getDuration() / 1000);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (musicService != null) {
                                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                                        seekBar.setProgress(mCurrentPosition);
                                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                                        txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                                    }
                                    handler.postDelayed(this, 100);
                                }
                            });
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        ivBAck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
                if(sliding_layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
                {

                    sliding_layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);



                    //MainFragment.sliding_layout.setPanelHeight(200);
                  tabLayout.setVisibility(View.GONE);
                    ll_root.setVisibility(View.VISIBLE);
                }




                //getActivity().finish();
            }
        });
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainFragment.sliding_layout.setPanelHeight(0);
                musicService.pause();
                musicService.showNotification(R.drawable.ic_play);

                ivPlayPause.setImageResource(R.drawable.ic_play_outlined);
                playPause.setImageResource(R.drawable.ic_play);
                seekBar.setMax(musicService.getDuration() / 1000);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPosition);
                            tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                            txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                        }
                        handler.postDelayed(this, 100);
                    }
                });


            }
        });

        ivShareSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoPlayerUtils.shareAudio(songsList.get(position).getId(), getActivity());
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
                if (!IS_SHUFFLED) {
                    shuffledList = songsList;
                    Collections.shuffle(shuffledList);
                    IS_SHUFFLED = true;
                    Toast.makeText(getActivity(), "Songs Shuffled", Toast.LENGTH_SHORT).show();
                    ivShuffleSong.setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.SRC_IN);
                } else {
                    ivShuffleSong.setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.SRC_IN);
                    IS_SHUFFLED = false;
                    Toast.makeText(getActivity(), "Playing in original sequence", Toast.LENGTH_SHORT).show();
                }
            }
        });


        ivFavouritesSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor checkUser = DB.getFavouritesFromId(songsList.get(position).getName());

                if (checkUser.getCount() <= 0) {
                    boolean checkAdd = DB.addToFavourites(songPath, songTitle, artistName, songAlbum, songDuration, songId, songAlbumArt.toString());
                    if (checkAdd) {
                        Toast.makeText(getActivity(), "Added to Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(getActivity(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    boolean checkDelFav = DB.removeFromFavourites(songsList.get(position).getName());
                    if (checkDelFav) {
                        Toast.makeText(getActivity(), "Remove from Favourites", Toast.LENGTH_SHORT).show();
                        ivFavouritesSong.setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.SRC_IN);
                    }
                }
            }
        });

        ivAddToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showShortDialog(-3, audioList.get(position));
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
                if (musicService != null && b) {
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

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    tvSongPlayedTime.setText(formattedTime(mCurrentPosition));
                    //txt_authors.setText(formattedTime(mCurrentPosition).toString() + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());
                    tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());
                }
                handler.postDelayed(this, 1000);
            }
        });
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
    }

    public void initView() {

        pagerAdapter = new PagerAdapter(getChildFragmentManager(), 0);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_COUNT);
//        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0);

        videoView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null);
        videoImage = videoView.findViewById(R.id.tabIcon);
        videoText = videoView.findViewById(R.id.tabTitle);

        videoImage.setImageResource(R.drawable.ic_grid_folder);
        videoText.setText(R.string.title_video);
        videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
        videoText.setTextColor(getResources().getColor(R.color.tab_selected_color));
        tabLayout.addTab(tabLayout.newTab().setCustomView(videoView));

        musicView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null);
        musicImage = musicView.findViewById(R.id.tabIcon);
        musicText = musicView.findViewById(R.id.tabTitle);
        musicImage.setImageResource(R.drawable.ic_baseline_queue_music_24_bordered);
        musicText.setText(R.string.title_music);
        musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
        musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
        tabLayout.addTab(tabLayout.newTab().setCustomView(musicView));

        playlistView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null);
        playlistImage = playlistView.findViewById(R.id.tabIcon);
        playlistText = playlistView.findViewById(R.id.tabTitle);
        playlistImage.setImageResource(R.drawable.ic_baseline_playlist_play_24_bordered);
        playlistText.setText(R.string.title_playlist);
        playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
        playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
        tabLayout.addTab(tabLayout.newTab().setCustomView(playlistView));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0) {
                    videoImage.setImageResource(R.drawable.ic_grid_folder);
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else if (tab.getPosition() == 1) {
                    musicImage.setImageResource(R.drawable.ic_baseline_queue_music_24);
                    musicImage.setColorFilter(
                            ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else {
                    playlistImage.setImageResource(R.drawable.ic_baseline_playlist_play_24);
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    videoImage.setImageResource(R.drawable.ic_grid_folder_bordered);
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else if (tab.getPosition() == 1) {
                    musicImage.setImageResource(R.drawable.ic_baseline_queue_music_24_bordered);
                    musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else {
                    playlistImage.setImageResource(R.drawable.ic_baseline_playlist_play_24_bordered);
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                    getActivity().setTitle("Folders");
                   /* musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));*/
                } else if (i == 1) {
                    musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                    getActivity().setTitle("Music");
                   /* videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));*/
                } else {
                  /*  musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));*/
                    getActivity().setTitle("Playlists");
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
                //textView.setText("panel is sliding");

                //  ll_root.setVisibility(View.GONE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    ll_root.setVisibility(View.GONE);
                    sliding_layout.setTouchEnabled(false);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.theme_grey, getActivity().getTheme()));
                        getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.black));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.black));
                        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.theme_grey));
                    }
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                    new Handler(Looper.myLooper() ).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tabLayout.setVisibility(View.GONE);
                        }
                    }, 500);

                    //here you can set the visibility of the panel you want to hide to GONE
                } else {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                    ll_root.setVisibility(View.VISIBLE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.tab_selected_color, getActivity().getTheme()));
                        getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.black));
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getContext(), R.color.black));
                        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.tab_selected_color));
                    }
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    //and here you would set the panel to VISIBLE again

                    new Handler(Looper.myLooper() ).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tabLayout.setVisibility(View.VISIBLE);
                        }
                    }, 500);


                }
            }



        };

    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private static final int ITEM_VIDEO = 0;
        private static final int ITEM_FOLDER = 1;
        private static final int ITEM_HISTORY = 2;

        public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case ITEM_VIDEO:
                    fragment = new VideoFragment();
                    break;
                case ITEM_FOLDER:
                    fragment = new MusicFragment();
                    break;
                case ITEM_HISTORY:
                    fragment = new PlaylistFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

//        @Nullable
//        @Override
//        public CharSequence getPageTitle(int position) {
//            String title = "";
//            switch (position) {
//                case ITEM_VIDEO:
//                    title = getString(R.string.title_video);
//                    break;
//                case ITEM_FOLDER:
//                    title = getString(R.string.title_folder);
//                    break;
//                case ITEM_HISTORY:
//                    title = getString(R.string.title_history);
//                    break;
//                default:
//                    break;
//            }
//            return title;
//        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }


    }

     private String formattedTime(int mCurrentPosition) {

        String totalOut = "a";
        String totalNew = "a";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if (seconds.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }


    public void getIntentMethod() {

        while (true) {
            if (MusicDataService.isComplete) {
                break;
            }
        }
        intent1 = getActivity().getIntent();

        activityName = ActivityName;/*tygetIntent().getExtras().getString("ActivityName", "");*/

        if (activityName.equals("MusicFragment")) {
            songsList = audioList;
            position = position1;
        }/* else if (activityName.equals("PlayPlayListActivity")) {
            List<AudioModel> a = new ArrayList<>();
            a = Parcels.unwrap(getIntent().getParcelableExtra("Audio"));

            //   a.add(audio1);
            songsList = a;
            position = getIntent().getIntExtra("Position", -1);
        } else if (activityName.equals("PlayListItemAdapter")) {
            List<AudioModel> a = new ArrayList<>();
            a = Parcels.unwrap(getIntent().getParcelableExtra("Audio"));

            //   a.add(audio1);
            songsList = a;
            position = getIntent().getIntExtra("Position", -1);
        } else if (activityName.equals("OnMenuFragment")) {
            songsList = audioList;
        } else if (activityName.equals("MusicSearch")) {
            songsList = audioList;
            position =0;
        } */else {
            songsList = audioList;
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

        if (songsList != null) {
            uri = Uri.parse(songsList.get(position).getPath());
        }

        if (currentSongIndex == position) //&& FIRST_OPEN)
        {
            Intent intent = new Intent(getActivity(), MusicService.class);
            intent.putExtra("ServicePosition", position);
            getActivity().startService(intent);
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
        } else if (currentSongIndex != position) {
            Intent intent = new Intent(getActivity(), MusicService.class);
            intent.putExtra("ServicePosition", position);
            getActivity().startService(intent);
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
        }
        /*else if(!currentActivityName.equals(activityName))
        {
            Intent intent = new Intent(this, MusicService.class);
            intent.putExtra("ServicePosition", position);
            startService(intent);
            ivPlayPause.setImageResource(R.drawable.pause);
        }*/
        setBottomLayout();
        if (musicService != null) {
            musicService.showNotification(R.drawable.ic_pause);
        }


    }

    public void setBottomLayout() {
        try {
            tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));


        } catch(Exception e){
            e.printStackTrace();
        }
        AudioModel audioModel = songsList.get(position);
        File f = new File(audioModel.getPath());
        Uri albumArtUri = Uri.fromFile(f);
        bitmap = null;
        //ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), albumArtUri);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), albumArtUri);
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
        txt_music_name.setText(songTitle);

        if (bitmap != null) {
            ivSongImage.setImageBitmap(bitmap);
        } else {
            ivSongImage.setImageResource(R.drawable.ic_music_icon_round);
        }
        rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new LinearInterpolator());
        civDiskPhoto.startAnimation(rotation);
        rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new LinearInterpolator());
        ivSongImage.startAnimation(rotation);


        /*anim = ObjectAnimator.ofFloat(ivPlayPause, "rotation", 0, 360);
        anim.setDuration(1000);
        anim.setRepeatCount(5);
        anim.setRepeatMode(ObjectAnimator.RESTART);*/

        currentSongIndex = position;

        tvArtistName.setText(artistName);

        //ivPlayPause.setImageResource(R.drawable.pause);

        seekBar.setProgress(0);
        seekBar.setMax(100);

        Cursor checkFavUser = DB.getFavouritesFromId(songsList.get(position).getName());
        if (checkFavUser.getCount() > 0) {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(getActivity(), R.color.white), PorterDuff.Mode.SRC_IN);
        } else {
            ivFavouritesSong.setColorFilter(ContextCompat.getColor(getActivity(), R.color.md_green_500), PorterDuff.Mode.SRC_IN);
        }

        Cursor checkUser = DB.getRecentSongFromId(songsList.get(position).getId());

        if (checkUser.getCount() == 0) {
            boolean checkAdd = DB.insertRecentSongs(songsList.get(position).getPath(), songTitle, artistName, songsList.get(position).getAlbum(), songsList.get(position).getDuration(), songsList.get(position).getId(), songAlbumArt.toString());
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Name", songTitle);
        editor.putString("Artist", artistName);
        // editor.putString("Duration", String.valueOf(musicService.getDuration()));
        editor.putString("ActivityName", activityName);
        editor.putInt("Position", position);
        editor.putString("AlbumArt", songAlbumArt.toString());
        editor.apply();
    }



    public void showShortDialog ( int adapterPosition, AudioModel audioModel){
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance(adapterPosition, audioModel, getActivity());
        //bottomSheetDialog.setOuterClickListener(this);
        addPlaylistFragment.show(getFragmentManager(), "Bottom Sheet Dialog Fragment");
    }


    public void playSong(int songIndex) {

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

        if (currentSongIndex != songIndex) {
            // musicService.reset();

            if (mediaPlayer != null) {
                musicService.stop();
                musicService.release();
                musicService.createMediaPlayer(songIndex);
                musicService.start();
            } else {
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


    public void onBackPressed() {

        //overridePendingTransition( R.anim.slide_in_down, R.anim.slide_out_down );
        getActivity().finish();
    }

    public void afterSongCompletion() {
        if (currentSongIndex < ( songsList.size() - 1 )) {
            playSong(currentSongIndex + 1);
            currentSongIndex = currentSongIndex + 1;
        } else {
            if (songsList.size() > 0) {
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

            Intent intent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(intent, this, BIND_AUTO_CREATE);
            super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void prevThreadBtn() {
        ivPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousButtonClick();
            }
        });
    }

    private void nextThreadBtn() {
        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButtonClick();
            }
        });
    }

    private void playThreadBtn() {
        ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseButtonClick();
            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseButtonClick();
            }
        });
    }

    public void playPauseButtonClick() {


        if (musicService.isPlaying()) {
            musicService.pause();
            musicService.showNotification(R.drawable.ic_play);

            ivPlayPause.setImageResource(R.drawable.ic_play_outlined);
            playPause.setImageResource(R.drawable.ic_play);
            seekBar.setMax(musicService.getDuration() / 1000);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                        txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                    }
                    handler.postDelayed(this, 100);
                }
            });
        } else {
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause);
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
            playPause.setImageResource(R.drawable.ic_pause);
            seekBar.setMax(musicService.getDuration() / 1000);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                        txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                    }
                    handler.postDelayed(this, 100);
                }
            });
        }
        //  musicService.onComplete();
    }


    public void nextButtonClick() {
        boolean temp = false;
        if (musicService.isPlaying() && musicService != null) {

            musicService.stop();
            musicService.release();

            if (activityName.equals("PlayListItemAdapter")) {

                if (position >= ( songsList.size() - 1 )) {

                    if (activityName.equals("PlayListItemAdapter")) {
                        //position = getIntent().getIntExtra("Position", -1);

                        temp = true;

                           musicService.stopForeground(true);
                        musicService.onComplete();


                        musicService = null;


                        startActivity(VideoPlayerActivity.getIntent(getActivity(), aList, 0, "PlayListItemAdapter"));

                        getActivity().finish();
                    }
                } else {


                    position++;

                    musicService.createMediaPlayer(position);


                }
            } else {
                position = ( ( position + 1 ) % songsList.size() );


                musicService.createMediaPlayer(position);
            }


            if (!temp) {
                setBottomLayout();
                seekBar.setMax(musicService.getDuration() / 1000);
            }

            if (!temp) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicService != null) {
                            int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                            seekBar.setProgress(mCurrentPosition);
                            tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                            txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                        }
                        if (!getActivity().isFinishing()) {

                            handler.postDelayed(this, 100);
                        }
                    }
                });

                musicService.onComplete();
                ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
                playPause.setImageResource(R.drawable.ic_pause);
                musicService.start();
                musicService.showNotification(R.drawable.ic_pause);
            }

        } else {
            musicService.stop();
            musicService.release();
            position = ( ( position + 1 ) % songsList.size() );
            musicService.createMediaPlayer(position);
            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                        txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
            ivPlayPause.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
        }
    }

    public void previousButtonClick() {
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

        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();

            if (activityName.equals("PlayListItemAdapter")) {
                musicService.createMediaPlayer(position);
                if (position == 0) {
                    getActivity().finish();
                    musicService.stopForeground(true);
                    startActivity(VideoPlayerActivity.getIntent(getActivity(), aList, songsList.size() - 1, "PlayListItemAdapter"));
                    musicService = null;
                } else {
                    position = ( ( position - 1 ) < 0 ? ( songsList.size() - 1 ) : ( position - 1 ) );
                    musicService.createMediaPlayer(position);

                    setBottomLayout();
                    seekBar.setMax(musicService.getDuration() / 1000);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (musicService != null) {
                                int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                                seekBar.setProgress(mCurrentPosition);
                                tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                                txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                            }
                            handler.postDelayed(this, 100);
                        }
                    });
                    musicService.onComplete();
                    ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
                    playPause.setImageResource(R.drawable.ic_pause);
                    musicService.start();
                    musicService.showNotification(R.drawable.ic_pause);
                }
            } else {
                position = ( ( position - 1 ) < 0 ? ( songsList.size() - 1 ) : ( position - 1 ) );
                musicService.createMediaPlayer(position);

            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                        txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
                playPause.setImageResource(R.drawable.ic_pause);
            musicService.start();
            musicService.showNotification(R.drawable.ic_pause);
        }
        } else {
            musicService.stop();
            musicService.release();
            position = ( ( position - 1 ) < 0 ? ( songsList.size() - 1 ) : ( position - 1 ) );
            musicService.createMediaPlayer(position);
            setBottomLayout();
            seekBar.setMax(musicService.getDuration() / 1000);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                        tvDuration.setText(formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000));
                        txt_authors.setText(formattedTime(mCurrentPosition) + " / "+ formattedTime(Integer.parseInt(songsList.get(position).getDuration()) / 1000).toString());

                    }
                    handler.postDelayed(this, 100);
                }
            });
            musicService.onComplete();
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
            playPause.setImageResource(R.drawable.ic_pause);
            musicService.showNotification(R.drawable.ic_pause);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        seekBar.setMax(musicService.getDuration() / 1000);
        //Toast.makeText(this, "Connected :" + musicService, Toast.LENGTH_LONG).show();
        musicService.showNotification(R.drawable.ic_pause);
        musicService.onComplete();
        FIRST_OPEN = false;
        if (mediaPlayer.isPlaying()) {
            ivPlayPause.setImageResource(R.drawable.ic_pause_outlined);
            playPause.setImageResource(R.drawable.ic_pause);
        } else {
            ivPlayPause.setImageResource(R.drawable.ic_play_outlined);
            playPause.setImageResource(R.drawable.ic_play);
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

}
