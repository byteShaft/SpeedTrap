package com.byteshaft.speedtrap.utils;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;

import com.byteshaft.speedtrap.R;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by fi8er1 on 02/05/2017.
 */

public class SoundFX {

    private SoundPool soundPool;
    private boolean isSoundPlaying = false;
    private boolean isSoundLoaded = false;
    private float currentVolume;
    private float maxVolume;
    private float volume;
    private AudioManager audioManager;
    private int counter;


    public int soundEffectOne;

    public void initialize(Activity activity) {
        // AudioManager audio settings for adjusting the volume
        audioManager = (AudioManager) activity.getSystemService(AUDIO_SERVICE);
        currentVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = currentVolume / maxVolume;
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        counter = 0;
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                isSoundLoaded = true;
            }
        });
        soundEffectOne = soundPool.load(activity, R.raw.digital_beeps, 1);
    }

    public void playSound (int soundID) {
        if (isSoundLoaded && !isSoundPlaying) {
            soundPool.play(soundID, volume, volume, 1, 0, 1f);
            counter = counter++;
            isSoundPlaying = true;
        }
    }

    public void playSoundInLoop (int soundID) {
        if (isSoundLoaded && !isSoundPlaying) {
            soundPool.play(soundID, volume, volume, 1, -1, 1f);
            counter = counter++;
            isSoundPlaying = true;
        }
    }

    public void stopSound(int soundID) {
        if (isSoundPlaying) {
            soundPool.stop(soundID);
            isSoundPlaying = false;
        }
    }
}
