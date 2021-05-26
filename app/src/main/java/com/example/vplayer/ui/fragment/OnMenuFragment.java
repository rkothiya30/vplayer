package com.example.vplayer.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
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
import com.example.vplayer.dialog.DeletePlaylistDialog;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.RenameMusicDialog;
import com.example.vplayer.dialog.RenamePlaylistDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.adapter.OnMenuAdapter;
import com.example.vplayer.fragment.event.UpdateAdapterEvent;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.PlayListModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.MusicService;
import com.example.vplayer.service.VideoPlayAsAudioService;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.vplayer.fragment.utils.Constant.EXTRA_VIDEO_POSITION;
import static com.example.vplayer.service.VideoDataService.videobuckets;
import static com.example.vplayer.ui.fragment.PlaylistFragment.allPlaylist;

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
    MusicService musicService;



    boolean isBackgroundEnable = false;

    public static OnMenuFragment newInstance(int checks, List<Video> videoLis, List<AudioModel> audioLis,String playListNam) {
        OnMenuFragment fragment = new OnMenuFragment();
        audioList = audioLis;
        videoList = videoLis;

        check = checks;
        playListName = playListNam;
        return fragment;
    }


    public static OnMenuFragment newInstance(int checks,int positio, List<Video> videoLis, Video vide) {
        OnMenuFragment fragment = new OnMenuFragment();
        title = vide.getTitle();
        video = vide;
        videoList = videoLis;
        position = positio;
        check = checks;
        return fragment;
    }

    public static OnMenuFragment newInstance(int checks, AudioModel audioMode, int positio) {
        OnMenuFragment fragment = new OnMenuFragment();
        position = positio;
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

        preferencesUtility = PreferencesUtility.getInstance(getActivity());

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


        onMenuAdapter = new OnMenuAdapter(getContext(),check, sortByList, this, video);
        sortList.setAdapter(onMenuAdapter);
    }

    @Override
    public void onItemClick(int item) {
       // preferencesUtility.setSortByVideo(sortByList.indexOf(item));
        outerClickListener.onOuterClick();
        getDialog().dismiss();

        switch (item) {

            case 0:

                if(check == -2){

                        isBackgroundEnable = true;

                    startBackgroundVideoPlayService();
                } else if(check == -1 || check == -4){

                    isBackgroundEnable = true;
                    position = 0;
                    startBackgroundVideoPlayService();
                } else if(check == -3){
                    Intent intent = new Intent(getContext(), MusicService.class);
                    intent.putExtra("ServicePosition", position);
                    intent.putExtra("ActivityName", "OnMenuFragment");
                    getContext().startService(intent);



                    if(musicService != null) {
                        musicService.showNotification(R.drawable.ic_pause);
                    }
                }
                break;

            case 1:
                if(check == -2)
                showShortDialog(-2, video);
                else if(check == -3 )
                    showShortDialog(-3, audioModel);
                else if(check == -1 || check == -4)
                    showShortDialog(-1, videoList, audioList);
                break;

            case 2:
                if(check == -2) {
                    RenameDialog.getInstance(getActivity(), OnMenuFragment.video.getTitle(), OnMenuFragment.video.getId(), OnMenuFragment.video)
                            .show(getFragmentManager(), "");
                } else if(check == -1){
                    RenamePlaylistDialog.getInstance(getActivity(), playListName)
                            .show(getFragmentManager(), "");
                } else if(check == -3){
                    RenameMusicDialog.getInstance(getActivity(),title, audioModel.getId(), audioModel)
                            .show(getFragmentManager(), "");
                } else if(check == -4){

                }

                break;
            case 4:
                if(check == -2) {
                    VideoPlayerUtils.shareVideo(OnMenuFragment.video.getId(), getActivity());
                } else if(check == -1 || check == -4){
                    List<String> paths = new ArrayList<>();
                    for(int i = 0; i<videoList.size(); i++){
                        paths.add(videoList.get(i).getFullPath());
                    }
                    for(int i = 0; i<audioList.size(); i++){
                        paths.add(audioList.get(i).getPath());
                    }
                    VideoPlayerUtils.share( getContext(), paths);
                } else if(check == -3){
                    VideoPlayerUtils.shareAudio(OnMenuFragment.audioModel.getId(), getActivity());
                }

                break;
            case 3:
                if(check==-2) {
                    long[] videoId = {OnMenuFragment.video.getId()};
                    VideoPlayerUtils.showDeleteDialog(getActivity(), OnMenuFragment.video.getTitle(), videoId);

                } else if(check == -1){
                    DeletePlaylistDialog.getInstance(getActivity(), playListName)
                            .show(getFragmentManager(), "");
                }  else if(check == -3){
                    RenameMusicDialog.getInstance(getActivity(),title, audioModel.getId(), audioModel)
                            .show(getFragmentManager(), "");
                } else if(check == -4){
                    LinkedHashMap<String, String> playlists = preferencesUtility.getPlaylists();
                    String playListString;
                    if (playlists.containsKey(playListName)) {

                        String s = playlists.get(playListName);

                        PlayListModel playListModel1 = new Gson().fromJson(s, PlayListModel.class);
                        List<Video> videoList1 = playListModel1.getVideoList();
                        videoList1.clear();
                        List<AudioModel> audioList1 = playListModel1.getAudioList();
                        audioList1.clear();



                        playListModel1 =new PlayListModel(audioList1, videoList1);
                        playListString =new Gson().toJson(playListModel1);

                    allPlaylist.put(playListName, playListString);



                    }
                    preferencesUtility.setPlaylists(allPlaylist);
                    RxBus.getInstance().post(new UpdateAdapterEvent());
                }
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

    public void startBackgroundVideoPlayService() {
        if (isBackgroundEnable) {
            Video video =  videoList.get(position);
            video.setVideoLastPlayPosition(0);
            videoList.set(position, video);

            preferencesUtility.setVideoList(videoList);

            getContext().stopService(new Intent(getContext(), VideoPlayAsAudioService.class));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(new Intent(getContext(), VideoPlayAsAudioService.class).putExtra(EXTRA_VIDEO_POSITION, position));
            } else {
                getContext().startService(new Intent(getContext(), VideoPlayAsAudioService.class).putExtra(EXTRA_VIDEO_POSITION, position));
            }
        }
    }
}
