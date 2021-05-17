package com.example.vplayer.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.adapter.OnMenuAdapter;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.vplayer.service.VideoDataService.videobuckets;

public class OnMenuFragment extends BottomSheetDialogFragment implements OnMenuAdapter.ItemListener {

    private static int position;
    OnMenuAdapter onMenuAdapter;
    List<String> sortByList;
    PreferencesUtility preferencesUtility;
    OuterClickListener outerClickListener;
    private static int check;
    private static Video video;
    private static String title;


    public static OnMenuFragment newInstance(int adapterPosition) {
        OnMenuFragment fragment = new OnMenuFragment();
        position = adapterPosition;
        return fragment;
    }


    public static OnMenuFragment newInstance(int checks, Video vide) {
        OnMenuFragment fragment = new OnMenuFragment();
        title = vide.getTitle();
        video = vide;
        check = checks;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        final String[] sortList = getResources().getStringArray(R.array.menu_array);
        sortByList = new ArrayList<String>(Arrays.asList(sortList));



    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.dialog_on_menu, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        TextView text_title = contentView.findViewById(R.id.text_title);
        if(check == -2){
            text_title.setText(title);
        } else {
            text_title.setText(videobuckets.get(position).getFolderName());

        }
        RecyclerView sortList = contentView.findViewById(R.id.sortList);
        sortList.setHasFixedSize(true);
        sortList.setLayoutManager(new LinearLayoutManager(getContext()));


        onMenuAdapter = new OnMenuAdapter(getContext(), sortByList, this, video);
        sortList.setAdapter(onMenuAdapter);
    }

    @Override
    public void onItemClick(int item) {
       // preferencesUtility.setSortByVideo(sortByList.indexOf(item));
        outerClickListener.onOuterClick();
        getDialog().dismiss();

        switch (item) {
            case 2:
                RenameDialog.getInstance(getActivity(), video.getTitle(), video.getId(), video)
                        .show(getFragmentManager(), "");

                break;
            case 4:
                VideoPlayerUtils.shareVideo(video.getId(), getActivity());
                break;
            case 3:
                long[] videoId = {video.getId()};
                VideoPlayerUtils.showDeleteDialog(getActivity(), video.getTitle(), videoId);
                break;
            case 5:
                VideoDetailsDialog.getInstance(video)
                        .show(((AppCompatActivity) getContext())
                                .getSupportFragmentManager(), "");


                break;

        }


    }

    public void setOuterClickListener(OuterClickListener outerClickListener) {
        this.outerClickListener = outerClickListener;
    }
}
