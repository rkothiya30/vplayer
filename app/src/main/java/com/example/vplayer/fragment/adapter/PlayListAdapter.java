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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.MyViewHolder> implements OuterClickListener {

    Context context;
    LinkedHashMap<String, String> playlist = new LinkedHashMap<>();


    private static ClickListener listener;
    private static LongClickListener longClickListener;
    Set<String> keys = new LinkedHashSet<>();

    ArrayList<String> listkeys = new ArrayList<>();

    public PlayListAdapter(Context context, LinkedHashMap<String, String> playlist) {
        this.context = context;
       this.playlist = playlist;
       keys = playlist.keySet();
        listkeys.clear();
        listkeys = new ArrayList<>();
        listkeys.addAll(keys);

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        listkeys.clear();
        listkeys = new ArrayList<>();
        listkeys.addAll(keys);
        if(position==0){
            holder.ll_fav.setVisibility(View.VISIBLE);
        }
        else{
            holder.ll_fav.setVisibility(View.GONE);
            holder.ll_music.setVisibility(View.VISIBLE);
            holder.txt_folder_name.setText(listkeys.get(position));
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

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

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
