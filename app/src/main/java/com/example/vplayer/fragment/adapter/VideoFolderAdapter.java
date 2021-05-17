package com.example.vplayer.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.event.HistoryEvent;
import com.example.vplayer.fragment.event.UpdateVideoStatusEvent;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.HistoryVideo;
import com.example.vplayer.model.Video;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.vplayer.service.VideoDataService.videobucketimagesDataHashMap;
import static com.example.vplayer.service.VideoDataService.videobuckets;


public class VideoFolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OuterClickListener {

    Context context;
    //ArrayList<PhotoData> AlbumDetails;
    String s;
    Set<String> keys = new LinkedHashSet<>();
    ArrayList<String> listkeys = new ArrayList<>();
    private static ClickListener listener;
    private static MoreClickListener moreClickListener;
    private static LongClickListener longClickListener;
    ContinueWatchingVideoAdapter continueWatchingVideoAdapter;
    public PreferencesUtility preferencesUtility;
    Video lastPlayVideo;
    List<Video> videoList = new ArrayList<>();
    List<Video> filterVideoList = new ArrayList<>();
    public static List<Object> videoListWithContinueWatching = new ArrayList<>();
    boolean isVideoFragmeny = false;
    boolean isContinueWachingVideos = false;
    int ia = 1;

    public static final int ITEM_VIDEO_TYPE = 2;
    public static final int ITEM_CONTINUE_TYPE = 3;



    public VideoFolderAdapter(Context context) {
        this.context = context;
        continueWatchingVideoAdapter = new ContinueWatchingVideoAdapter(context);
        preferencesUtility = PreferencesUtility.getInstance(context);
        lastPlayVideo = preferencesUtility.getLastPlayVideos();



        keys = videobucketimagesDataHashMap.keySet();
        listkeys.clear();
        listkeys = new ArrayList<>();
        listkeys.addAll(keys);

        for(int i =0; i<videobucketimagesDataHashMap.size();i++) {
            videoList.add(videobucketimagesDataHashMap.get(listkeys.get(i)).get(0));

        }
        setVideoList(videoList, true);

        /*for(int i = 0; i< videobucketimagesDataHashMap.get(listkeys.get(0)).size(); i++) {
            videoListWithContinueWatching.add(videobucketimagesDataHashMap.get(listkeys.get(0)).get(i));

        }
*/

    }

    public void setVideoList(List<Video> videoList) {
        //this.videoList.clear();
        videoListWithContinueWatching.clear();
        this.videoList = videoList;
        videoListWithContinueWatching.addAll(videoList);
        this.filterVideoList = videoList;
        notifyDataSetChanged();
    }

    public void setVideoList(List<Video> videoList, boolean isVideoList) {
        //this.videoList.clear();
        videoListWithContinueWatching.clear();
        this.videoList = videoList;
        this.isVideoFragmeny = isVideoList;
        //videoListWithContinueWatching.add(0, "Ad");
        if (preferencesUtility.getContinueWatchingVideos() != null) {
            isContinueWachingVideos = true;
            HistoryVideo historyVideo = new HistoryVideo(preferencesUtility.getContinueWatchingVideos());
            videoListWithContinueWatching.add(new Gson().toJson(historyVideo));
        }
        //videoListWithContinueWatching.addAll(videoList);
        for(int i = 0; i< videobucketimagesDataHashMap.size(); i++) {
            videoListWithContinueWatching.add(videobucketimagesDataHashMap.get(listkeys.get(i)).get(0));

        }
//
        this.filterVideoList = videoList;
       // notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case ITEM_VIDEO_TYPE:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_grid_item, parent, false));
            case ITEM_CONTINUE_TYPE:
                return new ContinueVideoVideHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.continue_watching, parent, false));

        }
        return null;
    }

    public void updateHistoryVideos(long videoId) {
        List<Video> historyVideo = new ArrayList<>();
        historyVideo = preferencesUtility.getHistoryVideos();

        Iterator<Video> iterator = historyVideo.iterator();
        while (iterator.hasNext()) {
            Video currentVideo = iterator.next();
            // Do something with the value
            if (currentVideo.getId() == videoId) {
                iterator.remove();
            }
        }

        preferencesUtility.updateHistoryVideo(historyVideo);

        List<Video> continueWatchingVideo = new ArrayList<>();
        continueWatchingVideo = preferencesUtility.getContinueWatchingVideos();

        Iterator<Video> iterator1 = continueWatchingVideo.iterator();
        while (iterator1.hasNext()) {
            Video currentVideo = iterator1.next();
            // Do something with the value
            if (currentVideo.getId() == videoId) {
                iterator1.remove();
            }
        }

        preferencesUtility.updateContinueWatchingVideo(continueWatchingVideo);

        RxBus.getInstance().post(new UpdateVideoStatusEvent());
        RxBus.getInstance().post(new HistoryEvent());
    }

/*
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        *//*Glide.with(context).load(videobucketimagesDataHashMap.get(listkeys.get(position)).get(0).getFilePath())

                .apply(new RequestOptions().centerCrop())
                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder_2))
                .error(R.drawable.delete)
//                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.CollectionsImageview);*//*
        holder.folderTitle.setText(videobuckets.get(position).folderName);
        int videoCount = videobuckets.get(position).getVideoCount();
        if (videoCount == 1)
            holder.videoCount.setText(String.valueOf(videoCount) + " video");
        else
            holder.videoCount.setText(String.valueOf(videoCount) + " videos");


          *//*  holder.CollectionsImageview.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    Intent inc = new Intent(context, Singlelocation.class);
                    inc.putExtra("position", position );
                    context.startActivity(inc);
                }
            });*//*


    }*/




    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {
            case ITEM_VIDEO_TYPE:
                if (isContinueWachingVideos) {
                    position = position - 1;
                }
                MyViewHolder myViewHolder = (MyViewHolder) holder;

                myViewHolder.folderTitle.setText(videobuckets.get(position).folderName);
                int videoCount = videobuckets.get(position).getVideoCount();
                if (videoCount == 1)
                    myViewHolder.videoCount.setText(String.valueOf(videoCount) + " video");
                else
                    myViewHolder.videoCount.setText(String.valueOf(videoCount) + " videos");
                break;

            case ITEM_CONTINUE_TYPE:

                ContinueVideoVideHolder continueVideoVideHolder = (ContinueVideoVideHolder) holder;

                String video = String.valueOf(videoListWithContinueWatching.get(0));
                HistoryVideo videoList = new Gson().fromJson(video, HistoryVideo.class);
                if (videoList.getVideoList().size() != 0) {
                    continueVideoVideHolder.continueWatchingLayout.setVisibility(View.VISIBLE);
                } else {
                    continueVideoVideHolder.continueWatchingLayout.setVisibility(View.GONE);
                }


                /*RecyclerView.LayoutManager l = new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false);

                continueVideoVideHolder.continueVideoList.setLayoutManager(l);*/
                continueVideoVideHolder.continueVideoList.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
                continueVideoVideHolder.continueVideoList.setNestedScrollingEnabled(false);
                continueVideoVideHolder.continueVideoList.setHasFixedSize(true);
                continueVideoVideHolder.continueVideoList.setItemViewCacheSize(20);


                continueVideoVideHolder.continueVideoList.setNestedScrollingEnabled(false);
                continueVideoVideHolder.continueVideoList.setHasFixedSize(true);
                continueVideoVideHolder.continueVideoList.setItemViewCacheSize(20);
                continueVideoVideHolder.continueVideoList.setDrawingCacheEnabled(true);
                continueVideoVideHolder.continueVideoList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                continueVideoVideHolder.continueVideoList.setAdapter(continueWatchingVideoAdapter);
                continueWatchingVideoAdapter.setVideoList(videoList.getVideoList());

                break;



        }

    }

    @Override
    public int getItemViewType(int position) {

       /* if (ia==1 || ia ==2) {
            ia++;
                return ITEM_CONTINUE_TYPE;

        } else {


                    return ITEM_VIDEO_TYPE;

            }*/


        if (videoListWithContinueWatching.get(position) instanceof String) {

                return ITEM_CONTINUE_TYPE;

        } else {
            if (isContinueWachingVideos) {
                position = position - 1;
            }
            switch (videoList.get(position).getLayoutType()) {

                case 2:
                    return ITEM_VIDEO_TYPE;
                default:
                    return -1;
            }
        }

    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }
    public interface MoreClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnMoreItemClickListener(MoreClickListener clickListener) {
        this.moreClickListener = clickListener;
    }
    public interface LongClickListener {
        void onItemLongClick(int position, View v);
    }

    public void setOnLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }


    @Override
    public int getItemCount() {


       // return videobucketimagesDataHashMap.size();
        return videoListWithContinueWatching.size();

    }

   /* public void showShortDialog(int adapterPosition) {
        OnMenuFragment bottomSheetDialog = OnMenuFragment.newInstance(adapterPosition);

        bottomSheetDialog.show(( (FragmentActivity) context ).getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");

    }*/

    @Override
    public void onOuterClick() {

    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView folderIcon;
        TextView folderTitle, videoCount;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            folderIcon = itemView.findViewById(R.id.folderIcon);

            folderTitle = itemView.findViewById(R.id.folderTitle);
            videoCount = itemView.findViewById(R.id.videoCount);
            //popup_menu = itemView.findViewById(R.id.popup_menu);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            /*popup_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showShortDialog(getAdapterPosition());
                }
            });*/


        }

        @Override
        public void onClick(View view) {

            listener.onItemClick(getAdapterPosition(), view);
        }

        public boolean onLongClick(View v) {
            longClickListener.onItemLongClick(getAdapterPosition(), v);
            return true;
        }
    }

    public class ContinueVideoVideHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout continueWatchingLayout;
        RecyclerView continueVideoList;
        TextView tv_see_more;


        public ContinueVideoVideHolder(View itemView) {
            super(itemView);

            continueVideoList = itemView.findViewById(R.id.continueVideoList);
            continueWatchingLayout = itemView.findViewById(R.id.continueWatchingLayout);
            tv_see_more = itemView.findViewById(R.id.tv_see_more);


            tv_see_more.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            moreClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

}
