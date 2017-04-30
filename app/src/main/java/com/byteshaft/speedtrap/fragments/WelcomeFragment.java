package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.Helpers;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class WelcomeFragment extends Fragment implements View.OnClickListener {

    View baseViewWelcomeFragment;
    public static boolean wasPermissionRequestedOnStartup;

    public static boolean isMainActivityForeground;
    Button btnLogin;
    Button btnRegister;
    TextView tvForgotPassword;
    EditText etLoginEmail;
    EditText etLoginPassword;
    public static String sLoginEmail;
    String sLoginPassword;

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
                if (isMainActivityForeground) {
                    wasPermissionRequestedOnStartup = true;
                    Helpers.hasPermissionsForDevicesAboveMarshmallowIfNotRequestPermissions(getActivity());
                }
            }
        }, 2000);

        return baseViewWelcomeFragment;
    }


    @Override
    public void onClick(View v) {
//        MainActivity.mSoftKeyboard.closeSoftKeyboard();
        switch (v.getId()) {
            case R.id.btn_login_login:
                sLoginEmail = etLoginEmail.getText().toString();
                sLoginPassword = etLoginPassword.getText().toString();
                if (validateLoginInput()) {
                    wasPermissionRequestedOnStartup = false;
                    if (Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
//                    taskUserLogin = (UserLoginTask) new UserLoginTask().execute();
                        Helpers.loadFragment(MainActivity.fragmentManager, new MapFragment(), false, "MapFragment");
                    }
                }
                break;
            case R.id.btn_login_register:
                Helpers.loadFragment(MainActivity.fragmentManager, new RegisterFragment(), false, "RegisterFragment");
                break;
            case R.id.tv_login_forgot_password:
                Helpers.loadFragment(MainActivity.fragmentManager, new RecoveryFragment(), true, "Recovery Fragment");
                break;
        }
    }

    public boolean validateLoginInput() {
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

}
