package com.example.vplayer.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.MusicAdapter;
import com.example.vplayer.fragment.adapter.PlayListAdapter;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.ui.activity.PlayingSongActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static android.provider.MediaStore.Images.Thumbnails.IMAGE_ID;


public class MusicFragment extends Fragment {

    View view;
    public static ArrayList<AudioModel> audioList = new ArrayList<>();
    RecyclerView recycler_view;
    ImageView emptyString;
    public static MusicAdapter adapter;
    ProgressDialog loadingDialog;
    SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate( R.layout.fragment_music, container, false);

        recycler_view = view.findViewById(R.id.recycler_view);
        emptyString = view.findViewById(R.id.emptyString);
       // progress_bar = view.findViewById(R.id.progress_bar);
        refreshLayout = view.findViewById(R.id.refreshLayout);
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



       /* loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Fetching list...");
        loadingDialog.setCanceledOnTouchOutside(false);*/

    }

    private void getAllAudioList() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST, MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media._ID};
        Cursor c = getActivity().getContentResolver().query(uri, projection, null, null,
                "LOWER(" + MediaStore.Audio.Media.DATE_MODIFIED + ") DESC");


        if (c != null) {
            /*ArrayList<String> favList = PreferencesManager.getFavouriteList(this);
            if (favList == null) {
                favList = new ArrayList<>();
            }*/
            while (c.moveToNext()) {

                AudioModel audioModel = new AudioModel();
                String path = c.getString(0);
                String album = c.getString(1);
                String artist = c.getString(2);
                Long albumId = c.getLong(3);
                String id = c.getString(4);

                MediaMetadataRetriever mediaMetadataRetriever= new MediaMetadataRetriever();
                try {
                    mediaMetadataRetriever.setDataSource(path);
                } catch(Exception e){
                    e.printStackTrace();
                }

                String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


                File file = new File(path);
                Uri sArtworkUri = Uri
                        .parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);


                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), albumArtUri);

                } catch (FileNotFoundException exception) {
                } catch (IOException e) {

                    e.printStackTrace();
                }


                String name = path.substring(path.lastIndexOf("/") + 1);

                if (file.exists()) {
                    audioModel.setName(name);
                    audioModel.setAlbum(album);
                    audioModel.setArtist(artist);
                    audioModel.setPath(path);
                    audioModel.setDuration(duration);
                    /*if (favList.contains(path)) {
                        audioModel.setFavorite(true);
                    } else {
                        audioModel.setFavorite(false);
                    }*/
                    audioModel.setId(id);
                    audioModel.setPlay(false);
                    audioModel.setSelected(false);
                    audioModel.setCheckboxVisible(false);
                    audioModel.setBitmap(bitmap);

                    audioList.add(audioModel);
                }
            }
            c.close();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                setAdapter();
            }
        });

    }


    public void setAdapter() {
        //progress_bar.setVisibility(View.GONE);
        if (audioList != null && audioList.size() != 0) {
            recycler_view.setVisibility(View.VISIBLE);
            emptyString.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recycler_view.setLayoutManager(layoutManager);
            adapter = new MusicAdapter(getActivity(), audioList, false);
            recycler_view.setAdapter(adapter);

            adapter.setOnItemClickListener(new MusicAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {


                       /* File file = new File(audioList.get(position).getPath());
                        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, VideoPlayerUtils.getMimeTypeFromFilePath(file.getPath()));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        startActivity(Intent.createChooser(intent, "Open with"));*/

                    Intent i = new Intent(getActivity(), PlayingSongActivity.class);
                    i.putExtra("Position", position);
                    i.putExtra("ActivityName", "MusicFragment");
                    getActivity().startActivity(i);


                }
            });


        } else {
            recycler_view.setVisibility(View.GONE);
            emptyString.setVisibility(View.VISIBLE);
        }
    }

}