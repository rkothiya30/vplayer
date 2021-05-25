package com.example.vplayer.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.vplayer.R;


public class SettingsActivity extends AppCompatActivity {

    ImageView back_setting;
    RelativeLayout lout_privacy_policy, lout_rate_us, lout_share_us, lout_about_us;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.black, this.getTheme()));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black));
            getWindow().setStatusBarColor(getResources().getColor(R.color.black));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkGray)));

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        back_setting = findViewById(R.id.back_setting);
        lout_privacy_policy = findViewById(R.id.lout_privacy_policy);
        lout_rate_us = findViewById(R.id.lout_rate_us);
        lout_share_us = findViewById(R.id.lout_share_us);
        lout_about_us = findViewById(R.id.lout_about_us);

        back_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        lout_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
            }
        });

        lout_share_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=" + getPackageName();
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        /*lout_share_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                String shareUrl = "https://play.google.com/store/apps/details?id=" + getPackageName() + "";
                share.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                share.putExtra(Intent.EXTRA_TEXT, "Click on below link to download " + getResources().getString(R.string.app_name) + "\n" + shareUrl);

                startActivity(Intent.createChooser(share, "Share Using!"));
            }
        });*/

        lout_about_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*startActivity(new Intent(SettingsActivity.this, AboutUsActivity.class));*/


                Uri uri = Uri.fromParts(
                        "mailto",getResources().getString(R.string.feedback_email), null);

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Query:");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello");
                startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
            }
        });
    }


}