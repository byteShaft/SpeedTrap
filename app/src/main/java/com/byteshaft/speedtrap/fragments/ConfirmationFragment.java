package com.byteshaft.speedtrap.fragments;

import android.content.Intent;
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

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.AppGlobals;
import com.byteshaft.speedtrap.utils.Helpers;

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
        etCodeConfirmationEmail.setText(AppGlobals.getUsername());

        return baseViewConfirmationFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirmation_code_submit:
                sTextEmailEntry = etCodeConfirmationEmail.getText().toString();
                sConfirmationOtpEmail = etCodeConfirmationEmailOtp.getText().toString();
                if (validateConfirmationCode()) {
//                    taskUserConfirmation = (UserConfirmationTask) new UserConfirmationTask().execute();
                }
                break;
            case R.id.btn_confirmation_code_resend:
                sTextEmailEntry = etCodeConfirmationEmail.getText().toString();
                if (isTimerActive) {
                    Helpers.showSnackBar(baseViewConfirmationFragment,
                            getString(R.string.messageOtpAlreadySentWaitForTheCountdown),
                            Snackbar.LENGTH_SHORT, "#ffffff");
                } else {
//                    taskResendEmail = (UserResendOTP) new UserResendOTP().execute();
                }
                break;
        }
    }

    public boolean validateConfirmationCode() {
        boolean valid = true;
        if (sConfirmationOtpEmail.isEmpty() || sConfirmationOtpEmail.length() < 4) {
            etCodeConfirmationEmailOtp.setError(getString(R.string.errorMinimumFourCharacters));
            valid = false;
        } else {
            etCodeConfirmationEmailOtp.setError(null);
        }
        return valid;
    }

    public void onConfirmationSuccess() {
        Helpers.showSnackBar(getView(), getString(R.string.messageOtpSuccessfullyConfirmed), Snackbar.LENGTH_SHORT, "#A4C639");
//        if (Helpers.isIsSoftKeyboardOpen()) {
//            Helpers.closeSoftKeyboard(getActivity());
//        }
//        if (AppGlobals.get) {
//            AppGlobals.putUserType(0);
//        } else {
//            AppGlobals.putUserType(1);
//        }
//        if (!AppGlobals.isPushNotificationsEnabled()) {
//            new EnablePushNotificationsTask().execute();
//        }
        AppGlobals.setLoggedIn(true);
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainActivity.class));
        otp = null;
    }

    public void onConfirmationFailed(String message) {
        Helpers.showSnackBar(getView(), message, Snackbar.LENGTH_SHORT, "#f44336");
    }

    public void onResendSuccess() {
        Helpers.showSnackBar(getView(), getString(R.string.messageOtpSuccessfullySent), Snackbar.LENGTH_LONG, "#A4C639");
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
        Helpers.showSnackBar(getView(), message, Snackbar.LENGTH_LONG, "#f44336");
        tvCodeConfirmationStatusDisplay.setText(getString(R.string.textOtpResendFailed));
        tvCodeConfirmationStatusDisplay.setTextColor(Color.parseColor("#f44336"));
        tvCodeConfirmationStatusDisplay.clearAnimation();
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

}
