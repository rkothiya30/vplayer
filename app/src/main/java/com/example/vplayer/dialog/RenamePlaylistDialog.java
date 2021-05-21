package com.example.vplayer.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.vplayer.R;
import com.example.vplayer.fragment.event.RenameEvent;
import com.example.vplayer.fragment.event.UpdateAdapterEvent;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.example.vplayer.fragment.utils.RxBus;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;
import com.example.vplayer.ui.fragment.PlaylistFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.vplayer.ui.fragment.PlaylistFragment.allPlaylist;

public class RenamePlaylistDialog extends DialogFragment {

    Context context;
    String title;
    long id;
    public static PreferencesUtility preferencesUtility;
    EditText renameText;
    TextView btnCancel, btnRename;

    public static RenamePlaylistDialog getInstance(Activity context, String title) {
        RenamePlaylistDialog dialog = new RenamePlaylistDialog();
        dialog.context = context;
        dialog.title = title;

        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.WideDialog);
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
       /* renameText.setText(title);*/
        //renameText.setSelection(title.length());
        btnCancel.setOnClickListener(view1 -> {
            getDialog().dismiss();
        });

        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!renameText.getText().toString().isEmpty()) {
                    if (!renameText.getText().toString().equalsIgnoreCase(title)) {

                                    getDialog().dismiss();
                                    // set rename
                                    LinkedHashMap<String, String> playlists = preferencesUtility.getPlaylists();
                                    if (playlists.containsKey(title)) {
                                        String s = playlists.get(title);
                                        playlists.remove(title);
                                        playlists.put(renameText.getText().toString(), s);
                                        allPlaylist.put(renameText.getText().toString(), s);
                                    }
                                    preferencesUtility.setPlaylists(playlists);

                        PlaylistFragment.playListAdapter.notifyDataSetChanged();
                        RxBus.getInstance().post(new UpdateAdapterEvent());

                    } else {

                        // set rename
                        getDialog().dismiss();
                        Toast.makeText(context, "Same playlist name cannot be updated", Toast.LENGTH_SHORT).show();

                    }

                } else {
                    Toast.makeText(getActivity(), "New name can't be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

   /* private void showRenameValidationDialog() {
        Dialog validationDialog = new Dialog(getContext(), R.style.WideDialog);
        validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        validationDialog.setCancelable(true);
        validationDialog.setContentView(R.layout.dialog_rename_same_name_validation);
        validationDialog.setCanceledOnTouchOutside(true);
        validationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        validationDialog.getWindow().setGravity(Gravity.CENTER);

        LinearLayout btn_ok;
        btn_ok = validationDialog.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validationDialog.dismiss();
            }
        });

        validationDialog.show();
    }
*/
  /*  private void reNameFile(File file, String newName) {
        File file2 = new File(file.getParent() + "/" + newName);
        Log.e("1", "file name: " + file.getPath());
        Log.e("2", "file2 name: " + file2.getPath());
        if (file2.exists()) {
            Log.e("rename", "File already exists!");
            showRenameValidationDialog();
        } else {
            boolean renamed = false;

            if (sdCardPath != null && !sdCardPath.equalsIgnoreCase("") && file.getPath().contains(sdCardPath)) {
                renamed = VideoPlayerUtils.renameFile(file, newName, getContext());
            } else {
                renamed = file.renameTo(file2);


            }

            if (renamed) {
                Log.e("LOG", "File renamed...");
                MediaScannerConnection.scanFile(getContext(), new String[]{file2.getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        // Log.i("ExternalStorage", "Scanned " + path + ":" + uri);
                    }
                });


                Toast.makeText(getContext(), "Rename file successfully", Toast.LENGTH_SHORT).show();
                RxBus.getInstance().post(new RenameEvent(file, file2));
            } else {
                Log.e("LOG", "File not renamed...");
            }
//            storageList.clear();
//            getFilesList(arrayListFilePaths.get(arrayListFilePaths.size() - 1));

        }
    }*/

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
