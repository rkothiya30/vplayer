package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.SearchAdapter;
import com.example.vplayer.model.AudioModel;
import com.example.vplayer.model.Video;
import com.example.vplayer.service.MusicDataService;
import com.example.vplayer.service.VideoDataService;

import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {

    ArrayList<Object> searchList = new ArrayList<>();
    ArrayList<Object> searchDataList = new ArrayList<>();
    //ArrayList<AudioModel> searchList1 = new ArrayList<>();
    //ArrayList<AudioModel> searchDataList1 = new ArrayList<>();
    ImageView iv_back_search, iv_clear;
    EditText edt_search;
    RecyclerView recycler_view;
    LinearLayout ll_empty;
    SearchAdapter adapter;
    ProgressBar progress_bar;
    ProgressDialog loadingDialog;
    String activity;
    AppCompatTextView tv_no_data;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkGray, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.darkGray));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getSupportActionBar().hide();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkGray)));

        iv_back_search = findViewById(R.id.iv_back_search);
        edt_search = findViewById(R.id.edt_search);
        recycler_view = findViewById(R.id.recycler_view);
        ll_empty = findViewById(R.id.ll_empty);
        progress_bar = findViewById(R.id.progress_bar);
        iv_clear = findViewById(R.id.iv_clear);
        tv_no_data = findViewById(R.id.tv_no_data);

        activity = getIntent().getExtras().getString("Activity", "");
        if(activity.equals("Video")){
            edt_search.setHint("Search Video");
            tv_no_data.setText("No Video");
        } else if(activity.equals("Music")){
            edt_search.setHint("Search Audio");
            tv_no_data.setText("No Audio");
        }

        intView();

        iv_back_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edt_search.getText().clear();
                iv_clear.setVisibility(View.GONE);
                setClear();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm.isAcceptingText()) {
                    //writeToLog("Software Keyboard was shown");

                } else {
                    showKeyboard();
                    // writeToLog("Software Keyboard was not shown");
                }
            }
        });

    }


    public void intView() {



        searchList = new ArrayList<>();

        setSearchBarData();

        setAdapter();

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getString(R.string.app_name));
        if (!file.exists()) {
            file.mkdirs();
        }





        progress_bar.setVisibility(View.VISIBLE);
        new Thread(this::getDataList).start();



        loadingDialog = new ProgressDialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("Delete file...");
        loadingDialog.setCanceledOnTouchOutside(false);


    }



    public void setSearchBarData() {
        edt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {


            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String strSearch = charSequence.toString();
                Log.e("searchText addChange: ", strSearch);
                // hideSoftKeyboard(binding.edtSearch);
                setSearch(strSearch);

                if (i2 == 0) {
                    iv_clear.setVisibility(View.GONE);
                } else {
                    iv_clear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edt_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.e("TAG", "edtSearch: " + edt_search.getText().toString());

                    if (!edt_search.getText().toString().isEmpty() && edt_search.getText().toString().trim().length() != 0) {

                        String strSearch = edt_search.getText().toString().trim();

                        hideSoftKeyboard(edt_search);
                        setSearch(strSearch);


                    } else {
                        Toast.makeText(SearchActivity.this, "Enter file name", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
                return false;
            }
        });

    }

    public void setSearch(String searchText) {
        Log.e("searchText: ", searchText);
        searchDataList.clear();
        if(activity.equals("Video")) {
            for (int i = 0; i < searchList.size(); i++) {
                if(searchList.get(i) instanceof Video){
                    Video v =(Video) searchList.get(i);

                    String s = v.getTitle();
                //if the existing elements contains the search input
                if (s.toLowerCase().contains(searchText.toLowerCase())) {
                    //adding the element to filtered list

                    searchDataList.add(v);
                }
            }
            }
        } else if(activity.equals("Music")){
            for (int i = 0; i < searchList.size(); i++) {
                if(searchList.get(i) instanceof AudioModel){
                    AudioModel v =(AudioModel) searchList.get(i);

                    String s = v.getName();
                    //if the existing elements contains the search input
                    if (s.toLowerCase().contains(searchText.toLowerCase())) {
                        //adding the element to filtered list

                        searchDataList.add(v);
                    }
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            setAdapter();
        }

        if (searchDataList != null && searchDataList.size() != 0) {
            recycler_view.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        } else {
            recycler_view.setVisibility(View.GONE);
            ll_empty.setVisibility(View.VISIBLE);
        }
    }

    public void getDataList() {
        if(activity.equals("Video")) {
            getSearchList();
        } else if(activity.equals("Music")){
            getSearchList1();
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

//                setAdapter();
                progress_bar.setVisibility(View.GONE);
                if (searchDataList != null && searchDataList.size() != 0) {

                    recycler_view.setVisibility(View.VISIBLE);
                    ll_empty.setVisibility(View.GONE);

                } else {
                    recycler_view.setVisibility(View.GONE);
                    ll_empty.setVisibility(View.VISIBLE);

                }

            }
        });
    }

    public void setAdapter() {
        progress_bar.setVisibility(View.GONE);

//        if (searchDataList != null && searchDataList.size() != 0) {

        recycler_view.setVisibility(View.VISIBLE);
        ll_empty.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchActivity.this);
        recycler_view.setLayoutManager(layoutManager);
        if(activity.equals("Video")) {
            adapter = new SearchAdapter(SearchActivity.this, searchDataList, false);
        } else if(activity.equals("Music")){
            adapter = new SearchAdapter(SearchActivity.this, searchDataList, false);
        }
        recycler_view.setAdapter(adapter);




    }

    @Override
    public void onBackPressed() {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm.isAcceptingText()) {
                //writeToLog("Software Keyboard was shown");
                //  hideSoftKeyboard(binding.edtSearch);

                View view1 = getCurrentFocus();
                if (view1 != null) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(view1.getWindowToken(), 0);
                }

            } else {
                // writeToLog("Software Keyboard was not shown");
            }
            super.onBackPressed();

    }



    private void setClear() {

        searchDataList.clear();

        if (searchList != null)
            for (int f = 0; f < searchList.size(); f++) {

                searchDataList.add(searchList.get(f));
            }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            setAdapter();
        }

        if (searchDataList != null && searchDataList.size() != 0) {
            recycler_view.setVisibility(View.VISIBLE);
            ll_empty.setVisibility(View.GONE);
        } else {
            recycler_view.setVisibility(View.GONE);
            ll_empty.setVisibility(View.VISIBLE);
        }
    }



    protected void hideSoftKeyboard(EditText input) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(edt_search.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        edt_search.requestFocus();
    }







    private void getSearchList() {



        for(int i =0; i< VideoDataService.videoList.size(); i++){
            Video model = VideoDataService.videoList.get(i);
            searchList.add(model);
                            if (edt_search.getText().toString().length() == 0) {
                                searchDataList.add(model);

                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (edt_search.getText().toString().length() == 0)
                                        if (adapter != null) {
                                            adapter.notifyItemInserted(searchDataList.size());
                                            progress_bar.setVisibility(View.GONE);

                                        }
                                }
                            });

        }


    }

    private void getSearchList1() {


        for(int i = 0; i< MusicDataService.audioList.size(); i++){
            AudioModel model = MusicDataService.audioList.get(i);
            searchList.add(model);
            if (edt_search.getText().toString().length() == 0)
                searchDataList.add(model);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (edt_search.getText().toString().length() == 0)
                        if (adapter != null) {
                            adapter.notifyItemInserted(searchDataList.size());
                            progress_bar.setVisibility(View.GONE);

                        }
                }
            });

        }


    }



}