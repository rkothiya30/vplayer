package com.example.vplayer.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.adapter.OnMenuAdapter;
import com.example.vplayer.fragment.adapter.OnPlaylistMenuAdapter;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.vplayer.service.VideoDataService.videobuckets;

public class OnPlaylistMenuFragment extends BottomSheetDialogFragment implements OnPlaylistMenuAdapter.ItemListener {

    private static int position;
    OnPlaylistMenuAdapter onMenuAdapter;
    List<String> sortByList;
    PreferencesUtility preferencesUtility;
    OuterClickListener outerClickListener;
    private static int check;
    private static Video video;
    private static String title;


    public static OnPlaylistMenuFragment newInstance(int adapterPosition) {
        OnPlaylistMenuFragment fragment = new OnPlaylistMenuFragment();
        position = adapterPosition;
        return fragment;
    }


    public static OnPlaylistMenuFragment newInstance(int checks, String playlistName) {
        OnPlaylistMenuFragment fragment = new OnPlaylistMenuFragment();
        title = playlistName;
        position = checks;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        final String[] sortList = getResources().getStringArray(R.array.playlist_menu_array);
        sortByList = new ArrayList<String>(Arrays.asList(sortList));



    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.dialog_on_menu, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        TextView text_title = contentView.findViewById(R.id.text_title);

            text_title.setText(title);

        RecyclerView sortList = contentView.findViewById(R.id.sortList);
        sortList.setHasFixedSize(true);
        sortList.setLayoutManager(new LinearLayoutManager(getContext()));


        onMenuAdapter = new OnPlaylistMenuAdapter(getContext(), sortByList, this, video);
        sortList.setAdapter(onMenuAdapter);
    }

    @Override
    public void onItemClick(int item) {
       // preferencesUtility.setSortByVideo(sortByList.indexOf(item));
        outerClickListener.onOuterClick();
        getDialog().dismiss();

        switch (item) {
            case 0:
                Toast.makeText(getContext(), "Background Play", Toast.LENGTH_SHORT).show();
                break;

            case 1:
                Toast.makeText(getContext(), "Play Next", Toast.LENGTH_SHORT).show();
                break;
            case 2:
               /* RenameDialog.getInstance(getActivity(), video.getTitle(), video.getId(), video)
                        .show(getFragmentManager(), "");*/
                Toast.makeText(getContext(), "Add to queue", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(getContext(), "Add to playlist", Toast.LENGTH_SHORT).show();
                break;
            case 4:
                Toast.makeText(getContext(), "Rename", Toast.LENGTH_SHORT).show();
                break;
            case 5:
                Toast.makeText(getContext(), "Delete", Toast.LENGTH_SHORT).show();

                break;

        }


    }

    public void setOuterClickListener(OuterClickListener outerClickListener) {
        this.outerClickListener = outerClickListener;
    }
}
