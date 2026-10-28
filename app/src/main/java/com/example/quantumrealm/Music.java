package com.example.quantumrealm;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;

public class Music extends Application {

    private static final String TAG = "Music";
    private static final String PREFS = "music_prefs";
    private static final String KEY_MUTED = "muted";

    private MediaPlayer mp;
    private AudioAttributes audioAttributes;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private boolean hadAudioFocus;
    private int startedActivities;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        initPlayer();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                startedActivities++;
                ensurePlaying();
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                startedActivities--;
                if (startedActivities <= 0) {
                    startedActivities = 0;
                    pausePlayback(false);
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                ensurePlaying();
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, android.os.Bundle savedInstanceState) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull android.os.Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    private void initPlayer() {
        releasePlayer();
        audioFocusRequest = null;
        mp = MediaPlayer.create(this, R.raw.music);
        if (mp == null) {
            Log.e(TAG, "Failed to create MediaPlayer for R.raw.music");
            return;
        }

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mp.setAudioAttributes(audioAttributes);
        mp.setLooping(true);
        mp.setVolume(1f, 1f);
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        mp.setOnCompletionListener(player -> {
            if (!isMuted(this)) {
                player.seekTo(0);
                player.start();
            }
        });

        mp.setOnErrorListener((player, what, extra) -> {
            Log.w(TAG, "MediaPlayer error what=" + what + " extra=" + extra);
            initPlayer();
            ensurePlaying();
            return true;
        });
    }

    private void ensurePlaying() {
        if (isMuted(this) || mp == null) {
            return;
        }
        if (!requestAudioFocus()) {
            return;
        }
        try {
            if (!mp.isPlaying()) {
                mp.start();
            }
        } catch (IllegalStateException e) {
            Log.w(TAG, "Restarting player after IllegalStateException", e);
            initPlayer();
            if (mp != null) {
                mp.start();
            }
        }
    }

    private void pausePlayback(boolean abandonFocus) {
        if (mp != null && mp.isPlaying()) {
            mp.pause();
        }
        if (abandonFocus) {
            abandonAudioFocus();
        }
    }

    private boolean requestAudioFocus() {
        if (audioManager == null) {
            return true;
        }
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioAttributes != null) {
            if (audioFocusRequest == null) {
                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this::onAudioFocusChange)
                        .build();
            }
            result = audioManager.requestAudioFocus(audioFocusRequest);
        } else {
            result = audioManager.requestAudioFocus(
                    this::onAudioFocusChange,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
            );
        }
        hadAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        return hadAudioFocus || result == AudioManager.AUDIOFOCUS_REQUEST_DELAYED;
    }

    private void abandonAudioFocus() {
        if (audioManager == null || !hadAudioFocus) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
        } else {
            audioManager.abandonAudioFocus(this::onAudioFocusChange);
        }
        hadAudioFocus = false;
    }

    private void onAudioFocusChange(int focusChange) {
        if (isMuted(this) || mp == null) {
            return;
        }
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mp.setVolume(1f, 1f);
                if (!mp.isPlaying()) {
                    mp.start();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pausePlayback(false);
                abandonAudioFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mp.isPlaying()) {
                    mp.pause();
                }
                break;
            default:
                break;
        }
    }

    public static boolean isMuted(Context context) {
        return prefs(context).getBoolean(KEY_MUTED, false);
    }

    public static void setMuted(Context context, boolean muted) {
        prefs(context).edit().putBoolean(KEY_MUTED, muted).apply();
        Music app = app(context);
        if (app == null) {
            return;
        }
        if (muted) {
            app.pausePlayback(true);
        } else {
            app.ensurePlaying();
        }
    }

    public static void toggleMuted(Context context) {
        setMuted(context, !isMuted(context));
    }

    public static void bindMuteMenuItem(Context context, MenuItem item) {
        if (item == null) {
            return;
        }
        boolean muted = isMuted(context);
        item.setIcon(muted ? R.drawable.music_off : R.drawable.music_on);
        item.setTitle(muted ? R.string.music_unmute : R.string.music_mute);
    }

    public static boolean handleMuteMenuItem(Context context, MenuItem item) {
        if (item.getItemId() != R.id.action_mute_music) {
            return false;
        }
        toggleMuted(context);
        bindMuteMenuItem(context, item);
        return true;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private static Music app(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof Music) {
            return (Music) appContext;
        }
        return null;
    }

    private void releasePlayer() {
        if (mp != null) {
            try {
                mp.release();
            } catch (Exception ignored) {
            }
            mp = null;
        }
    }

    @Override
    public void onTerminate() {
        abandonAudioFocus();
        releasePlayer();
        super.onTerminate();
    }
}
