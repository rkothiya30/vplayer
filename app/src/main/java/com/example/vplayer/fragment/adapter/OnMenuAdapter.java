package com.example.vplayer.fragment.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.PreferencesUtility;

import java.util.List;

public class OnMenuAdapter extends RecyclerView.Adapter<OnMenuAdapter.ViewHolder> {

    private List<String> mItems;
    private Integer[] mImages = {R.drawable.background_play, R.drawable.add_to_playlist, R.drawable.rename, R.drawable.ic_delete, R.drawable.is_share};
    private ItemListener mListener;
    int selectedItem;
    PreferencesUtility preferencesUtility;

    public OnMenuAdapter(Context context, List<String> items, ItemListener listener) {
        mItems = items;

        mListener = listener;
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
        holder.item_image.setImageResource(mImages[position]);
        holder.item_image.setColorFilter(ContextCompat.getColor(holder.item_image.getContext(), R.color.defaultGray), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text;
        String item;
        ImageView item_image;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_text);
            item_image = itemView.findViewById(R.id.item_image);

        }



        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onSortByClick(item);
            }
        }
    }

    public interface ItemListener {
        void onSortByClick(String item);
    }
}
