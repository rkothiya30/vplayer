package com.example.vplayer.fragment.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.event.PlaylistItem;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.fragment.OnMenuFragment;
import com.example.vplayer.ui.fragment.OnPlaylistMenuFragment;

import java.util.ArrayList;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.MyViewHolder> implements OuterClickListener {

    Context context;
    ArrayList<String> playlist = new ArrayList<>();

    public PlayListAdapter(Context context, ArrayList<String> playlist) {
        this.context = context;
       this.playlist = playlist;
       this.playlist.add(0, "My Favorites");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        if(position==0){
            holder.ll_fav.setVisibility(View.VISIBLE);
        }
        else{
            holder.ll_fav.setVisibility(View.GONE);
            holder.ll_music.setVisibility(View.VISIBLE);
            holder.txt_folder_name.setText(playlist.get(position));
        }

        holder.popup_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShortDialog(position, playlist.get(position));
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
        return playlist.size();
    }

    @Override
    public void onOuterClick() {

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView popup_menu, iv_video;
        RelativeLayout ll_fav, ll_music, ll_video;
        AppCompatTextView txt_folder_name, txt_folder_item;




        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            popup_menu = itemView.findViewById(R.id.popup_menu);
            iv_video = itemView.findViewById(R.id.iv_video);
            ll_fav = itemView.findViewById(R.id.ll_fav);
            ll_music = itemView.findViewById(R.id.ll_music);
            ll_video = itemView.findViewById(R.id.ll_video);
            txt_folder_item = itemView.findViewById(R.id.txt_folder_item);
            txt_folder_name = itemView.findViewById(R.id.txt_folder_name);


        }
    }
}
