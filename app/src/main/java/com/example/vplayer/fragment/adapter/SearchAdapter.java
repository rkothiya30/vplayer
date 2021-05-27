package com.example.vplayer.fragment.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.PlayingSongActivity;
import com.example.vplayer.ui.activity.SearchActivity;
import com.example.vplayer.ui.activity.VideoPlayerActivity;

import org.parceler.Parcels;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private static ClickListener listener;
    private static LongClickListener longClickListener;
    boolean isGrid = false;
    ArrayList<Object> internalStorageList = new ArrayList<>();

    public static final int TYPE_AD = 2;
    public static final int VIDEO_ITEM = 0;
    public static final int AUDIO_ITEM = 1;


    public SearchAdapter(Context context, ArrayList<Object> internalStorageList, boolean isGrid) {
        this.context = context;
        this.internalStorageList = internalStorageList;
        this.isGrid = isGrid;
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
        if (internalStorageList.get(position) instanceof Video)
            return VIDEO_ITEM;
        else if (internalStorageList.get(position) instanceof AudioModel)
            return AUDIO_ITEM;

        return VIDEO_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIDEO_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
            return new VideoViewHolder(view);
        } else if (viewType == AUDIO_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_music_item, parent, false);
            return new MyViewHolder(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            int viewType = getItemViewType(position);
            switch (viewType) {
                case VIDEO_ITEM:
                    try {


                        VideoViewHolder viewholder = (VideoViewHolder) holder;


                        Video video = (Video) internalStorageList.get(position);


                        viewholder.videoName.setText(video.getTitle());
                        viewholder.videoName.setSelected(true);
                        //holder.videoExtension.setText(VideoPlayerUtils.getFilenameExtension(video.getFullPath()));


                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(context, Uri.fromFile(new File(video.getFullPath())));
                        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        long timeInMillisec = Long.parseLong(time);
                        String duration = VideoPlayerUtils.makeShortTimeString(viewholder.videoDuration.getContext(), timeInMillisec / 1000);
                        viewholder.videoDuration.setText(duration);

                        ContentResolver crThumb = context.getContentResolver();
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 1;
                        Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, video.getId(), MediaStore.Video.Thumbnails.MICRO_KIND, options);

                        if (curThumb != null) {
                            viewholder.videoThumbnail.setImageBitmap(curThumb);
                        } else {
                            File file = new File(video.getFullPath());

                            RequestOptions option = new RequestOptions()
                                    .signature(new ObjectKey(file.getAbsolutePath() + file.lastModified()))
                                    .priority(Priority.LOW)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                            Glide.with(context)
                                    .load(video.getFullPath()).apply(option)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(viewholder.videoThumbnail);
                        }

                        viewholder.videoSize.setText(VideoPlayerUtils.formateSize(video.getSize()));



                        /*InternalStorageFilesModel filesModel = internalStorageList.get(position);



                        File file = new File(filesModel.getFilePath());

                        String mimeType = Utils.getFilenameExtension(file.getName());
*/
                        viewholder.ll_root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<Video> videos = new ArrayList<>();
                                videos.add(video);
                                context.startActivity(VideoPlayerActivity.getIntent(context, videos, 0));
                            }
                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case AUDIO_ITEM:

                    MyViewHolder myViewHolder = (MyViewHolder) holder;
                    AudioModel a = ( (AudioModel) internalStorageList.get(position) );
                    File file = new File(a.getPath());

                    String currentString = file.getName();
                    String mimeType = currentString.substring(currentString.lastIndexOf(".") + 1);

                    myViewHolder.card_view.setVisibility(View.VISIBLE);
                    myViewHolder.iv_music.setVisibility(View.GONE);
                    myViewHolder.txt_authors.setText(a.getArtist());


                    if (a.getBitmap() != null) {


                        myViewHolder.iv_music.setVisibility(View.VISIBLE);
                        myViewHolder.card_view.setVisibility(View.GONE);

                        Glide.with(context).load(a.getBitmap()).
                                placeholder(R.drawable.ic_image_placeholder)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        // holder.ivFolder.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audio_file));
                                        //  holder.ivFolder.setColorFilter(context.getResources().getColor(R.color.theme_color), PorterDuff.Mode.SRC_IN);
                                        //  holder.ivFolder.setVisibility(View.VISIBLE);
                                        myViewHolder.card_view.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(myViewHolder.iv_music);

                    } else {

                        myViewHolder.card_view.setVisibility(View.VISIBLE);

                    }

                    myViewHolder.txt_music_name.setText(file.getName());
                    myViewHolder.ll_root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(context, PlayingSongActivity.class);
                            i.putExtra("Position", position);
                            i.putExtra("ActivityName", "MusicSearch");
                            i.putExtra("Audio", Parcels.wrap(a));
                            context.startActivity(i);
                        }
                    });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return internalStorageList.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder{


        ImageView videoThumbnail, popup_menu;
        TextView videoName, videoDuration, videoSize, videoTitle;
        ProgressBar videoProgress;
        LinearLayout ll_root;

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
ll_root = itemView.findViewById(R.id.ll_root);
            popup_menu.setVisibility(View.GONE);

        }


    }

    public class MyViewHolder extends RecyclerView.ViewHolder  {

        AppCompatImageView iv_music;
        AppCompatTextView txt_music_name, txt_authors;
        ImageView popup_menu;
        CardView card_view;
        ImageView iv_empty_checkbox, iv_selected_checkbox;
        LinearLayout ll_root;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            popup_menu = itemView.findViewById(R.id.popup_menu);
            iv_music = itemView.findViewById(R.id.iv_music);
            txt_music_name = itemView.findViewById(R.id.txt_music_name);
            txt_authors = itemView.findViewById(R.id.txt_authors);
            card_view = itemView.findViewById(R.id.card_view);
            iv_empty_checkbox = itemView.findViewById(R.id.iv_empty_checkbox);
            iv_selected_checkbox = itemView.findViewById(R.id.iv_selected_checkbox);
            ll_root = itemView.findViewById(R.id.ll_root);
            popup_menu.setVisibility(View.GONE);
            iv_empty_checkbox.setVisibility(View.GONE);

        }
    }

}
