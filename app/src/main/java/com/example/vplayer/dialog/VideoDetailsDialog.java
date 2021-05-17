package com.example.vplayer.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.vplayer.R;
import com.example.vplayer.fragment.utils.VideoPlayerUtils;
import com.example.vplayer.model.Video;

public class VideoDetailsDialog extends DialogFragment {

    Video video;
    TextView videoName, videoPath, videoModifyDate, videoResolution, videoSize, videoDuration, btnOk;
    
    public static VideoDetailsDialog getInstance(Video video) {
        VideoDetailsDialog dialog = new VideoDetailsDialog();

        dialog.video = video;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        /*Window window = getActivity().getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);*/
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View v = inflater.inflate( R.layout.dialog_video_details, container, false);

       videoName = v.findViewById(R.id.videoName);
       videoPath = v.findViewById(R.id.videoPath);
       videoModifyDate = v.findViewById(R.id.videoModifyDate);
       videoResolution = v.findViewById(R.id.videoResolution);
       videoSize = v.findViewById(R.id.videoSize);
       videoDuration = v.findViewById(R.id.videoDuration);
       btnOk = v.findViewById(R.id.btnOk);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoName.setText(video.getTitle());
        videoPath.setText(video.getFullPath());
        videoModifyDate.setText(VideoPlayerUtils.getDateFormate(video.getModifiyDate()));
        videoResolution.setText(video.getResolution());
        videoSize.setText(VideoPlayerUtils.formateSize(video.getSize()));
        String duration = VideoPlayerUtils.makeShortTimeString(getContext(), video.getVideoDuration() / 1000);
        videoDuration.setText(duration);
        btnOk.setOnClickListener(view1 -> {
            getDialog().dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
