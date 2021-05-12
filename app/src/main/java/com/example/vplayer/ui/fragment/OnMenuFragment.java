package com.example.vplayer.ui.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.OnMenuAdapter;
import com.example.vplayer.fragment.interfaces.SortByClickListener;
import com.example.vplayer.fragment.utils.PreferencesUtility;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.vplayer.service.VideoDataService.videobuckets;

public class OnMenuFragment extends BottomSheetDialogFragment implements OnMenuAdapter.ItemListener {

    private static int position;
    OnMenuAdapter onMenuAdapter;
    List<String> sortByList;
    PreferencesUtility preferencesUtility;
    SortByClickListener sortByClickListener;
    private static int check;
    private static String title;


    public static OnMenuFragment newInstance(int adapterPosition) {
        OnMenuFragment fragment = new OnMenuFragment();
        position = adapterPosition;
        return fragment;
    }


    public static OnMenuFragment newInstance(int checks, String t) {
        OnMenuFragment fragment = new OnMenuFragment();
        title = t;
        check = checks;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        final String[] sortList = getResources().getStringArray(R.array.menu_array);
        sortByList = new ArrayList<String>(Arrays.asList(sortList));



    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.dialog_on_menu, null);
        dialog.setContentView(contentView);
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        TextView text_title = contentView.findViewById(R.id.text_title);
        if(check == -2){
            text_title.setText(title);
        } else {
            text_title.setText(videobuckets.get(position).getFolderName());

        }
        RecyclerView sortList = contentView.findViewById(R.id.sortList);
        sortList.setHasFixedSize(true);
        sortList.setLayoutManager(new LinearLayoutManager(getContext()));


        onMenuAdapter = new OnMenuAdapter(getContext(), sortByList, this);
        sortList.setAdapter(onMenuAdapter);
    }

    @Override
    public void onSortByClick(String item) {
        preferencesUtility.setSortByVideo(sortByList.indexOf(item));
        sortByClickListener.onSortByClick();
        getDialog().dismiss();
    }

    public void setSortByClickListener(SortByClickListener sortByClickListener) {
        this.sortByClickListener = sortByClickListener;
    }
}
