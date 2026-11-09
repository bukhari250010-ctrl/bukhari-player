package com.bukhari.distractionfreeplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer player;
    private PlayerView playerView;
    private Button btnSpeed;
    private Uri videoUri;
    private float[] speeds = {1.0f, 1.25f, 1.5f, 2.0f, 0.5f, 0.75f};
    private int speedIndex = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playerView = findViewById(R.id.player_view);
        btnSpeed = findViewById(R.id.btn_speed);
        View touchOverlay = findViewById(R.id.touch_overlay);
        
        prefs = getSharedPreferences("playback_positions", Context.MODE_PRIVATE);

        if (getIntent() != null && getIntent().getData() != null) {
            videoUri = getIntent().getData();
        }

        // একবার স্ক্রিনে ট্যাপ করলেই ইনস্ট্যান্ট প্লে/পজ (হাই সেনসিটিভ)
        touchOverlay.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });

        // স্পিড বাড়ানোর বা কমানোর বাটন
        btnSpeed.setOnClickListener(v -> {
            speedIndex = (speedIndex + 1) % speeds.length;
            float currentSpeed = speeds[speedIndex];
            if (player != null) {
                player.setPlaybackSpeed(currentSpeed);
            }
            btnSpeed.setText(currentSpeed + "x");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        if (videoUri != null) {
            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            player.setMediaItem(mediaItem);

            // Resume Playback (শেষ দেখা জায়গা থেকে শুরু হওয়া)
            long savedPosition = prefs.getLong(videoUri.toString(), 0);
            if (savedPosition > 0) {
                player.seekTo(savedPosition);
            }

            player.prepare();
            player.play();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            // ভিডিও বন্ধ করার সময় বা বের হয়ে যাওয়ার সময় বর্তমান সময় সংরক্ষণ করা
            if (videoUri != null) {
                prefs.edit().putLong(videoUri.toString(), player.getCurrentPosition()).apply();
            }
            player.release();
            player = null;
        }
    }
}
