package com.byteshaft.speedtrap.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byteshaft.requests.HttpRequest;
import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.EndPoints;
import com.byteshaft.speedtrap.utils.Helpers;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class RecoveryFragment extends Fragment implements View.OnClickListener {

    View baseViewRecoveryFragment;

    EditText etForgotPasswordEmail;
    EditText etForgotPasswordConfirmationCode;
    EditText etForgotPasswordNewPassword;
    EditText etForgotPasswordConfirmPassword;
    LinearLayout llForgotPasswordNewPassword;
    Button btnForgotPasswordRecover;
    Button btnForgotPasswordNewSubmit;
    TextView tvForgotPasswordDisplayStatus;
    TextView tvForgotPasswordTitle;
    Animation animTextViewFading;
    String sRecoveryEmail;
    String sRecoveryConfirmationCode;
    String sRecoveryNewPassword;
    String sRecoveryConfirmPassword;
    boolean isRecoveryFragmentOpen;
    public static boolean isRecoveryOpenedFromLoginRequest;

    final Runnable sendOtpRequest = new Runnable() {
        public void run() {
            btnForgotPasswordRecover.callOnClick();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewRecoveryFragment = inflater.inflate(R.layout.fragment_recovery, container, false);

        llForgotPasswordNewPassword = (LinearLayout) baseViewRecoveryFragment.findViewById(R.id.ll_forgot_password_new_password_layout);

        etForgotPasswordEmail = (EditText) baseViewRecoveryFragment.findViewById(R.id.et_forgot_password_email);
        etForgotPasswordConfirmationCode = (EditText) baseViewRecoveryFragment.findViewById(R.id.et_forgot_password_confirmation_code);
        etForgotPasswordNewPassword = (EditText) baseViewRecoveryFragment.findViewById(R.id.et_forgot_password_new_password);
        etForgotPasswordConfirmPassword = (EditText) baseViewRecoveryFragment.findViewById(R.id.et_forgot_password_new_password_confirm);
        btnForgotPasswordRecover = (Button) baseViewRecoveryFragment.findViewById(R.id.btn_forgot_password_recover);
        btnForgotPasswordNewSubmit = (Button) baseViewRecoveryFragment.findViewById(R.id.btn_forgot_password_new_submit);
        tvForgotPasswordDisplayStatus = (TextView) baseViewRecoveryFragment.findViewById(R.id.tv_forgot_password_status_display);
        tvForgotPasswordTitle = (TextView) baseViewRecoveryFragment.findViewById(R.id.tv_forgot_password_title);

        animTextViewFading = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_text_complete_fading);
        btnForgotPasswordRecover.setOnClickListener(this);
        btnForgotPasswordNewSubmit.setOnClickListener(this);
        if (isRecoveryOpenedFromLoginRequest) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRecoveryFragmentOpen) {
                        Helpers.AlertDialogMessageWithPositiveFunction(getActivity(), null, getString(R.string.messagePasswordRecoveryStaffAccountSetupInitiated),
                                getString(R.string.buttonOk), sendOtpRequest);
                    }
                }
            }, 1500);
            tvForgotPasswordTitle.setText(getString(R.string.textSetupStaffAccount));
            etForgotPasswordEmail.setText(WelcomeFragment.sLoginEmail);
            etForgotPasswordEmail.setEnabled(false);
            btnForgotPasswordRecover.setText(getString(R.string.buttonSetup));
        }
        return baseViewRecoveryFragment;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_forgot_password_recover:
                sRecoveryEmail = etForgotPasswordEmail.getText().toString();
                if (isRecoveryDataInputValid()) {
                    sendRecoveryRequest();
                }
                break;
            case R.id.btn_forgot_password_new_submit:
                sRecoveryConfirmationCode = etForgotPasswordConfirmationCode.getText().toString();
                sRecoveryNewPassword = etForgotPasswordNewPassword.getText().toString();
                sRecoveryConfirmPassword = etForgotPasswordConfirmPassword.getText().toString();
                if (isPasswordChangeDataInputValid()) {
                    sendPasswordChangeRequest();
                }
                break;
        }
    }

    public boolean isRecoveryDataInputValid() {
        boolean valid = true;
        if (sRecoveryEmail.trim().isEmpty()) {
            etForgotPasswordEmail.setError("Empty");
            valid = false;
        } else if (!sRecoveryEmail.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(sRecoveryEmail).matches()) {
            etForgotPasswordEmail.setError("Invalid E-Mail");
            valid = false;
        } else {
            etForgotPasswordEmail.setError(null);
        }
        return valid;
    }

    public boolean isPasswordChangeDataInputValid() {
        boolean valid = true;
        if (sRecoveryConfirmationCode.trim().isEmpty() || sRecoveryConfirmationCode.trim().length() < 4) {
            etForgotPasswordConfirmationCode.setError("At least 4 characters");
            valid = false;
        } else {
            etForgotPasswordConfirmationCode.setError(null);
        }

        if (sRecoveryNewPassword.length() < 6) {
            etForgotPasswordNewPassword.setError("At least 6 characters");
            valid = false;
        } else {
            etForgotPasswordNewPassword.setError(null);
        }

        if (!sRecoveryNewPassword.equals(sRecoveryConfirmPassword)) {
            etForgotPasswordConfirmPassword.setError("Password does not match");
            valid = false;
        } else {
            etForgotPasswordConfirmPassword.setError(null);
        }
        return valid;
    }

    private void sendRecoveryRequest() {
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
                                if (isRecoveryOpenedFromLoginRequest) {
                                    onRecoverySuccess(getString(R.string.messageSetupRequestFailed) + "\n" +
                                            request.getResponseText());
                                } else {
                                    onRecoverySuccess(getString(R.string.messageRecoveryRequestConfirmed) + "\n" +
                                            request.getResponseText());
                                }
                                break;
                            default:
                                Log.i("code", "" + request.getStatus());
                                onRecoveryFailed(getString(R.string.messageRecoveryRequestFailed) + "\n" +
                                request.getResponseText());
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onRecoveryFailed(getString(R.string.messageRecoveryRequestFailed));
            }
        });
        request.open("POST", EndPoints.FORGOT_PASSWORD);
        request.send(getRecoveryString(sRecoveryEmail));
    }

    public static String getRecoveryString(String email) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void onRecoverySuccess(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.GREEN);
        btnForgotPasswordRecover.setVisibility(View.GONE);
        llForgotPasswordNewPassword.setVisibility(View.VISIBLE);
        etForgotPasswordEmail.setEnabled(false);
        tvForgotPasswordDisplayStatus.setText(getString(R.string.messageOtpRequestConfirmed));
        tvForgotPasswordDisplayStatus.setTextColor(Color.parseColor("#A4C639"));
        tvForgotPasswordDisplayStatus.clearAnimation();
    }

    private void onRecoveryFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.RED);
        if (isRecoveryOpenedFromLoginRequest) {
            tvForgotPasswordDisplayStatus.setText("Setup Failed");
        } else {
            tvForgotPasswordDisplayStatus.setText("Recovery Failed");
        }
        tvForgotPasswordDisplayStatus.setTextColor(Color.parseColor("#f44336"));
        tvForgotPasswordDisplayStatus.clearAnimation();
    }

    private void sendPasswordChangeRequest() {
        HttpRequest request = new HttpRequest(getActivity());
        if (isRecoveryOpenedFromLoginRequest) {
            Helpers.showProgressDialog(getActivity(), getString(R.string.messageSendingOtpForStaffSetup));
        } else {
            Helpers.showProgressDialog(getActivity(), getString(R.string.messageSendingPasswordChangeRequest));
        }
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                if (!isRecoveryOpenedFromLoginRequest) {
                                    onPasswordChangeSuccess(getString(R.string.messagePasswordChangeRequestConfirmed));
                                }
                                break;
                            default:
                                if (isRecoveryOpenedFromLoginRequest) {
                                    onPasswordChangeFailed(getString(R.string.messageSetupRequestFailed) + "\n" +
                                            request.getResponseText());
                                } else {
                                    onPasswordChangeFailed(getString(R.string.messagePasswordChangeRequestFailed) + "\n" +
                                            request.getResponseText());
                                }
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                if (isRecoveryOpenedFromLoginRequest) {
                    onPasswordChangeFailed(getString(R.string.messageSetupRequestFailed) + "\n" +
                            request.getResponseText());
                } else {
                    onPasswordChangeFailed(getString(R.string.messagePasswordChangeRequestFailed) + "\n" +
                            request.getResponseText());
                }
            }
        });
        request.open("POST", EndPoints.CHANGE_PASSWORD);
        request.send(getPasswordChangeString(sRecoveryEmail, sRecoveryNewPassword, sRecoveryConfirmationCode));
    }

    public static String getPasswordChangeString(String email, String password, String otp) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("new_password", password);
            json.put("email_otp", otp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void onPasswordChangeSuccess(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.GREEN);
        llForgotPasswordNewPassword.setVisibility(View.VISIBLE);
        etForgotPasswordEmail.setEnabled(false);
        tvForgotPasswordDisplayStatus.setText("Password Change Successful");
        tvForgotPasswordDisplayStatus.setTextColor(Color.parseColor("#A4C639"));
        tvForgotPasswordDisplayStatus.clearAnimation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.isMainActivityForeground) {
                    if (isRecoveryOpenedFromLoginRequest) {
                        WelcomeFragment.sLoginEmail = sRecoveryEmail;
                        WelcomeFragment.sLoginPassword = sRecoveryNewPassword;
                        WelcomeFragment.sendLoginRequest();
                    } else {
                        getActivity().onBackPressed();
                    }
                }
            }
        }, 1500);
    }

    private void onPasswordChangeFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.RED);
        tvForgotPasswordDisplayStatus.setText(getString(R.string.messageSetupRequestFailed));
        tvForgotPasswordDisplayStatus.setTextColor(Color.parseColor("#f44336"));
        tvForgotPasswordDisplayStatus.clearAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        isRecoveryFragmentOpen = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isRecoveryFragmentOpen = false;
    }
}
