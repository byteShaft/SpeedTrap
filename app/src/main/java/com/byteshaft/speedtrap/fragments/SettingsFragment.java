package com.byteshaft.speedtrap.fragments;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.byteshaft.requests.HttpRequest;
import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.AppGlobals;
import com.byteshaft.speedtrap.utils.EndPoints;
import com.byteshaft.speedtrap.utils.Helpers;
import com.byteshaft.speedtrap.utils.LocationService;
import com.byteshaft.speedtrap.utils.SoundFX;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;


/**
 * Created by fi8er1 on 23/04/2017.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener {

    View baseViewSettingsFragment;

    TextView tvSettingsAlertHeading;
    ImageButton ibSettingsSync;
    ImageButton ibSettingsLogout;
    EditText etSettingsAlertDistance;
    EditText etSettingsAlertSpeedLimit;
    SeekBar sbSettingsAlertVolume;
    Button btnSettingsUpdate;
    SoundFX soundFX;
    AudioManager audioManager;
    Switch sSettingsAlert;
    LocationService mLocationService;

    final Runnable logout = new Runnable() {
        public void run() {
            if (mLocationService.isBackgroundServiceRunning) {
                mLocationService.stopLocationService();
            }
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

        mLocationService = new LocationService();

        tvSettingsAlertHeading = (TextView) baseViewSettingsFragment.findViewById(R.id.tv_settings_alert_title);

        ibSettingsSync = (ImageButton) baseViewSettingsFragment.findViewById(R.id.ib_settings_sync);
        ibSettingsSync.setOnClickListener(this);
        ibSettingsLogout = (ImageButton) baseViewSettingsFragment.findViewById(R.id.ib_settings_logout);
        ibSettingsLogout.setOnClickListener(this);
        etSettingsAlertDistance = (EditText) baseViewSettingsFragment.findViewById(R.id.et_settings_alert_distance);
        etSettingsAlertDistance.setOnClickListener(this);
        etSettingsAlertDistance.setText(String.valueOf(AppGlobals.getAlertDistance()));
        etSettingsAlertDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                etSettingsAlertDistance.setError(null);
            }
        });

        etSettingsAlertSpeedLimit = (EditText) baseViewSettingsFragment.findViewById(R.id.et_settings_alert_speed_limit);
        etSettingsAlertSpeedLimit.setOnClickListener(this);
        etSettingsAlertSpeedLimit.setText(String.valueOf(AppGlobals.getAlertSpeedLimit()));
        etSettingsAlertSpeedLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                etSettingsAlertSpeedLimit.setError(null);
            }
        });

        sSettingsAlert = (Switch) baseViewSettingsFragment.findViewById(R.id.s_settings_alert);
        sSettingsAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sSettingsAlert.setText(getString(R.string.textEnabled));
                    sbSettingsAlertVolume.setEnabled(true);
                } else {
                    sSettingsAlert.setText(getString(R.string.textDisabled));
                    sbSettingsAlertVolume.setEnabled(false);
                }
            }
        });
        sbSettingsAlertVolume = (SeekBar) baseViewSettingsFragment.findViewById(R.id.sb_settings_alert_volume);
        sbSettingsAlertVolume.incrementProgressBy(10);
        sbSettingsAlertVolume.setMax(audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        sbSettingsAlertVolume.setProgress(AppGlobals.getAlertVolume());

        sbSettingsAlertVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!soundFX.isAlertInProgress) {
                    soundFX.typeOfAlertInProgress = 0;
                soundFX.playSound(getActivity(), SoundFX.soundEffectThree, i, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnSettingsUpdate = (Button) baseViewSettingsFragment.findViewById(R.id.btn_settings_update);
        btnSettingsUpdate.setOnClickListener(this);

        setupUI();

        return baseViewSettingsFragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_settings_sync:
                WelcomeFragment.isTrapRetrievalRequestFromLogin = false;
                MainActivity.sendTrapRetrievalRequest();
                break;
            case R.id.ib_settings_logout:
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(getActivity(),
                        getString(R.string.textLogout), getString(R.string.textAreYouSure),
                        getString(R.string.buttonYes), getString(R.string.buttonCancel), logout);
                break;
            case R.id.btn_settings_update:
                if (isUpdateDataInputValid()) {
                    sendUpdateRequest();
                }
                break;
        }
    }

    private void setupUI() {
        sSettingsAlert.setChecked(AppGlobals.isSoundAlertEnabled());
        sbSettingsAlertVolume.setProgress(AppGlobals.getAlertVolume());
        etSettingsAlertDistance.setText(String.valueOf(AppGlobals.getAlertDistance()));
        etSettingsAlertSpeedLimit.setText(String.valueOf(AppGlobals.getAlertSpeedLimit()));

        if (AppGlobals.getUserType() == 1) {
            tvSettingsAlertHeading.setText(R.string.textTrapAlert);
        } else {
            tvSettingsAlertHeading.setText(R.string.textMobileTrapAlert);
        }
    }

    private void sendUpdateRequest() {
        HttpRequest request = new HttpRequest(getActivity());
        Helpers.showProgressDialog(getActivity(), getString(R.string.messageSendingUpdateRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                onUpdateSuccess(getString(R.string.messageUpdateSuccessful));
                                break;
                            default:
                                onUpdateFailed(getString(R.string.messageUpdateRequestFailed) + "\n" +
                                        request.getResponseText());
                                Log.i("responseText", "" + request.getResponseText());
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onUpdateFailed(getString(R.string.messageUpdateRequestFailed) + "\n" +
                        request.getResponseText());
            }
        });
        request.open("PUT", EndPoints.PROFILE);
        request.setRequestHeader("Authorization", "Token " +  AppGlobals.getToken());
        request.send(getUpdateString(getJSONPreferencesString(sbSettingsAlertVolume.getProgress(), etSettingsAlertSpeedLimit.getText().toString(),
                sSettingsAlert.isChecked(), etSettingsAlertDistance.getText().toString())));
    }

    public static String getUpdateString(JSONObject jsonObjectPreferences) {
        JSONObject json = new JSONObject();
        try {
            if (AppGlobals.getUserType() == 1) {
                json.put("user_preferences", jsonObjectPreferences);
            } else {
                json.put("staff_preferences", jsonObjectPreferences);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public static JSONObject getJSONPreferencesString(int alertVolume, String speedLimit,
                                                      boolean isSoundAlertEnabled, String warningDistance) {
        JSONObject json = new JSONObject();
        try {
            json.put("alert_volume", alertVolume);
            if (AppGlobals.getUserType() == 2) {
                json.put("mobile_speed_trap_notifier", String.valueOf(isSoundAlertEnabled));
            } else {
                json.put("sound_alert", String.valueOf(isSoundAlertEnabled));
                json.put("warning_speed_limit", speedLimit);
            }
            json.put("warning_distance", warningDistance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void onUpdateSuccess(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.GREEN);
    }

    private void onUpdateFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.RED);
    }

    public boolean isUpdateDataInputValid() {
        boolean valid = true;

        if (Integer.parseInt(etSettingsAlertSpeedLimit.getText().toString()) < 20) {
            etSettingsAlertSpeedLimit.setError(getString(R.string.messageAlertSpeedLimitCannotBeSetLessThan20));
            valid = false;
        }

        if (Integer.parseInt(etSettingsAlertDistance.getText().toString()) < 250) {
            etSettingsAlertDistance.setError(getString(R.string.messageAlertDistanceCannotBeSetLessThan250));
            valid = false;
        } else if (Integer.parseInt(etSettingsAlertDistance.getText().toString()) > 2000) {
            etSettingsAlertDistance.setError(getString(R.string.messageAlertDistanceCannotBeSetMoreThan2000));
            valid = false;
        }

        if (!valid) {
            Helpers.showSnackBar(getString(R.string.messageUpdateInputIsInvalid), Snackbar.LENGTH_SHORT, Color.RED);
        }
        return valid;
    }
}
