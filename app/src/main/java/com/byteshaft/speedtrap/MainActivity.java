package com.byteshaft.speedtrap;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.RelativeLayout;

import com.byteshaft.speedtrap.fragments.WelcomeFragment;
import com.byteshaft.speedtrap.utils.Helpers;

import static com.byteshaft.speedtrap.fragments.WelcomeFragment.isMainActivityForeground;
import static com.byteshaft.speedtrap.utils.Helpers.openLocationServiceSettings;
import static com.byteshaft.speedtrap.utils.Helpers.recheckLocationServiceStatus;

public class MainActivity extends FragmentActivity {

    public static FragmentManager fragmentManager;
    public static RelativeLayout rlMainActivity;
    private static MainActivity sInstance;
//    public static SoftKeyboard mSoftKeyboard;

    public static MainActivity getInstance() {
        return sInstance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sInstance = this;
        Helpers.setStatusBarTranslucent(this);
        rlMainActivity = (RelativeLayout) findViewById(R.id.rl_main_layout);
        fragmentManager = getSupportFragmentManager();
//        RelativeLayout rlMainLayout = (RelativeLayout) findViewById(R.id.rl_main_layout);
//        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
//        mSoftKeyboard = new SoftKeyboard(rlMainLayout, im);
        Helpers.loadFragment(fragmentManager, new WelcomeFragment(), false, null);
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Helpers.showSnackBar(rlMainActivity.getRootView(), "Location permission granted", Snackbar.LENGTH_SHORT, "#A4C639");
                    if (!Helpers.isAnyLocationServiceAvailable()) {
                        Helpers.AlertDialogWithPositiveNegativeNeutralFunctions(this,
                                "Location Service disabled", "Enable location service to continue", "Settings", "ReCheck", "Dismiss",
                                openLocationServiceSettings, recheckLocationServiceStatus);
                    } else {
                        if (!WelcomeFragment.wasPermissionRequestedOnStartup) {
                            // TODO: fire login task here
                        }
                    }
                } else {
                    Helpers.showSnackBar(rlMainActivity.getRootView(), "Location permission denied", Snackbar.LENGTH_SHORT, "#f44336");
                    if (!WelcomeFragment.wasPermissionRequestedOnStartup) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isMainActivityForeground) {
                                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.this, "Permissions denied",
                                            "You cannot proceed without granting location access. Go to app settings and grant location permission",
                                            "Open Settings", "Dismiss", Helpers.openApplicationSettingsForPermissionRequest);
                                }
                            }
                        }, 1500);
                    }
                }

            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isMainActivityForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMainActivityForeground = true;
    }
}
