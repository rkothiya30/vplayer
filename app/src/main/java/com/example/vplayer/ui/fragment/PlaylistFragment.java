package com.example.vplayer.ui.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.PlayListAdapter;
import com.example.vplayer.fragment.adapter.VideoFolderAdapter;
import com.example.vplayer.ui.activity.FolderInFolderActivity;
import com.example.vplayer.ui.activity.SeeMoreActivity;
import com.example.vplayer.ui.activity.SelectItemActivity;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class PlaylistFragment extends Fragment  {

    View view;

    RecyclerView videoLList;
    SwipeRefreshLayout refreshLayout;
    ImageView emptyString, iv_add;
    PlayListAdapter playListAdapter;
    ArrayList<String> playlists= new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_playlist, container, false);

        videoLList = view.findViewById(R.id.videoList);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        emptyString = view.findViewById(R.id.emptyString);
        iv_add = view.findViewById(R.id.iv_add);

        refreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();

        iv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                        playlists.add(edtFileName.getText().toString());
                        dialog.dismiss();

                        playListAdapter.notifyDataSetChanged();
                        startActivity(new Intent(getActivity(), SelectItemActivity.class));
                    }
                });


                dialog.show();
            }
        });
    }

    public void initView() {

        playListAdapter = new PlayListAdapter(getContext(), playlists);
        videoLList.setLayoutManager(new LinearLayoutManager(getContext()));

        videoLList.setNestedScrollingEnabled(false);
        videoLList.setHasFixedSize(true);

        videoLList.setDrawingCacheEnabled(true);
        videoLList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        videoLList.setAdapter(playListAdapter);




    }


}