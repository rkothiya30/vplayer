package com.example.vplayer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.VideoAdapter;
import com.example.vplayer.fragment.event.DeleteEvent;
import com.example.vplayer.fragment.event.RenameEvent;
import com.example.vplayer.fragment.event.UpdateVideoList;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.FolderInFolderActivity;
import com.example.vplayer.ui.activity.SearchActivity;
import com.example.vplayer.ui.activity.SettingsActivity;
import com.example.vplayer.ui.activity.VideoPlayerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.example.vplayer.service.VideoDataService.videobucketimagesDataHashMap;


public class FolderInFolderFragment extends Fragment {

    public static String path = "";
    public static String folderName;
    public static int Position;
    Toolbar toolbar;
    RecyclerView videoList;
    List<Video> videoListList;
    VideoAdapter videoAdapter;
    TextView text_title, videoTitle;
    ImageView iv_back;
    AppCompatImageView iv_list_grid, iv_search;

    PreferencesUtility preferencesUtility;
    boolean isGrid = false;

    public static Intent getInstance(Context context, int positio, String pat, String folderNam) {
        Intent intent = new Intent(context, FolderInFolderFragment.class);
        intent.putExtra("Position", positio);
        intent.putExtra(Constant.EXTRA_FOLDER_PATH, pat);
        intent.putExtra(Constant.EXTRA_FOLDER_NAME, folderNam);

            path =pat;
            folderName = folderNam;
            Position = positio;

        return intent;
    }

    public static Intent getInstance(Context context, int position, String path) {
        Intent intent = new Intent(context, FolderInFolderFragment.class);
        intent.putExtra("Position", position);
        intent.putExtra(Constant.EXTRA_FOLDER_PATH, path);
        return intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_folder_in_folder, container, false);
        preferencesUtility = PreferencesUtility.getInstance(getActivity());
        videoListList = new ArrayList<>();

        videoList = v.findViewById(R.id.videoList);
        text_title =(TextView)  v.findViewById(R.id.text_title);
        iv_back = v.findViewById(R.id.iv_back);
        iv_list_grid = ( AppCompatImageView)v.findViewById(R.id.iv_list_grid);
        //appbar = findViewById(R.id.appbar);
        videoTitle = v.findViewById(R.id.videoTitle);

        iv_search = v.findViewById(R.id.iv_search);

        setHasOptionsMenu(true);
        getActivity().setTitle(folderName);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_in_folder, menu);





        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent inc = new Intent(getActivity(), SettingsActivity.class);
                startActivity(inc);

                break;

            case R.id.action_search:
                Intent i = new Intent(getActivity(), SearchActivity.class);
                i.putExtra("Activity", "Video");
                startActivity(i);
                break;

            case R.id.iv_list_grid:
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
                break;



        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);




        //getActivity().getActionBar().hide();

        //tv_size.setText(VideoPlayerUtils.formateSize(folderSize(new File(f.getParentFile().getAbsolutePath()))));
        //getActivity().getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkGray)));
        text_title.setText(folderName);
        //File f = new File(videobucketimagesDataHashMap.get(folderName).get(0).getFullPath());
        isGrid = preferencesUtility.getDirList_Grid();
        initView();
        renameEvent();
        DeleteEvent();

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
        videoAdapter.setOnItemClickListener(new VideoAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                startActivity(VideoPlayerActivity.getIntent(getActivity(), videobucketimagesDataHashMap.get(folderName), position));
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SearchActivity.class);
                i.putExtra("Activity", "Video");
                startActivity(i);
            }
        });


        subscribeUpdateVideoListEvent();
    }


    public void onBackPressed() {

       getActivity().onBackPressed();
    }

        public void initView() {
        if(isGrid)
            videoList.setLayoutManager(new GridLayoutManager(getContext(), 3));

        else
            videoList.setLayoutManager(new LinearLayoutManager(getContext()));

        setAdapter();
    }

    private void setAdapter() {

        videoAdapter = new VideoAdapter(getActivity(), folderName, isGrid);

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

    private void renameEvent() {
        Subscription subscription = RxBus.getInstance().toObservable(RenameEvent.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<RenameEvent>() {
            @Override
            public void call(RenameEvent event) {
                if (event.getNewFile() != null && event.getOldFile() != null) {

                    if (event.getNewFile().exists()) {

                        if (videobucketimagesDataHashMap.get(folderName) != null && videobucketimagesDataHashMap.get(folderName).size() != 0)
                            for (int i = 0; i < videobucketimagesDataHashMap.get(folderName).size(); i++) {

                                if (event.getOldFile().getPath().equalsIgnoreCase(videobucketimagesDataHashMap.get(folderName).get(i).getFullPath())) {

                                    videobucketimagesDataHashMap.get(folderName).get(i).setFullPath(event.getNewFile().getPath());
                                    videobucketimagesDataHashMap.get(folderName).get(i).setTitle(event.getNewFile().getName());

                                    break;
                                }

                            }

                        setAdapter();
                    }
                }


            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
            }
        });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    private void DeleteEvent() {
        Subscription subscription = RxBus.getInstance().toObservable(DeleteEvent.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<DeleteEvent>() {
            @Override
            public void call(DeleteEvent event) {
                if (event.getId() != 0 ) {


                    if (videobucketimagesDataHashMap.get(folderName) != null && videobucketimagesDataHashMap.get(folderName).size() != 0)
                        for (int i = 0; i < videobucketimagesDataHashMap.get(folderName).size(); i++) {

                            if (event.getId() == videobucketimagesDataHashMap.get(folderName).get(i).getId()) {
                                ArrayList<Video> videos = videobucketimagesDataHashMap.get(folderName);
                                Video video = videobucketimagesDataHashMap.get(folderName).get(i);
                                videos.remove(video);
                                videobucketimagesDataHashMap.put(folderName, videos);
                                break;
                            }

                        }

                        /*if (videoAdapter != null) {
                            videoAdapter.notifyDataSetChanged();
                        } else {*/
                    setAdapter();
                    /* }*/

                        /*if (documentList != null && documentList.size() != 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            llEmpty.setVisibility(View.GONE);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            llEmpty.setVisibility(View.VISIBLE);
                        }*/



                }


            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
            }
        });
        RxBus.getInstance().addSubscription(this, subscription);
    }
}