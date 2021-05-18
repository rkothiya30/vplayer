package com.example.vplayer.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.os.Bundle;
import android.text.Selection;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vplayer.R;
import com.example.vplayer.fragment.adapter.FragmentAdapter;
import com.example.vplayer.fragment.adapter.MusicAdapter;
import com.example.vplayer.fragment.adapter.VideoSAdapter;
import com.example.vplayer.ui.fragment.MusicFragment;
import com.example.vplayer.ui.fragment.MusicSFragment;
import com.example.vplayer.ui.fragment.VideoSFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static com.example.vplayer.ui.fragment.MusicFragment.audioList;

public class SelectItemActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    ArrayList<Fragment> fragments;
    ImageView iv_back;
    public static TextView text_title;
    public static int SelectCount = 0;
    Toolbar toolbar;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_item);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        getSupportActionBar().hide();

        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tabLayout);
        iv_back = findViewById(R.id.iv_back);
        text_title = findViewById(R.id.text_title);
        fragments =new ArrayList<>();

        fragments.add(new MusicSFragment());
        fragments.add(new VideoSFragment());



        FragmentAdapter pagerAdapter = new FragmentAdapter(getSupportFragmentManager(), getApplicationContext(), fragments);
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("MUSIC");
        tabLayout.getTabAt(1).setText("VIDEO");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        MusicAdapter.Selection = false;
        for(int i = 0; i<audioList.size(); i++)
        {
            audioList.get(i).setSelected(false);
        }
        for(int j=0; j< VideoSAdapter.videoList.size(); j++){
            VideoSAdapter.videoList.get(j).setSelected(false);
        }

        MusicFragment.adapter.notifyDataSetChanged();
        super.onBackPressed();
    }
}