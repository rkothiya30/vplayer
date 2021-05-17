package com.example.vplayer.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.VideoAdapter;
import com.example.vplayer.fragment.event.UpdateVideoList;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.google.android.material.appbar.AppBarLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.example.vplayer.service.VideoDataService.videobucketimagesDataHashMap;
import static com.example.vplayer.service.VideoDataService.videobuckets;

public class FolderInFolderActivity extends AppCompatActivity {

    String path = "";
    String folderName;
    int Position;
    Toolbar toolbar;
    RecyclerView videoList;
    List<Video> videoListList;
    VideoAdapter videoAdapter;
    TextView text_title, videoTitle, tv_size;
    ImageView iv_back;
    AppCompatImageView iv_list_grid;

    PreferencesUtility preferencesUtility;
    boolean isGrid = false;


    public static Intent getInstance(Context context, int position, String path, String folderName) {
        Intent intent = new Intent(context, FolderInFolderActivity.class);
        intent.putExtra("Position", position);
        intent.putExtra(Constant.EXTRA_FOLDER_PATH, path);
        intent.putExtra(Constant.EXTRA_FOLDER_NAME, folderName);
        return intent;
    }

    public static Intent getInstance(Context context, int position, String path) {
        Intent intent = new Intent(context, FolderInFolderActivity.class);
        intent.putExtra("Position", position);
        intent.putExtra(Constant.EXTRA_FOLDER_PATH, path);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_in_folder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        preferencesUtility = PreferencesUtility.getInstance(this);
        videoListList = new ArrayList<>();

        videoList = findViewById(R.id.videoList);
        text_title = findViewById(R.id.text_title);
        iv_back = findViewById(R.id.iv_back);
        iv_list_grid = findViewById(R.id.iv_list_grid);
        //appbar = findViewById(R.id.appbar);
        videoTitle = findViewById(R.id.videoTitle);
        tv_size = findViewById(R.id.tv_size);
        iv_list_grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGrid) {
                    isGrid = false;
                    preferencesUtility.saveToDirList_Grid(isGrid);


                    initView();

                    iv_list_grid.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));


                } else {
                    isGrid = true;
                    preferencesUtility.saveToDirList_Grid(isGrid);

                    initView();
                    iv_list_grid.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
                    // ivListGrid.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));

                }
            }
        });
        if (getIntent() != null) {
            path = getIntent().getStringExtra(Constant.EXTRA_FOLDER_PATH);
            folderName = getIntent().getStringExtra(Constant.EXTRA_FOLDER_NAME);
            Position = getIntent().getExtras().getInt("Position");
        }



        getSupportActionBar().hide();
        text_title.setText(folderName);
        File f = new File(videobucketimagesDataHashMap.get(folderName).get(0).getFullPath());
        tv_size.setText(VideoPlayerUtils.formateSize(folderSize(new File(f.getParentFile().getAbsolutePath()))));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkGray)));

        isGrid = preferencesUtility.getDirList_Grid();
        initView();

        videoAdapter.setOnItemClickListener(new VideoAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                startActivity(VideoPlayerActivity.getIntent(getApplicationContext(), videobucketimagesDataHashMap.get(folderName), position));
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        subscribeUpdateVideoListEvent();
    }


    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initView() {
        if(isGrid)
            videoList.setLayoutManager(new GridLayoutManager(getApplication(), 3));

        else
            videoList.setLayoutManager(new LinearLayoutManager(this));

        videoAdapter = new VideoAdapter(this, folderName, isGrid);

        videoList.setHasFixedSize(true);
        videoList.setItemViewCacheSize(20);
        videoList.setDrawingCacheEnabled(true);
        videoList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        videoList.setAdapter(videoAdapter);

    }

    private void subscribeUpdateVideoListEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(UpdateVideoList.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(new Action1<UpdateVideoList>() {
                    @Override
                    public void call(UpdateVideoList event) {
                        //videoList.remove(event.getPosition());
                        videoAdapter.setVideoList(videoListList);
                        videoAdapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }



}