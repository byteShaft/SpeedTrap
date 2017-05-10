package com.byteshaft.speedtrap.fragments;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.AppGlobals;
import com.byteshaft.speedtrap.utils.Helpers;
import com.byteshaft.speedtrap.utils.SoundFX;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener {

    View baseViewSettingsFragment;

    ImageButton ibSettingsLogout;
    EditText etSettingsAlertDistance;
    EditText etSettingsAlertSpeedLimit;
    SeekBar sbSettingsAlertVolume;
    Button btnSettingsUpdate;
    SoundFX soundFX;
    AudioManager audioManager;
    int cbChangedLevel = AppGlobals.getAlertVolume();

    final Runnable logout = new Runnable() {
        public void run() {
            AppGlobals.setLoggedIn(false);
            AppGlobals.setPushNotificationsEnabled(false);
            Helpers.loadFragment(MainActivity.fragmentManager, new WelcomeFragment(), false, "WelcomeFragment");
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewSettingsFragment = inflater.inflate(R.layout.fragment_settings, container, false);
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        soundFX = new SoundFX(getActivity());

        ibSettingsLogout = (ImageButton) baseViewSettingsFragment.findViewById(R.id.ib_settings_logout);
        ibSettingsLogout.setOnClickListener(this);
        etSettingsAlertDistance = (EditText) baseViewSettingsFragment.findViewById(R.id.et_settings_alert_radius);
        etSettingsAlertDistance.setOnClickListener(this);
        etSettingsAlertDistance.setText(String.valueOf(AppGlobals.getAlertDistance()));
        etSettingsAlertSpeedLimit = (EditText) baseViewSettingsFragment.findViewById(R.id.et_settings_alert_speed_limit);
        etSettingsAlertSpeedLimit.setOnClickListener(this);
        etSettingsAlertSpeedLimit.setText(String.valueOf(AppGlobals.getAlertSpeedLimit()));
        sbSettingsAlertVolume = (SeekBar) baseViewSettingsFragment.findViewById(R.id.sb_settings_alert_volume);
        sbSettingsAlertVolume.incrementProgressBy(10);
        sbSettingsAlertVolume.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbSettingsAlertVolume.setProgress(AppGlobals.getAlertVolume());

        sbSettingsAlertVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int type = Integer.parseInt(etSettingsAlertDistance.getText().toString());
                cbChangedLevel = i;
//                if (!soundFX.isAlertInProgress) {
//                soundFX.playSound(getActivity(), SoundFX.soundEffectThree, i, false);
//                }
                if (type == 0) {
                    soundFX.playSound(getActivity(), SoundFX.soundEffectThree, i, true);
                } else if (type == 1) {
                    soundFX.playSound(getActivity(), SoundFX.soundEffectTwo, i, true);
                } else {
                    soundFX.playSound(getActivity(), SoundFX.soundEffectOne, i, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnSettingsUpdate = (Button) baseViewSettingsFragment.findViewById(R.id.btn_settings_update);
        btnSettingsUpdate.setOnClickListener(this);


        return baseViewSettingsFragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_settings_logout:
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(),
                        getString(R.string.textLogout), getString(R.string.textAreYouSure),
                        getString(R.string.buttonYes), getString(R.string.buttonCancel), logout);
                break;
        }
    }
}
