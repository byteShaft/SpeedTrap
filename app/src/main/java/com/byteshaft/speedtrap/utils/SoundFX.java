package com.byteshaft.speedtrap.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.byteshaft.speedtrap.R;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by fi8er1 on 02/05/2017.
 */

public class SoundFX {

    public boolean isAlertInProgress = false;
    private float currentVolume;
    private float maxVolume;
    public int typeOfAlertInProgress;

    public static int soundEffectOne = R.raw.beep_one;
    public static int soundEffectTwo = R.raw.beep_two;
    public static int soundEffectThree = R.raw.beep_three;

    private SoundPool soundPool;

    private int playbackSoundID;
    private float playbackVolume;
    private boolean playbackLoop;

    public SoundFX(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        currentVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);
    }

    public void playSound(Context context, int soundID, int volume, boolean loop) {
        if (volume == -1) {
            playbackVolume = AppGlobals.getAlertVolume() / maxVolume;
        } else {
            playbackVolume = volume / maxVolume;
        }
        playbackLoop = loop;
        if (isAlertInProgress) {
            soundPool.stop(playbackSoundID);
            soundPool.release();
            soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);
            isAlertInProgress = false;
        }
        playbackSoundID = soundPool.load(context, soundID, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                isAlertInProgress = true;
                if (playbackLoop) {
                    soundPool.play(playbackSoundID, playbackVolume, playbackVolume, 1, -1, 1f);
                } else {
                    soundPool.play(playbackSoundID, playbackVolume, playbackVolume, 1, 1, 1f);
                }
            }
        });
    }

    void stopSound() {
        if (isAlertInProgress) {
            soundPool.stop(playbackSoundID);
            soundPool.release();
            isAlertInProgress = false;
        }
    }
}