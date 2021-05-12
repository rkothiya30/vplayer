package com.example.vplayer.fragment.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.HistoryVideo;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.VideoPlayerActivity;
import com.example.vplayer.ui.fragment.OnMenuFragment;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static com.example.vplayer.service.VideoDataService.videobucketimagesDataHashMap;

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int position;
    List<Video> videoList = new ArrayList<>();
    List<Video> filterVideoList = new ArrayList<>();
    Context context;
    boolean isShowVideoStatus = false;
    public PreferencesUtility preferencesUtility;
    private static ClickListener listener;
    private static LongClickListener longClickListener;
    boolean isGrid = false;

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_ITEM_GRID = 1;

    boolean isResumeVideo = false;
    String folderName = "";
    HashMap<String, Integer> videoLastStatusList = new HashMap<>();

    Video lastPlayVideo;
    boolean isSecrentFragment = false;

    boolean isContinueWachingVideos = false;
    boolean isVideoFragmeny = false;

    public VideoAdapter(Context context) {
        this.context = context;
    }

    public VideoAdapter( boolean isGrid, Context context) {

        this.context = context;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }

    public interface LongClickListener {
        void onItemLongClick(int position, View v);
    }

    public void setOnLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (isGrid) {
            return TYPE_ITEM_GRID;
        } else
            return TYPE_ITEM;
    }

    public VideoAdapter(Context context, String folderName, boolean isGrid) {
        this.context = context;
        this.folderName = folderName;
        this.isGrid = isGrid;
        preferencesUtility = PreferencesUtility.getInstance(context);
        videoList = videobucketimagesDataHashMap.get(folderName);

    }

    public VideoAdapter(Context context, int isSecrentFragment) {
        this.context = context;
        this.position = isSecrentFragment;

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


    public void setVideoList(List<Video> videoList) {
        this.videoList.clear();
        this.videoList = videoList;
        this.filterVideoList = videoList;
        notifyDataSetChanged();
    }

    public void setVideoList(List<Video> videoList, boolean isVideoList) {
        this.videoList.clear();
        this.videoList = videoList;
        this.isVideoFragmeny = isVideoList;

//
        this.filterVideoList = videoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout., parent, false);
        VideoViewHolder mnvw = new VideoViewHolder(view);
        return mnvw;
*/
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
            return new VideoViewHolder(view);
        } else if (viewType == TYPE_ITEM_GRID) {
            View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage_grid, parent, false);
            return new GridHolder(view1);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        switch (viewType) {
            case TYPE_ITEM:
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;

                if (position == 0) {
                    videoViewHolder.videoTitle.setVisibility(View.VISIBLE);
                    File f = new File(videoList.get(0).getFullPath());

                    videoViewHolder.videoTitle.setText(String.valueOf(videoList.size()) + " VIDEOS      " + VideoPlayerUtils.formateSize(folderSize(new File(f.getParentFile().getAbsolutePath()))));
                } else {
                    videoViewHolder.videoTitle.setVisibility(View.GONE);
                }

                isResumeVideo = preferencesUtility.isResumeVideo();
                isShowVideoStatus = preferencesUtility.isShowResumeStatus();

                Video video = videoList.get(position);


                videoViewHolder.videoName.setText(video.getTitle());
                videoViewHolder.videoName.setSelected(true);
                //holder.videoExtension.setText(VideoPlayerUtils.getFilenameExtension(video.getFullPath()));


                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(context, Uri.fromFile(new File(video.getFullPath())));
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMillisec = Long.parseLong(time);
                String duration = VideoPlayerUtils.makeShortTimeString(videoViewHolder.videoDuration.getContext(), timeInMillisec / 1000);
                videoViewHolder.videoDuration.setText(duration);

                ContentResolver crThumb = context.getContentResolver();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, video.getId(), MediaStore.Video.Thumbnails.MICRO_KIND, options);

                if (curThumb != null) {
                    videoViewHolder.videoThumbnail.setImageBitmap(curThumb);
                } else {
                    File file = new File(video.getFullPath());

                    RequestOptions option = new RequestOptions()
                            .signature(new ObjectKey(file.getAbsolutePath() + file.lastModified()))
                            .priority(Priority.LOW)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                    Glide.with(context)
                            .load(video.getFullPath()).apply(option)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(videoViewHolder.videoThumbnail);
                }

                videoViewHolder.videoSize.setText(VideoPlayerUtils.formateSize(video.getSize()));

                if (videoLastStatusList.containsKey(video.getFullPath())) {
                    if (isShowVideoStatus) {
                        videoViewHolder.videoProgress.setVisibility(View.VISIBLE);
                        videoViewHolder.videoProgress.setMax(video.getVideoDuration());
                        double progress1 = Double.parseDouble(String.valueOf(videoLastStatusList.get(video.getFullPath())));
                        int progress = (int) progress1;
                        videoViewHolder.videoProgress.setProgress(progress);
                    } else {
                        videoViewHolder.videoProgress.setVisibility(View.INVISIBLE);
                    }
                }


                videoViewHolder.popup_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showShortDialog(-2, video.getTitle());
                    }
                });
                break;

            case TYPE_ITEM_GRID:
                GridHolder gridHolder = (GridHolder) holder;

                if (position == 0) {
                    gridHolder.tv_videos.setVisibility(View.VISIBLE);
                    gridHolder.tv_size.setVisibility(View.VISIBLE);
                    File f = new File(videoList.get(0).getFullPath());

                    gridHolder.tv_videos.setText(String.valueOf(videoList.size()) + " VIDEOS" );
                    gridHolder.tv_size.setText(VideoPlayerUtils.formateSize(folderSize(new File(f.getParentFile().getAbsolutePath()))));
                } else if(position == 1 || position == 2) {
                    gridHolder.tv_videos.setVisibility(View.INVISIBLE);
                    gridHolder.tv_size.setVisibility(View.INVISIBLE);
                }
                   else {
                        gridHolder.tv_videos.setVisibility(View.GONE);
                        gridHolder.tv_size.setVisibility(View.GONE);
                    }


                isResumeVideo = preferencesUtility.isResumeVideo();
                isShowVideoStatus = preferencesUtility.isShowResumeStatus();

                Video video1 = videoList.get(position);


                gridHolder.videoName.setText(video1.getTitle());
                gridHolder.videoName.setSelected(true);
                //holder.videoExtension.setText(VideoPlayerUtils.getFilenameExtension(video.getFullPath()));


                MediaMetadataRetriever retriever1 = new MediaMetadataRetriever();
                retriever1.setDataSource(context, Uri.fromFile(new File(video1.getFullPath())));
                String time1 = retriever1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMillisec1 = Long.parseLong(time1);
                String duration1 = VideoPlayerUtils.makeShortTimeString(gridHolder.videoDuration.getContext(), timeInMillisec1 / 1000);
                gridHolder.videoDuration.setText(duration1);

                ContentResolver crThumb1 = context.getContentResolver();
                BitmapFactory.Options options1 = new BitmapFactory.Options();
                options1.inSampleSize = 1;
                Bitmap curThumb1 = MediaStore.Video.Thumbnails.getThumbnail(crThumb1, video1.getId(), MediaStore.Video.Thumbnails.MICRO_KIND, options1);

                if (curThumb1 != null) {
                    gridHolder.videoThumbnail.setImageBitmap(curThumb1);
                } else {
                    File file = new File(video1.getFullPath());

                    RequestOptions option = new RequestOptions()
                            .signature(new ObjectKey(file.getAbsolutePath() + file.lastModified()))
                            .priority(Priority.LOW)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                    Glide.with(context)
                            .load(video1.getFullPath()).apply(option)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(gridHolder.videoThumbnail);
                }

                gridHolder.videoSize.setText(VideoPlayerUtils.formateSize(video1.getSize()));

                if (videoLastStatusList.containsKey(video1.getFullPath())) {
                    if (isShowVideoStatus) {
                        gridHolder.videoProgress.setVisibility(View.VISIBLE);
                        gridHolder.videoProgress.setMax(video1.getVideoDuration());
                        double progress1 = Double.parseDouble(String.valueOf(videoLastStatusList.get(video1.getFullPath())));
                        int progress = (int) progress1;
                        gridHolder.videoProgress.setProgress(progress);
                    } else {
                        gridHolder.videoProgress.setVisibility(View.INVISIBLE);
                    }
                }


                gridHolder.popup_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showShortDialog(-2, video1.getTitle());
                    }
                });
                break;

        }
    }






   /* public void onBindVideoViewHolder(@NonNull VideoViewHolder holder, final int position) {




    }*/

        public void showShortDialog ( int adapterPosition, String title){
            OnMenuFragment bottomSheetDialog = OnMenuFragment.newInstance(adapterPosition, title);

            bottomSheetDialog.show(( (FragmentActivity) context ).getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");

        }

        @Override
        public int getItemCount ( ) {
            return videoList.size();
        }


        public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            ImageView videoThumbnail, popup_menu;
            TextView videoName, videoDuration, videoSize, videoTitle;
            ProgressBar videoProgress;

            public VideoViewHolder(@NonNull View itemView) {
                super(itemView);

                videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
                videoName = itemView.findViewById(R.id.videoName);
                // videoExtension = itemView.findViewById(R.id.videoExtension);
                videoDuration = itemView.findViewById(R.id.videoDuration);
                videoSize = itemView.findViewById(R.id.videoSize);
                videoTitle = itemView.findViewById(R.id.videoTitle);
                popup_menu = itemView.findViewById(R.id.popup_menu);
                videoProgress = itemView.findViewById(R.id.videoProgress);


                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
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

        public class GridHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            ImageView videoThumbnail, popup_menu;
            TextView videoName, videoDuration, videoSize, videoTitle, tv_videos, tv_size;
            ProgressBar videoProgress;

            public GridHolder(View itemView) {
                super(itemView);

                videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
                videoName = itemView.findViewById(R.id.videoName);
                // videoExtension = itemView.findViewById(R.id.videoExtension);
                videoDuration = itemView.findViewById(R.id.videoDuration);
                videoSize = itemView.findViewById(R.id.videoSize);
                videoTitle = itemView.findViewById(R.id.videoTitle);
                popup_menu = itemView.findViewById(R.id.popup_menu);
                videoProgress = itemView.findViewById(R.id.videoProgress);
                tv_videos = itemView.findViewById(R.id.tv_videos);
                tv_size = itemView.findViewById(R.id.tv_size);

                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                listener.onItemClick(getAdapterPosition(), view);
            }

            @Override
            public boolean onLongClick(View view) {
                longClickListener.onItemLongClick(getAdapterPosition(), view);
                return true;
            }
        }



}