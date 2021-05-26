package com.example.vplayer.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.ContinueWatchingVideoAdapter;
import com.example.vplayer.fragment.adapter.VideoFolderAdapter;
import com.example.vplayer.fragment.event.UpdateContinueWatchingEvent;
import com.example.vplayer.fragment.event.UpdateVideoList;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.HistoryVideo;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.VideoDataService;
import com.example.vplayer.ui.activity.FolderInFolderActivity;
import com.example.vplayer.ui.activity.SeeMoreActivity;
import com.example.vplayer.ui.activity.SettingsActivity;
import com.google.gson.Gson;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;
import static com.example.vplayer.service.VideoDataService.videobuckets;

public class VideoFragment extends Fragment {

    View view;
    List<Video> videoList;
    List<String> sortByList;
    PreferencesUtility preferencesUtility;

    RecyclerView videoLList;
    SwipeRefreshLayout refreshLayout;
    AppCompatImageView emptyString;

    List<Video> videoListWithAd;
    List<Video> continueWatchingVideoList;

    VideoFolderAdapter videoFolderAdapter;
    ContinueWatchingVideoAdapter continueWatchingVideoAdapter;
    /*@Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
    }
*/

   /* @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(getActivity(), "Search Clicked", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_sort_by:
                Toast.makeText(getActivity(), "Sort By Clicked", Toast.LENGTH_SHORT).show();
                return true;


            case R.id.action_menu:
                Toast.makeText(getActivity(), "Menu Clicked", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_select:
                Toast.makeText(getActivity(), "Select Clicked", Toast.LENGTH_SHORT).show();
                return true;


        }
        return super.onOptionsItemSelected(item);

    }*/

    public void setVideoList() {
        videoListWithAd = new ArrayList<>();
        videoListWithAd.addAll(videoList);

        //videoFolderAdapter.setVideoList(videoListWithAd, true);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoList = new ArrayList<>();
        continueWatchingVideoList = new ArrayList<>();

        continueWatchingVideoAdapter = new ContinueWatchingVideoAdapter(getContext());

        preferencesUtility = PreferencesUtility.getInstance(getContext());
        continueWatchingVideoList = preferencesUtility.getContinueWatchingVideos();
        setHasOptionsMenu(true);


        subscribeUpdateVideoListEvent();
        subscribeUpdateContinueWatchingEvent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_video, container, false);


        videoLList = view.findViewById(R.id.videoList);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        emptyString = view.findViewById(R.id.emptyString);


        refreshLayout.setEnabled(false);


        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        new DownloadWebPageTask().execute();
    }

    public void initView() {

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position==0)
                    return 3;
                return 1;
            }
        });
        videoLList.setLayoutManager(gridLayoutManager);

        videoLList.setNestedScrollingEnabled(false);
        videoLList.setHasFixedSize(true);
        videoLList.setItemViewCacheSize(20);
        videoLList.setDrawingCacheEnabled(true);
        videoLList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        videoLList.setAdapter(videoFolderAdapter);

        videoLList.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                // TODO Auto-generated method stub
                //super.onScrollStateChanged(recyclerView, newState);
                try {
                    int firstPos = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstPos > 0) {
                        refreshLayout.setEnabled(false);
                    } else {
                        refreshLayout.setEnabled(true);
                        if (videoLList.getScrollState() == 1)
                            if (refreshLayout.isRefreshing())
                                videoLList.stopScroll();
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Scroll Error : " + e.getLocalizedMessage());
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new DownloadWebPageTask().execute();
            }
        });

        videoFolderAdapter.setOnItemClickListener(new VideoFolderAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                position--;
                if (videobuckets.get(position).getFolderList() != null && videobuckets.get(position).getFolderList().size() != 0) {
                    startActivity(FolderInFolderActivity.getInstance(getActivity(), position, videobuckets.get(position).folderPath, videobuckets.get(position).getFolderList().get(0)));
                } else {
                    startActivity(FolderInFolderActivity.getInstance(getActivity(), position, videobuckets.get(position).folderPath));
                }
            }
        });
        videoFolderAdapter.setOnMoreItemClickListener(new VideoFolderAdapter.MoreClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                startActivity(new Intent(getActivity(), SeeMoreActivity.class));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_video, menu);





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

                break;




        }

        return super.onOptionsItemSelected(item);
    }



    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshLayout.setRefreshing(true);
            emptyString.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... urls) {
            while (true) {
                if (VideoDataService.isImageComplate) {
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (VideoDataService.videoList.size() == 0) {
                    emptyString.setVisibility(View.VISIBLE);
                }

                if (videoList != null && videoList.size() == 0) {
//                    videoList.clear();
                    videoList = VideoDataService.videoList;
                    boolean isContains = false;
                    List<Video> hidevideoList = preferencesUtility.getUnhideVideos();
                    for (int i = 0; i < hidevideoList.size(); i++) {
                        isContains = false;
                        for (int j = 0; j < videoList.size(); j++) {
                            if (hidevideoList.get(i).getTitle().equals(videoList.get(j).getTitle())) {
                                isContains = true;
                            }
                        }
                        if (!isContains) {
                            videoList.add(hidevideoList.get(i));
                            isContains = false;
                        }
                    }

                    List<Video> unhidevideoList = preferencesUtility.getHideVideos();
                    for (int i = 0; i < unhidevideoList.size(); i++) {
                        int index = 0;
                        isContains = false;
                        for (int j = 0; j < videoList.size(); j++) {
                            if (unhidevideoList.get(i).getTitle().equals(videoList.get(j).getTitle())) {
                                isContains = true;
                                index = j;
                            }
                        }
                        if (isContains) {
                            videoList.remove(index);
                            isContains = false;
                        }
                    }
                    //sortList();
                }
                refreshLayout.setRefreshing(false);

            } catch (Exception e) {
            }
            videoFolderAdapter = new VideoFolderAdapter(getContext());

            initView();
        }


    }



    @Override
    public void onResume() {
        super.onResume();

        for (int i = 0; i < VideoDataService.videoList.size(); i++) {
            if (!new File(VideoDataService.videoList.get(i).getFullPath()).exists()) {
                VideoDataService.videoList.remove(i);
            }
        }

        if (VideoDataService.videoList.size() == 0) {
            // mPresenter.loadVideos();
            VideoDataService.videoList.clear();
        }
        if (videoFolderAdapter != null) {
            videoList = new ArrayList<>();
            videoList = VideoDataService.videoList;
            boolean isContains = false;
            List<Video> hideVideoList = preferencesUtility.getUnhideVideos();
            for (int i = 0; i < hideVideoList.size(); i++) {
                isContains = false;
                for (int j = 0; j < videoList.size(); j++) {
                    if (hideVideoList.get(i).getTitle().equals(videoList.get(j).getTitle())) {
                        isContains = true;
                    }
                }
                if (!isContains) {
                    videoList.add(hideVideoList.get(i));
                    isContains = false;
                }
            }

            List<Video> unhideVideoList = preferencesUtility.getHideVideos();
            for (int i = 0; i < unhideVideoList.size(); i++) {
                int index = 0;
                isContains = false;
                for (int j = 0; j < videoList.size(); j++) {
                    if (unhideVideoList.get(i).getTitle().equals(videoList.get(j).getTitle())) {
                        isContains = true;
                        index = j;
                    }
                }

                if (isContains) {
                    videoList.remove(index);
                    isContains = false;

                }
            }
            /*     sortList();*/
            refreshLayout.setRefreshing(false);
        }
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
                        videoListWithAd.remove(event.getPosition());
                        setVideoList();
                        videoFolderAdapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }

    private void subscribeUpdateContinueWatchingEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(UpdateContinueWatchingEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(new Action1<UpdateContinueWatchingEvent>() {
                    @Override
                    public void call(UpdateContinueWatchingEvent event) {
                        HistoryVideo updateVideos = new HistoryVideo(preferencesUtility.getContinueWatchingVideos());

                        VideoFolderAdapter.videoListWithContinueWatching.remove(0);
                        VideoFolderAdapter.videoListWithContinueWatching.add(0, new Gson().toJson(updateVideos));
                        setVideoList();
                        videoFolderAdapter.notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }


}