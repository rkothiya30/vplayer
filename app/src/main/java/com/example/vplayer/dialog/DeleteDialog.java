package com.example.vplayer.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.activity.VideoPlayerActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DeleteDialog extends DialogFragment {

    Context context;
    String title;
    long[] videoId;
    public static PreferencesUtility preferencesUtility;
    TextView btnDelete, btnCancel, deleteText;


    public static DeleteDialog getInstance(Activity context, String title, long[] id) {
        DeleteDialog dialog = new DeleteDialog();
        dialog.context = context;
        dialog.title = title;
        dialog.videoId = id;
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.WideDialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_delete, container, false);

        btnDelete = view.findViewById(R.id.btnDelete);
        btnCancel = view.findViewById(R.id.btnCancel);
        deleteText = view.findViewById(R.id.deleteText);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesUtility = PreferencesUtility.getInstance(context);
        //btnDelete.setTextColor(ATEUtil.getThemeAccentColor(context));
        deleteText.setText("Are you sure you have to Delete" + " " + title + " ?");

        btnCancel.setOnClickListener(view1 -> {
            getDialog().dismiss();
        });

        btnDelete.setOnClickListener(view1 -> {
            //loadAds.showFullAd(3);
            VideoPlayerUtils.deleteTracks(context, videoId);
            deleteHistoryVideo(videoId[0]);
            getDialog().dismiss();

            if (((Activity) context) instanceof VideoPlayerActivity) {
                ((Activity) context).finish();
            }
        });
    }

    public static void deleteHistoryVideo(long videoId) {
        List<Video> historyVideo = new ArrayList<>();
        historyVideo = preferencesUtility.getHistoryVideos();

        Iterator<Video> iterator = historyVideo.iterator();
        while (iterator.hasNext()) {
            Video currentVideo = iterator.next();
            // Do something with the value
            if (currentVideo.getId() == videoId) {
                iterator.remove();
            }
        }

        preferencesUtility.updateHistoryVideo(historyVideo);

        List<Video> continueWatchingVideo = new ArrayList<>();
        continueWatchingVideo = preferencesUtility.getContinueWatchingVideos();

        Iterator<Video> iterator1 = continueWatchingVideo.iterator();
        while (iterator1.hasNext()) {
            Video currentVideo = iterator1.next();
            // Do something with the value
            if (currentVideo.getId() == videoId) {
                iterator1.remove();
            }
        }

        preferencesUtility.updateContinueWatchingVideo(continueWatchingVideo);

       // RxBus.getInstance().post(new MediaUpdateEvent());
       // RxBus.getInstance().post(new UpdateVideoStatusEvent());
       // RxBus.getInstance().post(new HistoryEvent());
    }
}
