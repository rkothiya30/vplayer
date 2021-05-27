package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.FragmentAdapter;
import com.example.vplayer.fragment.adapter.MusicAdapter;
import com.example.vplayer.fragment.adapter.MusicSAdapter;
import com.example.vplayer.fragment.adapter.PlayListAdapter;
import com.example.vplayer.fragment.adapter.VideoSAdapter;
import com.example.vplayer.fragment.event.UpdateAdapterEvent;
import com.example.vplayer.fragment.event.UpdateContinueWatchingEvent;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.HashMapModel;
import com.example.vplayer.model.HistoryVideo;
import com.example.vplayer.model.PlayListModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.MusicDataService;
import com.example.vplayer.ui.fragment.MusicFragment;
import com.example.vplayer.ui.fragment.MusicSFragment;
import com.example.vplayer.ui.fragment.PlaylistFragment;
import com.example.vplayer.ui.fragment.VideoSFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.vplayer.ui.fragment.PlaylistFragment.allPlaylist;
import static com.example.vplayer.ui.fragment.PlaylistFragment.tempPlayListName;

public class SelectItemActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    ArrayList<Fragment> fragments;
    ImageView iv_back, iv_true;
    public static TextView text_title;
    PreferencesUtility preferencesUtility;
    public static int SelectCount = 0;

    public static LinkedHashMap<String, String> playlist = new LinkedHashMap<>();
    public static ArrayList<String> playlistItems = new ArrayList<>();
    Toolbar toolbar;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getSupportActionBar().hide();

        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);
        iv_back = findViewById(R.id.iv_back);
        text_title = findViewById(R.id.text_title);
        iv_true = findViewById(R.id.iv_true);
        fragments =new ArrayList<>();

        preferencesUtility = PreferencesUtility.getInstance(SelectItemActivity.this);

        fragments.add(new MusicSFragment());
        fragments.add(new VideoSFragment());



        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager(), getApplicationContext(), fragments);
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("MUSIC");
        tabLayout.getTabAt(1).setText("VIDEO");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        iv_true.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Video> tempVideos = new ArrayList<>();
                ArrayList<AudioModel> tempAudios = new ArrayList<>();

                for(int i = 0; i<MusicDataService.audioList.size(); i++)
                {
                    if(MusicDataService.audioList.get(i).isSelected()){
                        tempAudios.add(MusicDataService.audioList.get(i));
                    }
                }
                for(int j=0; j< VideoSAdapter.videoList.size(); j++){
                    if(VideoSAdapter.videoList.get(j).isSelected()){
                        tempVideos.add(VideoSAdapter.videoList.get(j));
                    }
                }

                PlayListModel playListModel = new PlayListModel(); // new PlayListModel(tempAudios, tempVideos);
                String playListString = ""; //new Gson().toJson(playListModel);


                List<Video> videoList = new ArrayList<>();
                List<AudioModel> audioModels = new ArrayList<>();

                LinkedHashMap<String, String> playlists = preferencesUtility.getPlaylists();
                if(playlists.containsKey(tempPlayListName)){
                    String s = playlists.get(tempPlayListName);
                    PlayListModel playListModel1 = new Gson().fromJson(s, PlayListModel.class);
                    videoList = playListModel1.getVideoList();
                    audioModels = playListModel1.getAudioList();
                    for(int ik = 0; ik<tempVideos.size(); ik++){
                        if(!videoList.contains(tempVideos.get(ik)))
                        videoList.add(tempVideos.get(ik));
                    }
                    for(int ik = 0; ik<tempAudios.size(); ik++){
                        if(!audioModels.contains(tempAudios.get(ik)))
                            audioModels.add(tempAudios.get(ik));
                    }


                    playListModel = new PlayListModel(audioModels, videoList);
                    playListString = new Gson().toJson(playListModel);
                }else {

                   playListModel =new PlayListModel(tempAudios, tempVideos);
                  playListString =new Gson().toJson(playListModel);

                }

                allPlaylist.put(tempPlayListName, playListString);
                preferencesUtility.setPlaylists(allPlaylist);

                onBackPressed();

                /*PlaylistFragment.playListAdapter = new PlayListAdapter(getApplicationContext(), preferencesUtility.getPlaylists());
                PlaylistFragment.videoLList.setAdapter(PlaylistFragment.playListAdapter);*/
                RxBus.getInstance().post(new UpdateAdapterEvent());






            }
        });

    }

    @Override
    public void onBackPressed() {
        MusicSAdapter.Selection = false;
        for(int i = 0; i< MusicDataService.audioList.size(); i++)
        {
            MusicDataService.audioList.get(i).setSelected(false);
        }
        for(int j=0; j< VideoSAdapter.videoList.size(); j++){
            VideoSAdapter.videoList.get(j).setSelected(false);
        }

        MusicFragment.adapter.notifyDataSetChanged();
        super.onBackPressed();
    }
}