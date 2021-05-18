package com.example.vplayer.ui.fragment;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.MusicAdapter;
import com.example.vplayer.fragment.adapter.MusicSAdapter;
import com.example.vplayer.fragment.adapter.VideoSAdapter;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.service.VideoDataService;
import com.example.vplayer.ui.activity.SelectItemActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.vplayer.ui.fragment.MusicFragment.audioList;

public class MusicSFragment extends Fragment {



    View view;
    RecyclerView recycler_view;
    ImageView emptyString;
    MusicSAdapter adapter;
    ProgressDialog loadingDialog;
    SwipeRefreshLayout refreshLayout;
    TextView select_all;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.music_s_fragment, container, false);

        recycler_view = view.findViewById(R.id.recycler_view);
        emptyString = view.findViewById(R.id.emptyString);
        // progress_bar = view.findViewById(R.id.progress_bar);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        select_all = view.findViewById(R.id.select_all);
        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshLayout.setEnabled(false);
        initView();


    }

    public void initView() {
        //progress_bar.setVisibility(View.VISIBLE);
        new Thread(this::getAllAudioList).start();



        /*loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Fetching list...");
        loadingDialog.setCanceledOnTouchOutside(false);*/

    }

    private void getAllAudioList() {



        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                setAdapter();
            }
        });

    }


    public void setAdapter() {
        //progress_bar.setVisibility(View.GONE);

            recycler_view.setVisibility(View.VISIBLE);
            emptyString.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recycler_view.setLayoutManager(layoutManager);
            adapter = new MusicSAdapter(getActivity(), audioList, true);
            recycler_view.setAdapter(adapter);

            adapter.setOnItemClickListener(new MusicSAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    AudioModel current = audioList.get(position);
                    if(current.isSelected()) {

                        audioList.get(position).setSelected(false);
                        SelectItemActivity.SelectCount--;
                    }
                    else {
                        audioList.get(position).setSelected(true);
                        SelectItemActivity.SelectCount++;
                    }
                        adapter.notifyItemChanged(position);


                }
            });

        select_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioModel current = new AudioModel();
                for(int i = 0; i< audioList.size(); i++){
                    current = audioList.get(i);
                    if(current.isSelected()) {
                        current.setSelected(false);
                        SelectItemActivity.SelectCount--;
                    }
                    else{
                        current.setSelected(true);
                        SelectItemActivity.SelectCount++;
                    }
                    if(current.isSelected()){
                        select_all.setText("UNSELECT ALL");

                    } else {
                        select_all.setText("SELECT ALL");


                    }
                    SelectItemActivity.text_title.setText(SelectItemActivity.SelectCount + " selected");
                    adapter.notifyDataSetChanged();

                }
            }});



    }

}