package com.example.vplayer.fragment.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.interfaces.SortByClickListener;
import com.example.vplayer.ui.fragment.OnMenuFragment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.vplayer.service.VideoDataService.videobucketimagesDataHashMap;
import static com.example.vplayer.service.VideoDataService.videobuckets;


public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.MyViewHolder> implements SortByClickListener {

    Context context;
    //ArrayList<PhotoData> AlbumDetails;
    String s;
    Set<String> keys = new LinkedHashSet<>();
    ArrayList<String> listkeys = new ArrayList<>();
    private static ClickListener listener;
    private static LongClickListener longClickListener;


    public VideoFolderAdapter(Context context) {
        this.context = context;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        MyViewHolder mnvw = new MyViewHolder(view);
        keys = videobucketimagesDataHashMap.keySet();
        listkeys.clear();
        listkeys = new ArrayList<>();
        listkeys.addAll(keys);
        return mnvw;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {


        /*Glide.with(context).load(videobucketimagesDataHashMap.get(listkeys.get(position)).get(0).getFilePath())

                .apply(new RequestOptions().centerCrop())
                .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_image_placeholder_2))
                .error(R.drawable.delete)
//                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.CollectionsImageview);*/
        holder.folderTitle.setText(videobuckets.get(position).folderName);
        holder.videoCount.setText(String.valueOf(videobuckets.get(position).videoCount));


          /*  holder.CollectionsImageview.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    Intent inc = new Intent(context, Singlelocation.class);
                    inc.putExtra("position", position );
                    context.startActivity(inc);
                }
            });*/


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
    public int getItemCount() {


        return videobucketimagesDataHashMap.size();

    }

    public void showShortDialog(int adapterPosition) {
        OnMenuFragment bottomSheetDialog = OnMenuFragment.newInstance(adapterPosition);

        bottomSheetDialog.show(( (FragmentActivity) context ).getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");

    }

    @Override
    public void onSortByClick() {

    }


    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ImageView folderIcon, popup_menu;
        TextView folderTitle, videoCount;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            folderIcon = itemView.findViewById(R.id.folderIcon);

            folderTitle = itemView.findViewById(R.id.folderTitle);
            videoCount = itemView.findViewById(R.id.videoCount);
            popup_menu = itemView.findViewById(R.id.popup_menu);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            popup_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showShortDialog(getAdapterPosition());
                }
            });


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
