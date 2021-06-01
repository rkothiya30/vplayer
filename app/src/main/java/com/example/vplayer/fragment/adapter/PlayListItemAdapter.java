package com.example.vplayer.fragment.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
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
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.MusicDataService;
import com.example.vplayer.ui.activity.PlayPlayListActivity;
import com.example.vplayer.ui.activity.PlayingSongActivity;
import com.example.vplayer.ui.activity.VideoPlayerActivity;
import com.example.vplayer.ui.fragment.MusicFragment;
import com.example.vplayer.ui.fragment.OnMenuFragment;
import com.example.vplayer.ui.fragment.OnPlaylistMenuFragment;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OuterClickListener {

    Context context;
    private static ClickListener listener;

    public static ArrayList<Object> aList = new ArrayList<>();

    public static final int ITEM_VIDEO_TYPE = 2;
    public static final int ITEM_MUSIC_TYPE = 3;

    public PlayListItemAdapter(Context context, ArrayList<Object> videoList,  boolean Selection) {
        this.context = context;
        this.aList = videoList;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        this.listener = clickListener;
    }







    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_MUSIC_TYPE:
                return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_video_list, parent, false));
            case ITEM_VIDEO_TYPE:
                return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false));

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case ITEM_MUSIC_TYPE:
                MyViewHolder myViewHolder = (MyViewHolder) holder;
                AudioModel audioFile = MusicDataService.audioList.get(0);
                try {
                     audioFile = (AudioModel) aList.get(position);
                } catch(Exception e){

                    e.printStackTrace();
                }
                File file1 = new File(audioFile.getPath());

                String currentString = file1.getName();
                String mimeType = currentString.substring(currentString.lastIndexOf(".") + 1);

                //myViewHolder.card_view.setVisibility(View.VISIBLE);
                myViewHolder.iv_music.setVisibility(View.GONE);
                myViewHolder.txt_authors.setText(audioFile.getArtist());


                if (audioFile.getBitmap() != null) {


                    myViewHolder.iv_music.setVisibility(View.VISIBLE);
                    //myViewHolder.card_view.setVisibility(View.GONE);

                    Glide.with(context).load(audioFile.getBitmap()).
                            placeholder(R.drawable.ic_image_placeholder)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                                    //myViewHolder.card_view.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(myViewHolder.iv_music);

                } else {

                    //myViewHolder.card_view.setVisibility(View.VISIBLE);

                }

                myViewHolder.txt_music_name.setText(file1.getName());


                myViewHolder.iv_empty_checkbox.setVisibility(View.GONE);
                myViewHolder.popup_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showShortDialog(position, currentString);
                    }
                });


                AudioModel finalAudioFile = audioFile;
                myViewHolder.ll_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
/*

                        Intent i = new Intent(context, PlayingSongActivity.class);
                        i.putExtra("Position", position);
                        i.putExtra("Audio", Parcels.wrap(aList));
                        i.putExtra("ActivityName", "PlayPlayListActivity");
                        context.startActivity(i);
*/



                                List<AudioModel> l = new ArrayList<>();
                                for (int i = 0; i < aList.size(); i++) {
                                    if (aList.get(i) instanceof AudioModel)
                                        l.add((AudioModel) aList.get(i));
                                }


                                Intent in = new Intent(context, PlayingSongActivity.class);
                                in.putExtra("Position", 0);
                                in.putExtra("Audio", Parcels.wrap(l));
                                in.putExtra("ActivityName", "PlayListItemAdapter");
                                context.startActivity(in);



                    }
                });
                break;

            case ITEM_VIDEO_TYPE:
                VideoViewHolder videoViewHolder = (VideoViewHolder) holder;



                Video video =(Video) aList.get(position);


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




                videoViewHolder.popup_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showShortDialog1(0, video);
                    }
                });
                videoViewHolder.ll_root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Video> l = new ArrayList<>();
                        for(int i = 0; i<aList.size();i++){
                            if(aList.get(i) instanceof Video)
                                l.add((Video) aList.get(i));
                        }

                        context.startActivity(VideoPlayerActivity.getIntent(context, aList, position, "PlayListItemAdapter"));

                       /* Intent i = new Intent(context, PlayingSongActivity.class);
                        i.putExtra("Position", position);
                        i.putExtra("Audio", Parcels.wrap(aList));
                        i.putExtra("ActivityName", "PlayPlayListActivity");
                        context.startActivity(i);*/
                    }
                });
                break;


        }
    }

    @Override
    public int getItemViewType(int position) {

            if(aList.get(position) instanceof AudioModel) {

                return ITEM_MUSIC_TYPE;
            } else if(aList.get(position) instanceof Video){
                    return ITEM_VIDEO_TYPE;

        }
        return 3;
            //throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }




    public void showShortDialog1 ( int adapterPosition, Video video){
        List<Video> v = new ArrayList<>();
        v.add(video);
        OnMenuFragment bottomSheetDialog = OnMenuFragment.newInstance(-2, 0, v,video);
        bottomSheetDialog.setOuterClickListener(this);
        bottomSheetDialog.show(( (FragmentActivity) context ).getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");


    }

    public void showShortDialog ( int adapterPosition, String playlistName){
        OnPlaylistMenuFragment bottomSheetDialog = OnPlaylistMenuFragment.newInstance(adapterPosition, playlistName);
        bottomSheetDialog.setOuterClickListener(this);
        bottomSheetDialog.show(( (FragmentActivity) context ).getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");


    }

    @Override
    public int getItemCount() {
        return aList.size();
    }

    @Override
    public void onOuterClick() {

    }

     class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        AppCompatImageView iv_music;
        AppCompatTextView txt_music_name, txt_authors;
        ImageView popup_menu;

        ImageView iv_empty_checkbox, iv_selected_checkbox;
        LinearLayout ll_root;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            popup_menu = itemView.findViewById(R.id.popup_menu);
            iv_music = itemView.findViewById(R.id.iv_music);
            txt_music_name = itemView.findViewById(R.id.txt_music_name);
            txt_authors = itemView.findViewById(R.id.txt_authors);

            iv_empty_checkbox = itemView.findViewById(R.id.iv_empty_checkbox);
            iv_selected_checkbox = itemView.findViewById(R.id.iv_selected_checkbox);
            ll_root = itemView.findViewById(R.id.ll_root);

                popup_menu.setVisibility(View.VISIBLE);
                iv_empty_checkbox.setVisibility(View.GONE);

            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            listener.onItemClick(getAdapterPosition(), view);
        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {

            listener.onItemClick(getAdapterPosition(), view);
        }


    }
}
