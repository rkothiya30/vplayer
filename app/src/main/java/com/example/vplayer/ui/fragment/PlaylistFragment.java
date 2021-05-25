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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.example.vplayer.fragment.event.UpdateAdapterEvent;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.PlayListModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.FolderInFolderActivity;
import com.example.vplayer.ui.activity.PlayPlayListActivity;
import com.example.vplayer.ui.activity.SeeMoreActivity;
import com.example.vplayer.ui.activity.SelectItemActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

public class PlaylistFragment extends Fragment  {

    View view;

    TextView playlist_nums;
    public static RecyclerView videoLList;
    SwipeRefreshLayout refreshLayout;
    ImageView emptyString, iv_add;
    public static PlayListAdapter playListAdapter;
    ArrayList<String> playlists= new ArrayList<>();
    public static LinkedHashMap<String, String> allPlaylist = new LinkedHashMap<>();
    public static String tempPlayListName = "";
    PreferencesUtility preferencesUtility;

    Set<String> keys = new LinkedHashSet<>();

    ArrayList<String> listkeys = new ArrayList<>();

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
        playlist_nums = view.findViewById(R.id.playlist_nums);
        preferencesUtility = PreferencesUtility.getInstance(getContext());
        refreshLayout.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        subscribeUpdateAdapterEvent();

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
                        tempPlayListName = edtFileName.getText().toString();

                        playListAdapter.notifyDataSetChanged();
                        startActivity(new Intent(getActivity(), SelectItemActivity.class));
                    }
                });

                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }
        });
        playListAdapter.setOnItemClickListener(new PlayListAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

                keys = allPlaylist.keySet();
                listkeys.clear();
                listkeys = new ArrayList<>();
                listkeys.addAll(keys);
                Intent inc = new Intent(getActivity(), PlayPlayListActivity.class);
                inc.putExtra("Position", position);

                inc.putExtra("PlayName", listkeys.get(position));
                startActivity(inc);
            }
        });
    }

    public void initView() {


        allPlaylist = preferencesUtility.getPlaylists();

        PlayListModel playListModel = new PlayListModel(new ArrayList<AudioModel>(), new ArrayList<Video>());
        String playListString = new Gson().toJson(playListModel);
        if(preferencesUtility.getPlaylists().size() < 1) {
            allPlaylist.put("My Favourites", playListString);
            preferencesUtility.setPlaylists(allPlaylist);
        }
        playListAdapter = new PlayListAdapter(getContext(), allPlaylist);
        videoLList.setLayoutManager(new LinearLayoutManager(getContext()));

        videoLList.setNestedScrollingEnabled(false);
        videoLList.setHasFixedSize(true);

        videoLList.setDrawingCacheEnabled(true);
        videoLList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        videoLList.setAdapter(playListAdapter);




    }

    @Override
    public void onResume() {
        playlist_nums.setText(allPlaylist.size() +" Playlists");

        super.onResume();
    }

    private void subscribeUpdateAdapterEvent() {
        Subscription subscription = RxBus.getInstance()
                .toObservable(UpdateAdapterEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .subscribe(new Action1<UpdateAdapterEvent>() {
                    @Override
                    public void call(UpdateAdapterEvent event) {
                        //videoList.remove(event.getPosition());
                        PlaylistFragment.playListAdapter = new PlayListAdapter(getActivity(), preferencesUtility.getPlaylists());
                        PlaylistFragment.videoLList.setAdapter(PlaylistFragment.playListAdapter);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
        RxBus.getInstance().addSubscription(this, subscription);
    }
}