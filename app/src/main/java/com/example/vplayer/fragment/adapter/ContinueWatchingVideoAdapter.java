package com.example.vplayer.fragment.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.vplayer.R;
import com.example.vplayer.fragment.event.HistoryEvent;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.FloatingWidgetService;
import com.example.vplayer.ui.activity.VideoPlayerActivity;

import java.io.File;
import java.util.HashMap;
import java.util.List;


public class ContinueWatchingVideoAdapter extends RecyclerView.Adapter<ContinueWatchingVideoAdapter.ContinueWatchingVideoHolder> {

    List<Video> videoList;
    Context context;
    PreferencesUtility preferencesUtility;
    boolean isResumeVideo = false;
    boolean isShowVideoStatus = false;
    HashMap<String, Integer> videoLastStatusList = new HashMap<>();

    private static ClickListener listener;

    public ContinueWatchingVideoAdapter(Context context) {
        this.context = context;
        preferencesUtility = PreferencesUtility.getInstance(context);
    }

    public void setVideoList(List<Video> videoList) {
        this.videoList = videoList;
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }

    @NonNull
    @Override
    public ContinueWatchingVideoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContinueWatchingVideoHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_continue_watching, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ContinueWatchingVideoHolder holder, int position) {
        isResumeVideo = preferencesUtility.isResumeVideo();
        isShowVideoStatus = preferencesUtility.isShowResumeStatus();

        Video video = videoList.get(position);
        /*holder.videoName.setText(video.getTitle());
        holder.videoExtension.setText(VideoPlayerUtils.getFilenameExtension(video.getFullPath()));

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(new File(video.getFullPath())));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        String duration = VideoPlayerUtils.makeShortTimeString(holder.videoDuration.getContext(), timeInMillisec / 1000);
        holder.videoDuration.setText(duration);*/

       /* ContentResolver crThumb = context.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, video.getId(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND, options);

        if (curThumb != null){
            holder.videoThumbnail.setImageBitmap(curThumb);
        }else */{
            File file = new File(video.getFullPath());

            RequestOptions option = new RequestOptions()
                    .signature(new ObjectKey(file.getAbsolutePath() + file.lastModified()))
                    .priority(Priority.LOW)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            Glide.with(holder.videoThumbnail.getContext())
                    .load(video.getFullPath()).apply(option)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.videoThumbnail);
        }

       // holder.videoSize.setText(VideoPlayerUtils.formateSize(video.getSize()));

        holder.videoProgress.setVisibility(View.VISIBLE);
        holder.videoProgress.setMax(video.getVideoDuration());
        double progress1 = Double.parseDouble(String.valueOf(video.getVideoLastPlayPosition()));
        int progress = (int) progress1;

        //progressBar.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        holder.videoProgress.setProgressTintList(ColorStateList.valueOf(Color.RED));
        holder.videoProgress.setProgress(progress);

       holder.continue_watching.setOnClickListener(v -> {
            preferencesUtility.setIsFloatingVideo(false);
            context.stopService(new Intent(context, FloatingWidgetService.class));
            holder.continue_watching.getContext().startActivity(VideoPlayerActivity.getInstanceContinueWatching(context, videoList, position, true));
            RxBus.getInstance().post(new HistoryEvent());
            preferencesUtility.setHistoryVideos(video);
            preferencesUtility.setLastPlayVideos(video);
            //holder.videoName.setTextColor(context.getResources().getColor(R.color.md_blue_500));
            notifyDataSetChanged();

        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ContinueWatchingVideoHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        ImageView videoThumbnail;
        ProgressBar videoProgress;
        LinearLayout continue_watching;

        public ContinueWatchingVideoHolder(View itemView) {
            super(itemView);
           videoThumbnail = itemView.findViewById(R.id.videoThumbnail);
           /*videoExtension = itemView.findViewById(R.id.videoExtension);
           videoSize = itemView.findViewById(R.id.videoSize);
           videoDuration = itemView.findViewById(R.id.videoDuration);
           videoName = itemView.findViewById(R.id.videoName);*/
            videoProgress = itemView.findViewById(R.id.videoProgress);
            continue_watching = itemView.findViewById(R.id.continue_watching);


            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            listener.onItemClick(getAdapterPosition(), v);
        }
    }
}
