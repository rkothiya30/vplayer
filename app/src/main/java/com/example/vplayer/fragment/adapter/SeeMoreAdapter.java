package com.example.vplayer.fragment.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.SeeMoreActivity;
import com.example.vplayer.ui.fragment.OnMenuFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.LongConsumer;

import static com.example.vplayer.service.VideoDataService.videobucketimagesDataHashMap;

public class SeeMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public static boolean selectedSet = false;
    private int position;
    public static List<Video> videoList = new ArrayList<>();

    HashMap<String, Integer> videoLastStatusList = new HashMap<>();

    boolean isResumeVideo = false;
    boolean isShowVideoStatus = false;
    Context context;

    public PreferencesUtility preferencesUtility;
    private static ClickListener listener;
    private static LongClickListener longClickListener;


    public SeeMoreAdapter(Context context, List<Video> videoList) {
        preferencesUtility = PreferencesUtility.getInstance(context);
        this.videoList = videoList;
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
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.see_more_item, parent, false);
        return new VideoViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        isShowVideoStatus = preferencesUtility.isShowResumeStatus();

        int viewType = getItemViewType(position);

        VideoViewHolder videoViewHolder = (VideoViewHolder) holder;

        if (position == 0) {
            videoViewHolder.rl.setVisibility(View.VISIBLE);


            videoViewHolder.videoTitle.setText(String.valueOf(videoList.size()) + " VIDEOS");
            if (selectedSet == true) {
                //videoViewHolder.videoTitle.setText("0/" + String.valueOf(videoList.size()) + " SELECTED");
                videoViewHolder.text_selected.setVisibility(View.VISIBLE);

            }


        } else {
            videoViewHolder.rl.setVisibility(View.GONE);
        }


        Video video = videoList.get(position);
        if (selectedSet == true)
            if (video.isSelected()) {
                videoViewHolder.iv_uncheck.setVisibility(View.GONE);
                videoViewHolder.iv_check_grid.setVisibility(View.VISIBLE);
            } else {
                videoViewHolder.iv_uncheck.setVisibility(View.VISIBLE);
                videoViewHolder.iv_check_grid.setVisibility(View.GONE);
            }


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

                videoViewHolder.videoProgress.setVisibility(View.VISIBLE);
                videoViewHolder.videoProgress.setMax(video.getVideoDuration());

                double progress1 = Double.parseDouble(String.valueOf(video.getVideoLastPlayPosition()));
                int progress = (int) progress1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            videoViewHolder.videoProgress.setProgressTintList(ColorStateList.valueOf(Color.RED));
        }
        videoViewHolder.videoProgress.setProgress(progress);


        }









   /* public void onBindVideoViewHolder(@NonNull VideoViewHolder holder, final int position) {




    }*/


    @Override
    public int getItemCount() {
        return videoList.size();

    }


    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        boolean toggle = true;
        ImageView videoThumbnail, popup_menu, iv_uncheck_all, iv_check_all, iv_uncheck, iv_check_grid;
        TextView videoName, videoDuration, videoSize, videoTitle, text_selected;
        ProgressBar videoProgress;
        RelativeLayout rl, ll_check_all, ll_check_grid;
        LinearLayout ll_area;

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
            rl = itemView.findViewById(R.id.rl);
            iv_uncheck_all = itemView.findViewById(R.id.iv_uncheck_all);
            iv_check_all = itemView.findViewById(R.id.iv_check_all);
            iv_uncheck = itemView.findViewById(R.id.iv_uncheck);
            iv_check_grid = itemView.findViewById(R.id.iv_check_grid);
            text_selected = itemView.findViewById(R.id.text_selected);
            ll_check_all = itemView.findViewById(R.id.ll_check_all);
            ll_check_grid = itemView.findViewById(R.id.ll_check_grid);
            ll_area = itemView.findViewById(R.id.ll_area);

            ll_check_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (toggle) {
                        iv_uncheck_all.setVisibility(View.GONE);
                        iv_check_all.setVisibility(View.VISIBLE);
                            toggle = false;
                        listener.onItemClick(getAdapterPosition(), v);

                    } else{
                        iv_uncheck_all.setVisibility(View.VISIBLE);
                        iv_check_all.setVisibility(View.GONE);
                        toggle = true;
                        listener.onItemClick(getAdapterPosition(), v);
                    }
                }
            });

                    ll_area.setOnClickListener(this);
            //ll_check_all.setOnClickListener(this);
            ll_area.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View view) {

            listener.onItemClick(getAdapterPosition(), view);
        }

        @Override
        public boolean onLongClick(View v) {
            longClickListener.onItemLongClick(getAdapterPosition(), v);
            return true;
        }

        /*public boolean onLongClick(View v) {
            longClickListener.onItemLongClick(getAdapterPosition(), v);
            return true;
        }*/
    }


}