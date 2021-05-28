package com.example.vplayer.fragment.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;

import java.util.List;

import static android.view.View.GONE;

public class OnMenuAdapter extends RecyclerView.Adapter<OnMenuAdapter.ViewHolder> {

    private List<String> mItems;
    private Integer[] mImages = {R.drawable.background_play, R.drawable.add_to_playlist, R.drawable.rename, R.drawable.ic_delete, R.drawable.is_share, R.drawable.ic_properties};
    private Integer[] mImages1 = {R.drawable.background_play, R.drawable.add_to_playlist, R.drawable.rename, R.drawable.ic_delete, R.drawable.is_share};
    private Integer[] mImages4 = {R.drawable.background_play, R.drawable.add_to_playlist, R.drawable.ic_delete, R.drawable.is_share};
    private ItemListener itemListener;
    int selectedItem;
    Context context;
    Video video;
    int check;
    PreferencesUtility preferencesUtility;

    public OnMenuAdapter(Context context, List<String> items, ItemListener listener, Video video) {
        mItems = items;
        this.context = context;
        itemListener = listener;
        this.video = video;
        preferencesUtility = PreferencesUtility.getInstance(context);
    }

    public OnMenuAdapter(Context context,int check, List<String> items, ItemListener listener, Video video) {
        mItems = items;
        this.check = check;
        this.context = context;
        itemListener = listener;
        this.video = video;
        preferencesUtility = PreferencesUtility.getInstance(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.text.setText(mItems.get(position));

        if(check == -1 && position == 5){
            holder.item_image.setImageResource(mImages1[position]);

        }
        if(check == -4 && position == 2){
            holder.item_image.setImageResource(mImages4[position]);
              }else{
            holder.item_image.setImageResource(mImages[position]);
             }
        holder.item_image.setColorFilter(ContextCompat.getColor(holder.item_image.getContext(), R.color.icon_color), PorterDuff.Mode.SRC_IN);
        // setOnPopupMenuListener(holder, position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

  /*  private void setOnPopupMenuListener(ViewHolder itemHolder, final int position) {

        itemHolder.fragment_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(context, v);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (position) {
                            case 2:
                                RenameDialog.getInstance((Activity) context, video.getTitle(), video.getId(), video.getFullPath())
                                        .show(((AppCompatActivity) context)
                                                .getSupportFragmentManager(), "");
                                break;
                            case 4:
                                VideoPlayerUtils.shareVideo(video.getId(), context);
                                break;
                            case 3:
                                long[] videoId = {video.getId()};
                                VideoPlayerUtils.showDeleteDialog(context, video.getTitle(), videoId);
                                break;
                            case 5:
                                VideoDetailsDialog.getInstance(video)
                                        .show(((AppCompatActivity) context)
                                                .getSupportFragmentManager(), "");
                                break;

                        }
                        return false;
                    }
                });

            }
        });
    }*/

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text;
        String item;
        ImageView item_image;
        LinearLayout fragment_item;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_text);
            item_image = itemView.findViewById(R.id.item_image);
            fragment_item = itemView.findViewById(R.id.fragment_item);

            fragment_item.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            if (itemListener != null) {
                itemListener.onItemClick(getAdapterPosition());
            }
        }
    }

    public interface ItemListener {
        void onItemClick(int item);
    }
}
