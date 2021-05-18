package com.example.vplayer.fragment.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.VideoDataService;
import com.example.vplayer.ui.fragment.OnMenuFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoSAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private int position;
    public static List<Video> videoList = new ArrayList<>();
    Context context;
    private static ClickListener listener;
    private static LongClickListener longClickListener;

    private static boolean Selection = false;



    public VideoSAdapter(Context context) {
        this.context = context;
        videoList = VideoDataService.videoList;
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



    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout., parent, false);
        VideoViewHolder mnvw = new VideoViewHolder(view);
        return mnvw;
*/

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
            return new VideoViewHolder(view);



    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;


                    videoViewHolder.videoTitle.setVisibility(View.GONE);


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


                if(video.isSelected()){
                    videoViewHolder.iv_empty_checkbox.setVisibility(View.GONE);
                    videoViewHolder.iv_selected_checkbox.setVisibility(View.VISIBLE);
                }
                else{
                    videoViewHolder.iv_empty_checkbox.setVisibility(View.VISIBLE);
                    videoViewHolder.iv_selected_checkbox.setVisibility(View.GONE);
                }

    }






   /* public void onBindVideoViewHolder(@NonNull VideoViewHolder holder, final int position) {




    }*/


    @Override
    public int getItemCount ( ) {
        return videoList.size();}


    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView videoThumbnail, popup_menu, iv_empty_checkbox, iv_selected_checkbox;
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
            iv_empty_checkbox = itemView.findViewById(R.id.iv_empty_checkbox);
            iv_selected_checkbox = itemView.findViewById(R.id.iv_selected_checkbox);



                popup_menu.setVisibility(View.GONE);
                iv_empty_checkbox.setVisibility(View.VISIBLE);


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





}

