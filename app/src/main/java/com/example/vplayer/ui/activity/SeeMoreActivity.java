package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.SeeMoreAdapter;
import com.example.vplayer.fragment.adapter.VideoAdapter;
import com.example.vplayer.fragment.event.UpdateContinueWatchingEvent;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.Video;

import java.util.ArrayList;
import java.util.List;

public class SeeMoreActivity extends AppCompatActivity {


    RecyclerView videoList;
    TextView text_title;
    ImageView iv_back;
    AppCompatImageView iv_delete;
    public List<Video> CWVideos = new ArrayList<>();
    public PreferencesUtility preferencesUtility;
    SeeMoreAdapter seeMoreAdapter;
    boolean toggle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_more);
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

        videoList = findViewById(R.id.videoList);
        text_title = findViewById(R.id.text_title);
        iv_back = findViewById(R.id.iv_back);
        iv_delete = findViewById(R.id.iv_delete);

        preferencesUtility = PreferencesUtility.getInstance(this);
        CWVideos = preferencesUtility.getContinueWatchingVideos();

        initView();

        iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SeeMoreAdapter.selectedSet == false){
                    for (int i = 0; i < CWVideos.size(); i++) {
                        SeeMoreAdapter.selectedSet = true;

                    }
                seeMoreAdapter = new SeeMoreAdapter(SeeMoreActivity.this, CWVideos);
                    videoList.setAdapter(seeMoreAdapter);
                seeMoreAdapter.notifyDataSetChanged();
            } else{
                    int any = CWVideos.size();
                    CWVideos = SeeMoreAdapter.videoList;
                    ArrayList<Video> newList = new ArrayList<>() ;

                    for(int i = 0; i<CWVideos.size(); i++){
                        if(!CWVideos.get(i).isSelected()) {
                            newList.add(CWVideos.get(i));
                            any++;
                        }
                    }
                    CWVideos = newList;
                    if(any==CWVideos.size()){
                        for (int i = 0; i < CWVideos.size(); i++) {
                            SeeMoreAdapter.selectedSet = false;
                            CWVideos.get(i).setSelected(false);
                        }
                    } else{
                        for (int i = 0; i < CWVideos.size(); i++) {
                            SeeMoreAdapter.selectedSet = false;
                            CWVideos.get(i).setSelected(false);
                        }
                        preferencesUtility.updateContinueWatchingVideo(CWVideos);
                        RxBus.getInstance().post(new UpdateContinueWatchingEvent());
                    }
                    seeMoreAdapter = new SeeMoreAdapter(SeeMoreActivity.this, CWVideos);
                    videoList.setAdapter(seeMoreAdapter);
                    seeMoreAdapter.notifyDataSetChanged();
                    if(CWVideos.size() == 0)
                        finish();
                }
        }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

        seeMoreAdapter.setOnItemClickListener(new SeeMoreAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if(SeeMoreAdapter.selectedSet == true){
                    if(CWVideos.get(position).isSelected()){
                        CWVideos.get(position).setSelected(false);
                    } else {
                        CWVideos.get(position).setSelected(true);
                    }
                    //videoList.setAdapter(seeMoreAdapter);
                    seeMoreAdapter.notifyItemChanged(position);
                    if(v.getId() == R.id.ll_check_all){

                        if(toggle) {
                            for (int j = 0; j < CWVideos.size(); j++) {
                                CWVideos.get(j).setSelected(toggle);

                            }
                            toggle = false;
                        } else{
                            for(int j =0; j<CWVideos.size(); j++){
                                CWVideos.get(j).setSelected(toggle);

                            }
                            toggle = true;
                        }
                        videoList.setAdapter(seeMoreAdapter);
                        seeMoreAdapter.notifyDataSetChanged();
                    }
                }
                else{
                    startActivity(VideoPlayerActivity.getIntent(getApplicationContext(), preferencesUtility.getContinueWatchingVideos(), position));
                }
            }
        });
        seeMoreAdapter.setOnLongClickListener(new SeeMoreAdapter.LongClickListener() {
            @Override
            public void onItemLongClick(int position, View v) {
                if (SeeMoreAdapter.selectedSet == false){
                    SeeMoreAdapter.selectedSet = true;
                    CWVideos.get(position).setSelected(true);
                    seeMoreAdapter = new SeeMoreAdapter(SeeMoreActivity.this, CWVideos);
                    videoList.setAdapter(seeMoreAdapter);
                    seeMoreAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void initView() {

            videoList.setLayoutManager(new LinearLayoutManager(this));

        seeMoreAdapter = new SeeMoreAdapter(this, CWVideos);

        videoList.setHasFixedSize(true);
        videoList.setItemViewCacheSize(20);
        videoList.setDrawingCacheEnabled(true);
        videoList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        videoList.setAdapter(seeMoreAdapter);

    }

    @Override
    public void onBackPressed() {
        if (SeeMoreAdapter.selectedSet == true) {
            SeeMoreAdapter.selectedSet = false;
            CWVideos = SeeMoreAdapter.videoList;
            for (int j = 0; j < CWVideos.size(); j++) {
                CWVideos.get(j).setSelected(false);
                videoList.setAdapter(seeMoreAdapter);
                seeMoreAdapter.notifyDataSetChanged();
            }
        } else {
            super.onBackPressed();
        }
    }
}