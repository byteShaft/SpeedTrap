package com.byteshaft.speedtrap;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.RelativeLayout;

import com.byteshaft.speedtrap.fragments.MapFragment;
import com.byteshaft.speedtrap.fragments.WelcomeFragment;
import com.byteshaft.speedtrap.utils.AppGlobals;
import com.byteshaft.speedtrap.utils.Helpers;

import static com.byteshaft.speedtrap.utils.Helpers.openLocationServiceSettings;
import static com.byteshaft.speedtrap.utils.Helpers.recheckLocationServiceStatus;

public class MainActivity extends FragmentActivity {


    public static boolean isMainActivityForeground;
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
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        rlMainActivity = (RelativeLayout) findViewById(R.id.rl_main_layout);
        fragmentManager = getSupportFragmentManager();
//        RelativeLayout rlMainLayout = (RelativeLayout) findViewById(R.id.rl_main_layout);
//        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
//        mSoftKeyboard = new SoftKeyboard(rlMainLayout, im);
        if (AppGlobals.isLoggedIn()) {
            Helpers.loadFragment(fragmentManager, new MapFragment(), false, "MapFragment");
        } else {
            Helpers.loadFragment(fragmentManager, new WelcomeFragment(), false, "WelcomeFragment");
        }
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
                    Helpers.showSnackBar(getString(R.string.messageLocationPermissionGranted), Snackbar.LENGTH_SHORT, Color.GREEN);
                    if (!Helpers.isAnyLocationServiceAvailable()) {
                        Helpers.AlertDialogWithPositiveNegativeNeutralFunctions(this,
                                getString(R.string.errorLocationServiceDisabled), getString(R.string.messageEnableLocationServiceToContinue), getString(R.string.buttonSettings),
                                getString(R.string.buttonRecheck), getString(R.string.buttonDismiss), openLocationServiceSettings, recheckLocationServiceStatus);
                    } else {
                        if (!WelcomeFragment.wasPermissionRequestedOnStartup) {
                            // TODO: fire login task here
                        }
                    }
                } else {
                    Helpers.showSnackBar(getString(R.string.messageLocationPermissionDenied), Snackbar.LENGTH_SHORT, Color.RED);
                    if (!WelcomeFragment.wasPermissionRequestedOnStartup) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isMainActivityForeground) {
                                    Helpers.AlertDialogWithPositiveFunctionNegativeButton(MainActivity.this, getString(R.string.errorPermissionDenied),
                                            getString(R.string.messageYouCannotProceedWithoutGrantingLocationAccess),
                                            getString(R.string.buttonSettings), getString(R.string.buttonDismiss), Helpers.openApplicationSettingsForPermissionRequest);
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
