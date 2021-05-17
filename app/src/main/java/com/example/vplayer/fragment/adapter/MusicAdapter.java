package com.example.vplayer.fragment.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.vplayer.R;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.ui.fragment.OnPlaylistMenuFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> implements OuterClickListener {

    Context context;

    ArrayList<AudioModel> audioList = new ArrayList<>();

    public MusicAdapter(Context context, ArrayList<AudioModel> audioList) {
        this.context = context;
        this.audioList = audioList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_music_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {



        File file = new File(audioList.get(position).getPath());

        String currentString = file.getName();
        String mimeType = currentString.substring(currentString.lastIndexOf(".") + 1);

        holder.card_view.setVisibility(View.VISIBLE);
        holder.iv_music.setVisibility(View.GONE);
        holder.txt_authors.setText(audioList.get(position).getArtist());


        if (audioList.get(position).getBitmap() != null) {


            holder.iv_music.setVisibility(View.VISIBLE);
            holder.card_view.setVisibility(View.GONE);

            Glide.with(context).load(audioList.get(position).getBitmap()).
                    placeholder(R.drawable.ic_image_placeholder)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                          // holder.ivFolder.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audio_file));
                         //  holder.ivFolder.setColorFilter(context.getResources().getColor(R.color.theme_color), PorterDuff.Mode.SRC_IN);
                         //  holder.ivFolder.setVisibility(View.VISIBLE);
                           holder.card_view.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)).into(holder.iv_music);

        } else {
                          /*  viewholder.llPlay.setVisibility(View.VISIBLE);
                            viewholder.ivImage.setVisibility(View.GONE);

                            if (mimeType.equalsIgnoreCase("mp3")) {
                                viewholder.llPlay.setBackgroundColor(context.getResources().getColor(R.color.pink_audio_bg));
                            } else {
                                viewholder.llPlay.setBackgroundColor(context.getResources().getColor(R.color.blue_audio_bg));
                            }*/

           /* holder.ivFolder.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_audio_file));
            holder.ivFolder.setColorFilter(context.getResources().getColor(R.color.theme_color), PorterDuff.Mode.SRC_IN);
            holder.ivFolder.setVisibility(View.VISIBLE);*/
            holder.card_view.setVisibility(View.GONE);

        }

        holder.txt_music_name.setText(file.getName());

        /*if (audioList.get(position).isCheckboxVisible()) {
            viewholder.ll_check.setVisibility(View.VISIBLE);
            if (audioList.get(position).isSelected()) {
                viewholder.ivCheck.setVisibility(View.VISIBLE);
            } else {
                viewholder.ivCheck.setVisibility(View.GONE);
            }
        } else {
            viewholder.ll_check.setVisibility(View.GONE);
        }*/

        /*if (audioList.get(position).isFavorite()) {
            if (viewholder.ivFolder.getVisibility() == View.VISIBLE) {

                viewholder.iv_fav_file.setVisibility(View.GONE);
                viewholder.iv_fav_image.setVisibility(View.GONE);
                viewholder.iv_fav_other_file.setVisibility(View.VISIBLE);

            } else {
                viewholder.iv_fav_image.setVisibility(View.VISIBLE);
                viewholder.iv_fav_file.setVisibility(View.GONE);
                viewholder.iv_fav_other_file.setVisibility(View.GONE);
            }
        } else {
            viewholder.iv_fav_image.setVisibility(View.GONE);
            viewholder.iv_fav_file.setVisibility(View.GONE);
            viewholder.iv_fav_other_file.setVisibility(View.GONE);
        }*/


                      /*  if (audioList.get(position).isCheckboxVisible()) {
                            viewholder.ivCheck.setVisibility(View.VISIBLE);
                            if (audioList.get(position).isSelected()) {

                                viewholder.ivCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_radio_btn_selected));
                            } else
                                viewholder.ivCheck.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_radio_btn_unseleted));
                        } else
                            viewholder.ivCheck.setVisibility(View.GONE);*/



        holder.popup_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShortDialog(position, currentString);
            }
        });

    }

    public void showShortDialog ( int adapterPosition, String playlistName){
        OnPlaylistMenuFragment bottomSheetDialog = OnPlaylistMenuFragment.newInstance(adapterPosition, playlistName);
        bottomSheetDialog.setOuterClickListener(this);
        bottomSheetDialog.show(( (FragmentActivity) context ).getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");


    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    @Override
    public void onOuterClick() {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView iv_music;
        AppCompatTextView txt_music_name, txt_authors;
        ImageView popup_menu;
        CardView card_view;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            popup_menu = itemView.findViewById(R.id.popup_menu);
            iv_music = itemView.findViewById(R.id.iv_music);
            txt_music_name = itemView.findViewById(R.id.txt_music_name);
            txt_authors = itemView.findViewById(R.id.txt_authors);
            card_view = itemView.findViewById(R.id.card_view);


        }
    }
}
