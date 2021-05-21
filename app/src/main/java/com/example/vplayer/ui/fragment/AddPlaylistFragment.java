package com.example.vplayer.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.dialog.RenameDialog;
import com.example.vplayer.dialog.VideoDetailsDialog;
import com.example.vplayer.fragment.adapter.AddPlaylistAdapter;
import com.example.vplayer.fragment.adapter.OnMenuAdapter;
import com.example.vplayer.fragment.adapter.PlayListAdapter;
import com.example.vplayer.fragment.interfaces.OuterClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.PlayListModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.SelectItemActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.vplayer.service.VideoDataService.videobuckets;
import static com.example.vplayer.ui.fragment.PlaylistFragment.allPlaylist;

public class AddPlaylistFragment extends BottomSheetDialogFragment  {

    private static int position;
    public AddPlaylistAdapter addPlaylistAdapter;
    private static PreferencesUtility preferencesUtility;
    List<String> sortByList;

    OuterClickListener outerClickListener;
    RecyclerView sortList;
    private static int check;
    private static Video video;
    private static String title;
    static Context context;
    private static AudioModel audioModel;
    private static List<Video> videoList;
    private static List<AudioModel> audioList;


    public static AddPlaylistFragment newInstance(int checks, List<Video> videoLis, List<AudioModel> audioLis, Context contex) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();

        videoList = videoLis;
        audioList = audioLis;
        check = checks;
        preferencesUtility = PreferencesUtility.getInstance(contex);
        context = contex;
        return fragment;
    }


    public static AddPlaylistFragment newInstance(int checks, Video vide, Context contex) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();
        title = vide.getTitle();
        video = vide;
        check = checks;
        preferencesUtility = PreferencesUtility.getInstance(contex);
        context = contex;
        return fragment;
    }

    public static AddPlaylistFragment newInstance(int checks, AudioModel audioMode, Context context) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();

        audioModel = audioMode;
        title = audioMode.getName();
        check = checks;
        preferencesUtility = PreferencesUtility.getInstance(context);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);







    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.dialog_on_add_playlist, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        TextView text_title = contentView.findViewById(R.id.text_title);
        sortList = contentView.findViewById(R.id.sortList);

        text_title.setText("Add to playlist");
        RelativeLayout ll_create = contentView.findViewById(R.id.ll_create);

        initView();

        ll_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();


                Dialog dialog = new Dialog(getActivity(), R.style.WideDialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_create_playlist);
                dialog.setCanceledOnTouchOutside(true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.CENTER);

                AppCompatEditText edtFileName;
                LinearLayout btn_cancel, btn_ok;
                edtFileName = dialog.findViewById(R.id.edt_file_name);
                edtFileName.setFocusable(true);
                edtFileName.requestFocus();
                btn_cancel = dialog.findViewById(R.id.btn_cancel);
                btn_ok = dialog.findViewById(R.id.btn_ok);

                //edtFileName.setText(file.getName());

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //playlists.add(edtFileName.getText().toString());
                        dialog.dismiss();
                        PlaylistFragment.tempPlayListName = edtFileName.getText().toString();

                        addPlaylistAdapter.notifyDataSetChanged();
                        startActivity(new Intent(context, SelectItemActivity.class));
                    }
                });

                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }

        });

        addPlaylistAdapter.setOnItemClickListener(new AddPlaylistAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

            }
        });



    }

    /*@Override
    public void onItemClick(int item) {
       // preferencesUtility.setSortByVideo(sortByList.indexOf(item));
        outerClickListener.onOuterClick();
        getDialog().dismiss();

        switch (item) {
            
            case 1:


            
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


    }*/

    public void setOuterClickListener(OuterClickListener outerClickListener) {
        this.outerClickListener = outerClickListener;
    }



    public void initView() {


        allPlaylist = preferencesUtility.getPlaylists();

        PlayListModel playListModel = new PlayListModel(new ArrayList<AudioModel>(), new ArrayList<Video>());
        String playListString = new Gson().toJson(playListModel);
        if(preferencesUtility.getPlaylists().size() < 2) {
            allPlaylist.put("My Favourites", playListString);
            preferencesUtility.setPlaylists(allPlaylist);
        }
        if(check == -2)
        addPlaylistAdapter = new AddPlaylistAdapter(check, getContext(), allPlaylist, video, getDialog());
        else if(check == -3)
            addPlaylistAdapter = new AddPlaylistAdapter(check, getContext(), allPlaylist, audioModel, getDialog());
        else if(check == -1)
            addPlaylistAdapter = new AddPlaylistAdapter(check, getContext(), allPlaylist, videoList, audioList, getDialog());
        sortList.setLayoutManager(new LinearLayoutManager(getContext()));

        sortList.setNestedScrollingEnabled(false);
        sortList.setHasFixedSize(true);

        sortList.setDrawingCacheEnabled(true);
        sortList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        sortList.setAdapter(addPlaylistAdapter);




    }

}
