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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.RenamePlaylistDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.adapter.OnMenuAdapter;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
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
    private static List<Video> videoList ;
    private static List<AudioModel> audioList;
    private static AudioModel audioModel;
    private static String title;
    private static String playListName;

    public static OnMenuFragment newInstance(int checks, List<Video> videoLis, List<AudioModel> audioLis,String playListNam) {
        OnMenuFragment fragment = new OnMenuFragment();
        audioList = audioLis;
        videoList = videoLis;
        check = checks;
        playListName = playListNam;
        return fragment;
    }


    public static OnMenuFragment newInstance(int checks, Video vide) {
        OnMenuFragment fragment = new OnMenuFragment();
        title = vide.getTitle();
        video = vide;
        check = checks;
        return fragment;
    }

    public static OnMenuFragment newInstance(int checks, AudioModel audioMode) {
        OnMenuFragment fragment = new OnMenuFragment();

        audioModel = audioMode;
        title = audioMode.getName();
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
        }else if(check == -1){
            text_title.setText("Add to playlist");
        } else if(check == -3){
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

            case 1:
                if(check == -2)
                showShortDialog(-2, video);
                else if(check == -3)
                    showShortDialog(-3, audioModel);
                else if(check == -1)
                    showShortDialog(-1, videoList, audioList);
                break;

            case 2:
                if(check == -2) {
                    RenameDialog.getInstance(getActivity(), OnMenuFragment.video.getTitle(), OnMenuFragment.video.getId(), OnMenuFragment.video)
                            .show(getFragmentManager(), "");
                } else if(check == -1){
                    RenamePlaylistDialog.getInstance(getActivity(), playListName)
                            .show(getFragmentManager(), "");
                }
                break;
            case 4:
                VideoPlayerUtils.shareVideo(OnMenuFragment.video.getId(), getActivity());
                break;
            case 3:
                long[] videoId = {OnMenuFragment.video.getId()};
                VideoPlayerUtils.showDeleteDialog(getActivity(), OnMenuFragment.video.getTitle(), videoId);
                break;
            case 5:
                VideoDetailsDialog.getInstance(OnMenuFragment.video)
                        .show(((AppCompatActivity) getContext())
                                .getSupportFragmentManager(), "");


                break;

        }


    }

    public void setOuterClickListener(OuterClickListener outerClickListener) {
        this.outerClickListener = outerClickListener;
    }

    public void showShortDialog ( int adapterPosition, List<Video> videoList, List<AudioModel> audioList){
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance(adapterPosition, videoList, audioList, getContext());
        //bottomSheetDialog.setOuterClickListener(this);
        addPlaylistFragment.show(getFragmentManager(), "Bottom Sheet Dialog Fragment");


    }

    public void showShortDialog ( int adapterPosition, Video video){
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance(adapterPosition, video, getContext());
        //bottomSheetDialog.setOuterClickListener(this);
        addPlaylistFragment.show(getFragmentManager(), "Bottom Sheet Dialog Fragment");


    }


    public void showShortDialog ( int adapterPosition, AudioModel audioModel){
        AddPlaylistFragment addPlaylistFragment = AddPlaylistFragment.newInstance(adapterPosition, audioModel, getContext());
        //bottomSheetDialog.setOuterClickListener(this);
        addPlaylistFragment.show(getFragmentManager(), "Bottom Sheet Dialog Fragment");


    }
}
