package com.zbar.code.camera.beep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.zbar.code.R;

import java.io.IOException;

/**
 * Description Beep Beep!!
 * Version 1.0
 * Created by Czf on 2019/11/20 11:20
 */
public class BeepSound implements LifecycleObserver {
    private static final String TAG = BeepSound.class.getSimpleName();

    private static BeepSound BEEP_SOUND;

    private Activity mActivity;

    private MediaPlayer mediaPlayer;
    private static final float BEEP_VOLUME = 0.50f;
    private static final long VIBRATE_DURATION = 200L;

    private boolean playBeep = true;
    private boolean vibrate = true;

    public static void init(AppCompatActivity mActivity) {
        if (BEEP_SOUND == null) {
            BEEP_SOUND = new BeepSound(mActivity);
        }
    }

    public static BeepSound get() {
        if (BEEP_SOUND == null) throw new NullPointerException("mBeepSound == null");
        return BEEP_SOUND;
    }

    public void gc() {
        BeepSound.BEEP_SOUND = null;
    }

    private BeepSound(Activity mActivity) {
        this.mActivity = mActivity;
    }

    public BeepSound play(boolean playBeep) {
        this.playBeep = playBeep;
        return this;
    }

    public BeepSound vibrate(boolean vibrate) {
        this.vibrate = vibrate;
        return this;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        initBeepSound();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mActivity = null;
    }


    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            mActivity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.seekTo(0);
                }
            });

            AssetFileDescriptor file = mActivity.getResources().openRawResourceFd(R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }


    public void play() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VIBRATE_DURATION);
            }
        }
    }
}
