package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;

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
    Animation animTextViewFading;
    String passwordRecoveryEmail;
    String forgotPasswordConfirmationCode;
    String forgotPasswordNewPassword;
    String forgotPasswordNewPasswordRepeat;

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

        animTextViewFading = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_text_complete_fading);
        btnForgotPasswordRecover.setOnClickListener(this);
        btnForgotPasswordNewSubmit.setOnClickListener(this);
        
        
        return baseViewRecoveryFragment;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_forgot_password_recover:
                passwordRecoveryEmail = etForgotPasswordEmail.getText().toString();
                if (validateRecoverInfo()) {
                    MainActivity.mSoftKeyboard.closeSoftKeyboard();
//                    new UserRecoveryTask().execute();
                }
                break;
            case R.id.btn_forgot_password_new_submit:
                forgotPasswordConfirmationCode = etForgotPasswordConfirmationCode.getText().toString();
                forgotPasswordNewPassword = etForgotPasswordNewPassword.getText().toString();
                forgotPasswordNewPasswordRepeat = etForgotPasswordConfirmPassword.getText().toString();

                if (validateSubmitInfo()) {
                    MainActivity.mSoftKeyboard.closeSoftKeyboard();
//                    new ChangePasswordTask().execute();
                }
                break;
        }
    }

    public boolean validateRecoverInfo() {
        boolean valid = true;
        if (passwordRecoveryEmail.trim().isEmpty()) {
            etForgotPasswordEmail.setError("Empty");
            valid = false;
        } else if (!passwordRecoveryEmail.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(passwordRecoveryEmail).matches()) {
            etForgotPasswordEmail.setError("Invalid E-Mail");
            valid = false;
        } else {
            etForgotPasswordEmail.setError(null);
        }
        return valid;
    }

    public boolean validateSubmitInfo() {
        boolean valid = true;
        if (forgotPasswordConfirmationCode.trim().isEmpty() || forgotPasswordConfirmationCode.trim().length() < 4) {
            etForgotPasswordConfirmationCode.setError("At least 4 characters");
            valid = false;
        } else {
            etForgotPasswordConfirmationCode.setError(null);
        }

        if (forgotPasswordNewPassword.length() < 6) {
            etForgotPasswordNewPassword.setError("At least 6 characters");
            valid = false;
        } else {
            etForgotPasswordNewPassword.setError(null);
        }

        if (!forgotPasswordNewPassword.equals(forgotPasswordNewPasswordRepeat)) {
            etForgotPasswordConfirmPassword.setError("Password does not match");
            valid = false;
        } else {
            etForgotPasswordConfirmPassword.setError(null);
        }
        return valid;
    }
    
}
