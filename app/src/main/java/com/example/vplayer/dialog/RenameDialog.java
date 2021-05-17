package com.example.vplayer.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RenameDialog extends DialogFragment {

    Context context;
    String title;
    long id;
    String videoPath;
    public static PreferencesUtility preferencesUtility;
    EditText renameText;
    TextView btnCancel, btnRename;

    public static RenameDialog getInstance(Activity context, String title, long id, String videoPath) {
        RenameDialog dialog = new RenameDialog();
        dialog.context = context;
        dialog.title = title;
        dialog.id = id;
        dialog.videoPath = videoPath;
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dialog_rename, container, false);

        renameText = v.findViewById(R.id.renameText);
        btnCancel = v.findViewById(R.id.btnCancel);
        btnRename = v.findViewById(R.id.btnRename);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferencesUtility = PreferencesUtility.getInstance(context);

        //btnRename.setTextColor(ATEUtil.getThemeAccentColor(context));
        renameText.setText(title);
        renameText.setSelection(title.length());
        btnCancel.setOnClickListener(view1 -> {
            getDialog().dismiss();
        });

        btnRename.setOnClickListener(view1 -> {

            if (!renameText.getText().toString().isEmpty()) {
                VideoPlayerUtils.renameVideo(context, id, renameText.getText().toString(), videoPath);
                renameHistoryVideo(id, renameText.getText().toString());
                Toast.makeText(context, "Rename Success", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            } else {
                Toast.makeText(context, "Please enter video name", Toast.LENGTH_SHORT).show();
                return;
            }

        });
    }

    public static void renameHistoryVideo(long videoId, String videoName) {
        List<Video> historyVideo = new ArrayList<>();
        historyVideo = preferencesUtility.getHistoryVideos();

        Iterator<Video> iterator = historyVideo.iterator();
        while (iterator.hasNext()) {
            Video currentVideo = iterator.next();
            // Do something with the value
            if (currentVideo.getId() == videoId) {
                currentVideo.setTitle(videoName);
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
                currentVideo.setTitle(videoName);
            }
        }

        preferencesUtility.updateContinueWatchingVideo(continueWatchingVideo);

       //RxBus.getInstance().post(new MediaUpdateEvent());
       //RxBus.getInstance().post(new UpdateVideoStatusEvent());
       //RxBus.getInstance().post(new HistoryEvent());
    }
}
