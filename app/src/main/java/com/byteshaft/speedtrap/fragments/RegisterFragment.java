package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.Helpers;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class RegisterFragment extends Fragment {

    View baseViewRegisterFragment;
    CheckBox cbRegisterStaffCheck;
    CheckBox cbRegisterTermsOfServiceCheck;
    static String urlTOS = "";
    EditText etRegisterUserFullName;
    EditText etRegisterUserEmail;
    EditText etRegisterUserEmailRepeat;
    EditText etRegisterUserPassword;
    EditText etRegisterUserConfirmPassword;
    EditText etRegisterUserContactNumber;

    String sRegisterFullName;
    String sRegisterEmail;
    String sRegisterEmailRepeat;
    String sRegisterPassword;
    String sRegisterConfirmPassword;
    String sRegisterContactNumber;

    Button btnRegisterCreateAccount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewRegisterFragment = inflater.inflate(R.layout.fragment_register, container, false);

        etRegisterUserFullName = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_full_name);
        etRegisterUserEmail = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_email);
        etRegisterUserEmailRepeat = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_email_repeat);
        etRegisterUserPassword = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_password);
        etRegisterUserConfirmPassword = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_confirm_password);
        etRegisterUserContactNumber = (EditText) baseViewRegisterFragment.findViewById(R.id.et_register_contact_number);

        btnRegisterCreateAccount = (Button) baseViewRegisterFragment.findViewById(R.id.btn_register_create_account);
        btnRegisterCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sRegisterFullName = etRegisterUserFullName.getText().toString();
                sRegisterEmail = etRegisterUserEmail.getText().toString();
                sRegisterEmailRepeat = etRegisterUserEmailRepeat.getText().toString();
                sRegisterPassword = etRegisterUserPassword.getText().toString();
                sRegisterConfirmPassword = etRegisterUserConfirmPassword.getText().toString();
                sRegisterContactNumber = etRegisterUserContactNumber.getText().toString();

                if (validateRegisterInfo()) {

                }

                Helpers.loadFragment(MainActivity.fragmentManager, new ConfirmationFragment(), false, "Confirmation Fragment");
            }
        });

        cbRegisterTermsOfServiceCheck = (CheckBox) baseViewRegisterFragment.findViewById(R.id.cb_register_terms_of_service_check);

        SpannableStringBuilder text = new SpannableStringBuilder();
        text.append(getString(R.string.TermsOfServiceInitialText)).append(" ");

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Helpers.loadFragment(MainActivity.fragmentManager, new TermsFragment(), false, "TermsFragment");
            }
        };
        TextPaint ds = new TextPaint();
        clickableSpan.updateDrawState(ds);
        ds.setUnderlineText(false);
        text.append(getString(R.string.TermsOfServiceLateralText));

        text.setSpan(clickableSpan, getString(R.string.TermsOfServiceInitialText).length() + 1,
                getString(R.string.TermsOfServiceInitialText).length() + 1 + getString(R.string.TermsOfServiceLateralText).length(), 0);
        cbRegisterTermsOfServiceCheck.setMovementMethod(LinkMovementMethod.getInstance());
        cbRegisterTermsOfServiceCheck.setText(text, TextView.BufferType.SPANNABLE);
        return baseViewRegisterFragment;
    }


    public boolean validateRegisterInfo() {
        boolean valid = true;

        if (sRegisterFullName.trim().isEmpty()) {
            etRegisterUserFullName.setError(getString(R.string.errorEmpty));
            valid = false;
        } else {
            etRegisterUserFullName.setError(null);
        }

        if (sRegisterEmail.trim().isEmpty()) {
            etRegisterUserEmail.setError(getString(R.string.errorEmpty));
            valid = false;
        } else if (!sRegisterEmail.trim().isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(sRegisterEmail).matches()) {
            etRegisterUserEmail.setError(getString(R.string.errorInvalidEmail));
            valid = false;
        } else if (!sRegisterEmail.equals(sRegisterEmailRepeat)) {
            etRegisterUserEmailRepeat.setError(getString(R.string.errorEmailDoesNotMatch));
            valid = false;
        } else {
            etRegisterUserEmail.setError(null);
        }

        if (sRegisterPassword.trim().isEmpty() || sRegisterPassword.length() < 6) {
            etRegisterUserPassword.setError(getString(R.string.errorMinimumSixCharacters));
            valid = false;
        } else if (!sRegisterPassword.equals(sRegisterConfirmPassword)) {
            etRegisterUserConfirmPassword.setError(getString(R.string.errorPasswordDoesNotMatch));
            valid = false;
        } else {
            etRegisterUserPassword.setError(null);
        }

        if (sRegisterContactNumber.trim().isEmpty()) {
            etRegisterUserContactNumber.setError(getString(R.string.errorEmpty));
            valid = false;
        } else {
            etRegisterUserContactNumber.setError(null);
        }

        if (valid && !cbRegisterTermsOfServiceCheck.isChecked()) {
            Helpers.showSnackBar(getView(), getString(R.string.errorCheckTermsOfServiceToContinue), Snackbar.LENGTH_LONG, "#f44336");
            valid = false;
        }

        return valid;
    }

}
