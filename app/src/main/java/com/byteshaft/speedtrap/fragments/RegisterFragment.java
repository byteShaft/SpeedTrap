package com.byteshaft.speedtrap.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
    public static String sRegisterEmail;
    String sRegisterEmailRepeat;
    String sRegisterPassword;
    String sRegisterConfirmPassword;

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

                if (isRegistrationDataInputValid()) {
                    sendRegistrationRequest();
                    Log.i("valid", "valid");
                }
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

    public boolean isRegistrationDataInputValid() {
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

        if (valid && !cbRegisterTermsOfServiceCheck.isChecked()) {
            Helpers.showSnackBar(getString(R.string.errorCheckTermsOfServiceToContinue), Snackbar.LENGTH_LONG, Color.RED);
            valid = false;
        }
        return valid;
    }

    private void sendRegistrationRequest() {
        HttpRequest request = new HttpRequest(getActivity());
        Helpers.showProgressDialog(getActivity(), getString(R.string.messageSendingRegistrationRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                onRegistrationSuccess(getString(R.string.messageRegistrationRequestSent));
                                break;
                            default:
                                onRegistrationFailed(getString(R.string.messageRegistrationRequestFailed) + "\n" +
                                        request.getResponseText());
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onRegistrationFailed(getString(R.string.messageRegistrationRequestFailed) + "\n" +
                        request.getResponseText());
            }
        });
        request.open("POST", EndPoints.REGISTER);
            request.send(getRegistrationString(sRegisterEmail, sRegisterFullName, sRegisterPassword));
    }

    public static String getRegistrationString(String email, String fullName, String password) {
        JSONObject json = new JSONObject();
        try {
            json.put("email", email);
            json.put("full_name", fullName);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private void onRegistrationSuccess(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.GREEN);
        Helpers.loadFragment(MainActivity.fragmentManager, new ConfirmationFragment(), false, "ConfirmationFragment");
        ConfirmationFragment.isFragmentOpenedFromLogin = false;
    }

    private void onRegistrationFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_SHORT, Color.RED);
    }

}
