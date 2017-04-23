package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
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

    Button btnLogin;
    Button btnRegister;
    TextView tvForgotPassword;
    EditText etLoginEmail;
    EditText etLoginPassword;
    String sLoginEmail;
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

        return baseViewWelcomeFragment;
    }


    @Override
    public void onClick(View v) {
        MainActivity.mSoftKeyboard.closeSoftKeyboard();
        switch (v.getId()) {
            case R.id.btn_login_login:
                sLoginEmail = etLoginEmail.getText().toString();
                sLoginPassword = etLoginPassword.getText().toString();
                if (validateLoginInput()) {
//                    taskUserLogin = (UserLoginTask) new UserLoginTask().execute();
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
            etLoginEmail.setError("Empty");
            valid = false;
        } else if (!sLoginEmail.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(sLoginEmail).matches()) {
            etLoginEmail.setError("Invalid E-Mail");
            valid = false;
        } else {
            etLoginEmail.setError(null);
        }

        if (sLoginPassword.trim().isEmpty() || sLoginPassword.length() < 6) {
            etLoginPassword.setError("Minimum 6 Characters");
            valid = false;
        } else {
            etLoginPassword.setError(null);
        }
        return valid;
    }

}
