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
 * Created by fi8er1 on 23/04/2017.
 */

public class WelcomeFragment extends Fragment implements View.OnClickListener {

    View baseViewWelcomeFragment;
    public static boolean wasPermissionRequestedOnStartup;

    Button btnLogin;
    Button btnRegister;
    TextView tvForgotPassword;
    EditText etLoginEmail;
    EditText etLoginPassword;
    public static String sLoginEmail;
    static String sLoginPassword;
    public static boolean bIsTrapRetrievalRequestFromLogin;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewWelcomeFragment = inflater.inflate(R.layout.fragment_welcome, container, false);

        etLoginEmail = (EditText) baseViewWelcomeFragment.findViewById(R.id.et_login_email);
        etLoginPassword = (EditText) baseViewWelcomeFragment.findViewById(R.id.et_login_password);
        btnLogin = (Button) baseViewWelcomeFragment.findViewById(R.id.btn_login_login);
        btnLogin.setOnClickListener(this);
        btnRegister = (Button) baseViewWelcomeFragment.findViewById(R.id.btn_login_register);
        btnRegister.setOnClickListener(this);
        tvForgotPassword = (TextView) baseViewWelcomeFragment.findViewById(R.id.tv_login_forgot_password);
        tvForgotPassword.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.isMainActivityForeground) {
                    wasPermissionRequestedOnStartup = true;
                    Helpers.hasPermissionsForDevicesAboveMarshmallowIfNotRequestPermissions(getActivity());
                }
            }
        }, 2000);

        return baseViewWelcomeFragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login_login:
                sLoginEmail = etLoginEmail.getText().toString();
                sLoginPassword = etLoginPassword.getText().toString();
                if (isLoginDataInputValid()) {
                    wasPermissionRequestedOnStartup = false;
                    if (Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
                        sendLoginRequest();
                    }
                }
                break;
            case R.id.btn_login_register:
                Helpers.loadFragment(MainActivity.fragmentManager, new RegisterFragment(), false, "RegisterFragment");
                break;
            case R.id.tv_login_forgot_password:
                RecoveryFragment.isRecoveryOpenedFromLoginRequest = false;
                Helpers.loadFragment(MainActivity.fragmentManager, new RecoveryFragment(), true, "RecoveryFragment");
                break;
        }
    }

    public boolean isLoginDataInputValid() {
        boolean valid = true;
        if (sLoginEmail.trim().isEmpty()) {
            etLoginEmail.setError(getString(R.string.errorEmpty));
            valid = false;
        } else if (!sLoginEmail.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(sLoginEmail).matches()) {
            etLoginEmail.setError(getString(R.string.errorInvalidEmail));
            valid = false;
        } else {
            etLoginEmail.setError(null);
        }
        return valid;
    }

    public static void sendLoginRequest() {
        HttpRequest request = new HttpRequest(MainActivity.getInstance());
        Helpers.showProgressDialog(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.messageSendingLoginRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                onLoginSuccess(MainActivity.getInstance().getString(R.string.messageLoginRequestConfirmed), request.getResponseText());
                                break;
                            case HttpURLConnection.HTTP_FORBIDDEN:
                                onLoginFailed(MainActivity.getInstance().getString(R.string.messageLoginRequestFailed) + "\n" +
                                        request.getResponseText());
                                ConfirmationFragment.isFragmentOpenedFromLogin = true;
                                Helpers.loadFragment(MainActivity.fragmentManager, new ConfirmationFragment(), false, "ConfirmationFragment");
                                break;
                            default:
                                onLoginFailed(MainActivity.getInstance().getString(R.string.messageLoginRequestFailed) + "\n" +
                                        request.getResponseText());
                                try {
                                    JSONObject jsonObject = new JSONObject(request.getResponseText());
                                    if (jsonObject.get("detail").toString().equalsIgnoreCase("STAFF_ACCOUNT_NOT_SET")) {
                                        RecoveryFragment.isRecoveryOpenedFromLoginRequest = true;
                                        Helpers.loadFragment(MainActivity.fragmentManager, new RecoveryFragment(), true, "RecoveryFragment");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onLoginFailed(MainActivity.getInstance().getString(R.string.messageLoginRequestFailed));
            }
        });
        request.open("POST", EndPoints.LOGIN);
        request.send(getLoginString(sLoginEmail, sLoginPassword));
    }

    public static String getLoginString(String email, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private static void onLoginSuccess(String message, String responseText) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.GREEN);
        try {
            JSONObject jsonObjectLoginResponseMain = new JSONObject(responseText);
            JSONObject jsonObjectLoginResponsePreferences;

            Log.i("one", "" + jsonObjectLoginResponseMain);
            AppGlobals.putToken(jsonObjectLoginResponseMain.getString("token"));
            Log.i("Token", "" + AppGlobals.getToken());
            AppGlobals.putUserType(Integer.parseInt(jsonObjectLoginResponseMain.getString("user_type")));

            if (AppGlobals.getUserType() == 2) {
                jsonObjectLoginResponsePreferences = new JSONObject(jsonObjectLoginResponseMain.get("staff_preferences").toString());
                AppGlobals.setMobileSpeedTrapNotifier(jsonObjectLoginResponsePreferences.getBoolean("mobile_speed_trap_notifier"));
            } else {
                jsonObjectLoginResponsePreferences = new JSONObject(jsonObjectLoginResponseMain.get("user_preferences").toString());
                AppGlobals.putAlertSpeedLimit(jsonObjectLoginResponsePreferences.getInt("warning_speed_limit"));
            }
            Log.i("Two", "" + jsonObjectLoginResponsePreferences);
            AppGlobals.putAlertDistance(jsonObjectLoginResponsePreferences.getInt("warning_distance"));
            AppGlobals.setSoundAlertEnabled(jsonObjectLoginResponsePreferences.getBoolean("sound_alert"));
            AppGlobals.putAlertVolume(jsonObjectLoginResponsePreferences.getInt("alert_volume"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        bIsTrapRetrievalRequestFromLogin = true;
        MainActivity.sendTrapRetrievalRequest();
    }

    private static void onLoginFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.RED);
    }

}
