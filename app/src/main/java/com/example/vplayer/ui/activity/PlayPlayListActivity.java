package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.MusicSAdapter;
import com.example.vplayer.fragment.adapter.PlayListItemAdapter;
import com.example.vplayer.fragment.event.UpdateAdapterEvent;
import com.example.vplayer.fragment.event.UpdateVideoList;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.HistoryVideo;
import com.example.vplayer.model.PlayListModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.fragment.PlaylistFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.example.vplayer.ui.fragment.PlaylistFragment.allPlaylist;

public class PlayPlayListActivity extends AppCompatActivity {

    RecyclerView recycler_view;
    TextView emptyString;
    public static PlayListItemAdapter adapter;
    TextView text_title, play_mun;
    ImageView iv_back, iv_add;
    AppCompatImageView iv_music;
    String PlayName;
    PreferencesUtility preferencesUtility;
    public static List<Object> aList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_play_list);
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

        recycler_view = findViewById(R.id.recycler_view);
        emptyString = findViewById(R.id.emptyString);
        text_title = findViewById(R.id.text_title);
        iv_back = findViewById(R.id.iv_back);
        iv_music = findViewById(R.id.iv_music);
        play_mun = findViewById(R.id.play_mun);
        iv_add = findViewById(R.id.iv_add);
        preferencesUtility = PreferencesUtility.getInstance(PlayPlayListActivity.this);

        initView();
        subscribeUpdateAdapterEvent();

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaylistFragment.tempPlayListName = PlayName;
                startActivity(new Intent(PlayPlayListActivity.this, SelectItemActivity.class));
            }
        });
    }

    public void initView() {
        //progress_bar.setVisibility(View.VISIBLE);

                setAdapter();

    }

    public void setAdapter() {
        //progress_bar.setVisibility(View.GONE);

        recycler_view.setVisibility(View.VISIBLE);
        emptyString.setVisibility(View.GONE);

        int position = getIntent().getExtras().getInt("Position");
        PlayName = getIntent().getStringExtra("PlayName");

        String s = allPlaylist.get(PlayName);

        aList.clear();
        PlayListModel playListModel = new PlayListModel();
        if (s != null) {
            playListModel = new Gson().fromJson(s, PlayListModel.class);
            if (playListModel != null)
                if(playListModel.getVideoList() == null){
                    List<Video> temp = new ArrayList<>();
                    aList.add(temp);
                }else{
                    aList.addAll(playListModel.getVideoList());
                }

            if (playListModel != null)
            if(playListModel.getAudioList() == null){
                List<AudioModel> temp = new ArrayList<>();
                aList.add(temp);
            }else{
                aList.addAll(playListModel.getAudioList());
            }



        }

        text_title.setText(PlayName);
        play_mun.setText(String.valueOf(aList.size()));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layoutManager);
        adapter = new PlayListItemAdapter(this, (ArrayList<Object>) aList, true);
        recycler_view.setAdapter(adapter);

        adapter.setOnItemClickListener(new PlayListItemAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void subscribeUpdateAdapterEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(UpdateAdapterEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(new Action1<UpdateAdapterEvent>() {
                    @Override
                    public void call(UpdateAdapterEvent event) {
                        //videoList.remove(event.getPosition());
                        setAdapter();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }
}