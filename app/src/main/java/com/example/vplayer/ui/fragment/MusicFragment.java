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
import com.example.vplayer.fragment.event.RenameEvent;
import com.example.vplayer.fragment.utils.Constant;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.service.MusicDataService;
import com.example.vplayer.ui.activity.PlayingSongActivity;
import com.example.vplayer.ui.activity.SearchActivity;
import com.example.vplayer.ui.activity.SettingsActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.provider.MediaStore.Images.Thumbnails.IMAGE_ID;
import static com.example.vplayer.service.MusicDataService.audioList;


public class MusicFragment extends Fragment {

    View view;

    RecyclerView recycler_view;
    ImageView emptyString;
    public static MusicAdapter adapter;
    ProgressDialog loadingDialog;
    SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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
        renameEvent();

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_video, menu);





        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:
                Intent inc = new Intent(getActivity(), SettingsActivity.class);
                startActivity(inc);

                break;

            case R.id.action_search:
                Intent i = new Intent(getActivity(), SearchActivity.class);
                i.putExtra("Activity", "Music");
                startActivity(i);
                break;




        }

        return super.onOptionsItemSelected(item);
    }

    private void getAllAudioList() {

        while (true) {
            if (MusicDataService.isComplete) {
                break;
            }
        }
        new Thread(this::resumeServiceData).start();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                setAdapter();
            }
        });

    }

    private void resumeServiceData() {
        for(int i =0; i<audioList.size(); i++) {

            MediaMetadataRetriever mediaMetadataRetriever= new MediaMetadataRetriever();
            try {
                mediaMetadataRetriever.setDataSource(audioList.get(i).getPath());
            } catch(Exception e){
                e.printStackTrace();
            }

            String duration = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


            File file = new File(audioList.get(i).getPath());
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, audioList.get(i).getAlbumId());


            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), albumArtUri);

            } catch (FileNotFoundException exception) {
            } catch (IOException e) {

                e.printStackTrace();
            }

            audioList.get(i).setDuration(duration);
            audioList.get(i).setBitmap(bitmap);


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

    private void renameEvent() {
        Subscription subscription = RxBus.getInstance().toObservable(RenameEvent.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).distinctUntilChanged().subscribe(new Action1<RenameEvent>() {
            @Override
            public void call(RenameEvent event) {
                if (event.getNewFile() != null && event.getOldFile() != null) {

                    if (event.getNewFile().exists()) {

                        if (audioList != null && audioList.size() != 0)
                            for (int i = 0; i < audioList.size(); i++) {

                                if (event.getOldFile().getPath().equalsIgnoreCase(audioList.get(i).getPath())) {

                                    audioList.get(i).setPath(event.getNewFile().getPath());
                                    audioList.get(i).setName(event.getNewFile().getName());

                                    break;
                                }

                            }

                        /*if (videoAdapter != null) {
                            videoAdapter.notifyDataSetChanged();
                        } else {*/
                        setAdapter();
                        /* }*/

                        /*if (documentList != null && documentList.size() != 0) {
                            recyclerView.setVisibility(View.VISIBLE);
                            llEmpty.setVisibility(View.GONE);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            llEmpty.setVisibility(View.VISIBLE);
                        }*/


                    }
                }


            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
            }
        });
        RxBus.getInstance().addSubscription(this, subscription);
    }

}