package com.example.quantumrealm;

import android.app.Application;
import android.media.MediaPlayer;

public class Music extends Application {

    private MediaPlayer mp;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize MediaPlayer
        mp = MediaPlayer.create(this, R.raw.music);
        mp.setLooping(true);
        mp.start();
    }

    public MediaPlayer getMediaPlayer() {
        return mp;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // Release MediaPlayer when the application is terminated
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}