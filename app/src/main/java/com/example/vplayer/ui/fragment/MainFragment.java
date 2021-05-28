package com.example.vplayer.ui.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.vplayer.R;
import com.google.android.material.tabs.TabLayout;

public class MainFragment extends Fragment {

    private static final int PAGE_COUNT = 3;

    View view;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;
    TabLayout tabLayout;
    
    View videoView, musicView, playlistView;
    TextView videoText, musicText, playlistText;
    ImageView videoImage, musicImage, playlistImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*return super.onCreateView(inflater, container, savedInstanceState);*/
        view = inflater.inflate( R.layout.fragment_main, container, false);
        
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    public void initView() {

        pagerAdapter = new PagerAdapter(getChildFragmentManager(), 0);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_COUNT);
//        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0);

        videoView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null);
        videoImage = videoView.findViewById(R.id.tabIcon);
        videoText = videoView.findViewById(R.id.tabTitle);

        videoImage.setImageResource(R.drawable.ic_grid_folder);
        videoText.setText(R.string.title_video);
        videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
        videoText.setTextColor(getResources().getColor(R.color.tab_selected_color));
        tabLayout.addTab(tabLayout.newTab().setCustomView(videoView));

        musicView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null);
        musicImage = musicView.findViewById(R.id.tabIcon);
        musicText = musicView.findViewById(R.id.tabTitle);
        musicImage.setImageResource(R.drawable.ic_baseline_queue_music_24_bordered);
        musicText.setText(R.string.title_music);
        musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
        musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
        tabLayout.addTab(tabLayout.newTab().setCustomView(musicView));

        playlistView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null);
        playlistImage = playlistView.findViewById(R.id.tabIcon);
        playlistText = playlistView.findViewById(R.id.tabTitle);
        playlistImage.setImageResource(R.drawable.ic_baseline_playlist_play_24_bordered);
        playlistText.setText(R.string.title_playlist);
        playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
        playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
        tabLayout.addTab(tabLayout.newTab().setCustomView(playlistView));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0) {
                    videoImage.setImageResource(R.drawable.ic_grid_folder);
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else if (tab.getPosition() == 1) {
                    musicImage.setImageResource(R.drawable.ic_baseline_queue_music_24);
                    musicImage.setColorFilter(
                            ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else {
                    playlistImage.setImageResource(R.drawable.ic_baseline_playlist_play_24);
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    videoImage.setImageResource(R.drawable.ic_grid_folder_bordered);
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else if (tab.getPosition() == 1) {
                    musicImage.setImageResource(R.drawable.ic_baseline_queue_music_24_bordered);
                    musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                } else {
                    playlistImage.setImageResource(R.drawable.ic_baseline_playlist_play_24_bordered);
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i == 0) {
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                    getActivity().setTitle("Folders");
                   /* musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));*/
                } else if (i == 1) {
                    musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                    getActivity().setTitle("Music");
                   /* videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));*/
                } else {
                  /*  musicImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    musicText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));
                    videoImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_color), PorterDuff.Mode.SRC_IN);
                    videoText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_color));*/
                    getActivity().setTitle("Playlists");
                    playlistImage.setColorFilter(ContextCompat.getColor(getContext(), R.color.tab_selected_color), PorterDuff.Mode.SRC_IN);
                    playlistText.setTextColor(ContextCompat.getColor(getContext(), R.color.tab_selected_color));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private static final int ITEM_VIDEO = 0;
        private static final int ITEM_FOLDER = 1;
        private static final int ITEM_HISTORY = 2;

        public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case ITEM_VIDEO:
                    fragment = new VideoFragment();
                    break;
                case ITEM_FOLDER:
                    fragment = new MusicFragment();
                    break;
                case ITEM_HISTORY:
                    fragment = new PlaylistFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

//        @Nullable
//        @Override
//        public CharSequence getPageTitle(int position) {
//            String title = "";
//            switch (position) {
//                case ITEM_VIDEO:
//                    title = getString(R.string.title_video);
//                    break;
//                case ITEM_FOLDER:
//                    title = getString(R.string.title_folder);
//                    break;
//                case ITEM_HISTORY:
//                    title = getString(R.string.title_history);
//                    break;
//                default:
//                    break;
//            }
//            return title;
//        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}
