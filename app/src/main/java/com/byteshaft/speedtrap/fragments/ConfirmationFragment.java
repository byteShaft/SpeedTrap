package com.byteshaft.speedtrap.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byteshaft.requests.HttpRequest;
import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.AppGlobals;
import com.byteshaft.speedtrap.utils.EndPoints;
import com.byteshaft.speedtrap.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by fi8er1 on 24/04/2017.
 */

public class ConfirmationFragment extends Fragment implements View.OnClickListener {

    View baseViewConfirmationFragment;

    public static boolean isFragmentOpenedFromLogin;
    static boolean isCodeConfirmationFragmentOpen;
    EditText etCodeConfirmationEmail;
    EditText etCodeConfirmationEmailOtp;
    Button btnCodeConfirmationSubmitCode;
    Button btnCodeConfirmationResendCode;
    TextView tvCodeConfirmationStatusDisplay;
    TextView tvCodeConfirmationStatusDisplayTimer;
    final Runnable functionSetTimerTextOnTick = new Runnable() {
        public void run() {
            tvCodeConfirmationStatusDisplayTimer.setText(Helpers.secondsToMinutesSeconds(
                    Helpers.countDownTimerMillisUntilFinished / 1000));
        }
    };
    Animation animTimerFading;
    String sConfirmationOtpEmail;
    String sTextEmailEntry;
    HttpURLConnection connection;
    boolean isTimerActive;

    final Runnable functionOnTimerFinish = new Runnable() {
        public void run() {
            animTimerFading.cancel();
            isTimerActive = false;
            tvCodeConfirmationStatusDisplayTimer.setVisibility(View.GONE);
        }
    };

    public static String otp;
    boolean isUserConfirmationTaskRunning;
    boolean isResendEmailTaskRunning;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewConfirmationFragment = inflater.inflate(R.layout.fragment_confirmation, container, false);

        etCodeConfirmationEmail = (EditText) baseViewConfirmationFragment.findViewById(R.id.et_confirmation_code_email);
        etCodeConfirmationEmailOtp = (EditText) baseViewConfirmationFragment.findViewById(R.id.et_confirmation_code_email_otp);
        btnCodeConfirmationSubmitCode = (Button) baseViewConfirmationFragment.findViewById(R.id.btn_confirmation_code_submit);
        btnCodeConfirmationResendCode = (Button) baseViewConfirmationFragment.findViewById(R.id.btn_confirmation_code_resend);
        tvCodeConfirmationStatusDisplay = (TextView) baseViewConfirmationFragment.findViewById(R.id.tv_confirmation_code_status_display);
        tvCodeConfirmationStatusDisplayTimer = (TextView) baseViewConfirmationFragment.findViewById(R.id.tv_confirmation_code_resend_timer);
        animTimerFading = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_text_complete_fading);

        btnCodeConfirmationResendCode.setOnClickListener(this);
        btnCodeConfirmationSubmitCode.setOnClickListener(this);

        if (isFragmentOpenedFromLogin) {
            etCodeConfirmationEmail.setText(WelcomeFragment.sLoginEmail);
            sTextEmailEntry = WelcomeFragment.sLoginEmail;
            tvCodeConfirmationStatusDisplay.setText(getString(R.string.textActivateAccount));
            tvCodeConfirmationStatusDisplay.setTextColor(Color.parseColor("#ffffff"));
            return baseViewConfirmationFragment;
        }

        tvCodeConfirmationStatusDisplay.setText(getString(R.string.textOtpSentCheckInbox));
        tvCodeConfirmationStatusDisplay.setTextColor(Color.parseColor("#A4C639"));

        isTimerActive = true;
        Helpers.setCountDownTimer(120000, 1000, functionSetTimerTextOnTick, functionOnTimerFinish);
        tvCodeConfirmationStatusDisplayTimer.startAnimation(animTimerFading);
        tvCodeConfirmationStatusDisplayTimer.setVisibility(View.VISIBLE);
        etCodeConfirmationEmail.setText(RegisterFragment.sRegisterEmail);

        return baseViewConfirmationFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirmation_code_submit:
                sTextEmailEntry = etCodeConfirmationEmail.getText().toString();
                sConfirmationOtpEmail = etCodeConfirmationEmailOtp.getText().toString();
                if (isConfirmationCodeDataInputValid()) {
                    sendConfirmationRequest();
                }
                break;
            case R.id.btn_confirmation_code_resend:
                sTextEmailEntry = etCodeConfirmationEmail.getText().toString();
                if (isTimerActive) {
                    Helpers.showSnackBar(getString(R.string.messageOtpAlreadySentWaitForTheCountdown),
                            Snackbar.LENGTH_SHORT, Color.GREEN);
                } else {
                    sendOtpResendRequest();
                }
                break;
        }
    }

    public boolean isConfirmationCodeDataInputValid() {
        boolean valid = true;
        if (sConfirmationOtpEmail.isEmpty() || sConfirmationOtpEmail.length() < 4) {
            etCodeConfirmationEmailOtp.setError(getString(R.string.errorMinimumFourCharacters));
            valid = false;
        } else {
            etCodeConfirmationEmailOtp.setError(null);
        }
        return valid;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
        isCodeConfirmationFragmentOpen = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isCodeConfirmationFragmentOpen = true;
    }

    private void sendConfirmationRequest() {
        HttpRequest request = new HttpRequest(getActivity());
        Helpers.showProgressDialog(getActivity(), getString(R.string.messageSendingConfirmationRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                onConfirmationSuccess(getString(R.string.messageConfirmationRequestConfirmed), request.getResponseText());
                                break;
                            default:
                                onConfirmationFailed(getString(R.string.messageConfirmationRequestFailed) + "\n" + request.getResponseText());
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onConfirmationFailed(getString(R.string.messageConfirmationRequestFailed));
            }
        });
        request.open("POST", EndPoints.ACTIVATE);
        request.send(getConfirmationString(sTextEmailEntry, sConfirmationOtpEmail));
    }

    public static String getConfirmationString(String email, String otp) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("email_otp", otp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void onConfirmationSuccess(String message, String responseText) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.GREEN);
        try {
            JSONObject jsonObject = new JSONObject(responseText);
            JSONObject jsonObject1 = new JSONObject(jsonObject.toString());

            AppGlobals.setLoggedIn(true);
            AppGlobals.putToken(jsonObject.getString("token"));
            AppGlobals.putAlertDistance(jsonObject1.getInt("warning_radius"));
            AppGlobals.putAlertSpeedLimit(jsonObject1.getInt("warning_speed_limit"));
            AppGlobals.setSoundAlertEnabled(jsonObject1.getBoolean("sound_alert"));
            AppGlobals.putAlertVolume(jsonObject1.getInt("alert_volume"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Helpers.loadFragment(MainActivity.fragmentManager, new MapFragment(), false, "MapFragment");
    }

    private void onConfirmationFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.RED);
    }

    private void sendOtpResendRequest() {
        HttpRequest request = new HttpRequest(getActivity());
        Helpers.showProgressDialog(getActivity(), getString(R.string.messageSendingOtpRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                onResendSuccess(getString(R.string.messageOtpSuccessfullySent));
                                break;
                            default:
                                onResendFailed(getString(R.string.messageOtpRequestFailed));
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onConfirmationFailed(getString(R.string.messageConfirmationRequestFailed) + "\n"
                        + request.getResponseText());
            }
        });
        request.open("POST", EndPoints.REQUEST_ACTIVATION_KEY);
        request.send(getResendString(sTextEmailEntry));
    }

    public static String getResendString(String email) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public void onResendSuccess(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.GREEN);
        tvCodeConfirmationStatusDisplay.setText(getString(R.string.textOtpSentCheckInbox));
        tvCodeConfirmationStatusDisplay.setTextColor(Color.parseColor("#A4C639"));
        tvCodeConfirmationStatusDisplay.clearAnimation();

        isTimerActive = true;
        Helpers.setCountDownTimer(120000, 1000, functionSetTimerTextOnTick, functionOnTimerFinish);
        tvCodeConfirmationStatusDisplayTimer.startAnimation(animTimerFading);
        tvCodeConfirmationStatusDisplayTimer.setVisibility(View.VISIBLE);
        tvCodeConfirmationStatusDisplay.setVisibility(View.VISIBLE);
        otp = null;
    }

    public void onResendFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.RED);
        tvCodeConfirmationStatusDisplay.setText(getString(R.string.textOtpResendFailed));
        tvCodeConfirmationStatusDisplay.setTextColor(Color.parseColor("#f44336"));
        tvCodeConfirmationStatusDisplay.clearAnimation();
    }
}