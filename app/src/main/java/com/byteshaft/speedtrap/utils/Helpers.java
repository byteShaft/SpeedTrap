package com.byteshaft.speedtrap.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class Helpers {

    private static CountDownTimer countdownTimer;
    public static int countDownTimerMillisUntilFinished;
    private static ProgressDialog progressDialog;
    private static boolean isCountDownTimerRunning;


    public static final Runnable exitApp = new Runnable() {
        public void run() {
            MainActivity.getInstance().finish();
            System.exit(0);
        }
    };

    public static void setStatusBarTranslucent(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public static boolean isAnyLocationServiceAvailable() {
        LocationManager locationManager = getLocationManager();
        return isGpsEnabled(locationManager) || isNetworkBasedGpsEnabled(locationManager);
    }

    public static boolean isHighAccuracyLocationServiceAvailable() {
        LocationManager locationManager = getLocationManager();
        return isGpsEnabled(locationManager) && isNetworkBasedGpsEnabled(locationManager);
    }

    private static LocationManager getLocationManager() {
        return (LocationManager) AppGlobals.getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    private static boolean isGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private static boolean isNetworkBasedGpsEnabled(LocationManager locationManager) {
        return locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER));
    }

    public static void AlertDialogMessage(Context context, String title, String message, String neutralButtonText) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(neutralButtonText, null)
                .show();
    }

    public static void AlertDialogMessageWithPositiveFunction(
            Context context, String title, String message, String positiveButtonText,
            final Runnable listenerOk) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerOk.run();
                    }
                })
                .show();
    }

    public static void AlertDialogWithPositiveFunctionNegativeButton(Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, final Runnable listenerYes) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void AlertDialogWithPositiveNegativeFunctions(
            Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, final Runnable listenerYes, final Runnable listenerNo) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listenerNo.run();
                    }
                })
                .show();
    }

    public static void AlertDialogWithPositiveNegativeFunctionsNeutralButton(
            Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, String neutralButtonText, final Runnable listenerYes,
            final Runnable listenerNo) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listenerNo != null) {
                            listenerNo.run();
                        }
                    }
                })
                .setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    public static void AlertDialogWithPositiveNegativeNeutralFunctions(
            Context context, String title, String message, String positiveButtonText,
            String negativeButtonText, String neutralButtonText, final Runnable listenerYes,
            final Runnable listenerNo) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        listenerYes.run();
                    }
                })
                .setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listenerNo != null) {
                            listenerNo.run();
                        }
                    }
                })
                .setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public static void showProgressDialog(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public static boolean hasPermissionsForDevicesAboveMarshmallowIfNotRequestPermissions(Activity activity) {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null && PERMISSIONS != null) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
                    return false;
                }
            }
        }
        return true;
    }

    public static void loadFragment(FragmentManager fragmentManager, android.support.v4.app.Fragment fragment, boolean openingRecoveryFragment,
                                    String fragmentName) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (openingRecoveryFragment) {
            transaction.setCustomAnimations(R.anim.anim_transition_fragment_slide_left_enter, R.anim.anim_transition_fragment_slide_right_exit,
                    R.anim.anim_transition_fragment_slide_right_enter, R.anim.anim_transition_fragment_slide_left_exit);
        } else {
            transaction.setCustomAnimations(R.anim.anim_transition_fragment_slide_right_enter, R.anim.anim_transition_fragment_slide_left_exit,
                    R.anim.anim_transition_fragment_slide_left_enter, R.anim.anim_transition_fragment_slide_right_exit);
        }
        if (fragmentName != null) {
            if (fragmentName.equals("MapFragment") || fragmentName.equals("WelcomeFragment")) {
                MainActivity.fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transaction.replace(R.id.container, fragment);
            } else {
                transaction.replace(R.id.container, fragment).addToBackStack(fragmentName);
            }
        }else {
            transaction.replace(R.id.container, fragment);
        }
        transaction.commit();
    }

    public static void showSnackBar(String message, int time, int textColor) {
        Snackbar snackbar = Snackbar.make(MainActivity.getInstance().findViewById(R.id.container), message, time);
        View snackBarView = snackbar.getView();
        TextView snackBarText = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        snackBarText.setGravity(Gravity.CENTER_HORIZONTAL);
        snackBarText.setTextColor(textColor);
        snackBarText.setMaxLines(3);
        snackbar.show();
    }

    public static String secondsToMinutesSeconds(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static void setCountDownTimer(int totalTime, int tickTime, final Runnable functionTick, final Runnable functionFinished) {
        countdownTimer = new CountDownTimer(totalTime, tickTime) {
            public void onTick(long millisUntilFinished) {
                isCountDownTimerRunning = true;
                functionTick.run();
                countDownTimerMillisUntilFinished = (int) millisUntilFinished;
            }

            public void onFinish() {
                functionFinished.run();
                isCountDownTimerRunning = false;
            }
        };
        countdownTimer.start();
    }

    public static void stopCountDownTimer() {
        if (isCountDownTimerRunning) {
            countdownTimer.cancel();
        }
    }

    public static final Runnable recheckLocationServiceStatus = new Runnable() {
        public void run() {
            if (!Helpers.isAnyLocationServiceAvailable()) {
                Helpers.AlertDialogWithPositiveNegativeNeutralFunctions(MainActivity.getInstance(),
                        "Location Service disabled", "Enable device GPS to continue", "Settings", "ReCheck", "Dismiss",
                        openLocationServiceSettings, recheckLocationServiceStatus);
            }
        }
    };

    public static final Runnable openLocationServiceSettings = new Runnable() {
        public void run() {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            MainActivity.getInstance().startActivity(intent);
        }
    };

    public static final Runnable openApplicationSettingsForPermissionRequest = new Runnable() {
        public void run() {
            final Intent i = new Intent();
            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setData(Uri.parse("package:" + MainActivity.getInstance().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            MainActivity.getInstance().startActivity(i);
        }
    };

    private static boolean areGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private static final Runnable openInstallationActivityForPlayServices = new Runnable() {
        public void run() {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gms&hl=en"));
            MainActivity.getInstance().startActivity(i);
        }
    };

    public static boolean isDeviceReadyForLocationAcquisition(Activity activity) {
        boolean ready = true;
        if (hasPermissionsForDevicesAboveMarshmallowIfNotRequestPermissions(activity)) {
            if (!isAnyLocationServiceAvailable()) {
                AlertDialogWithPositiveNegativeNeutralFunctions(activity,
                        "Location Service disabled", "Enable location services to continue", "Settings", "ReCheck", "Dismiss",
                        openLocationServiceSettings, recheckLocationServiceStatus);
                ready = false;
            } else if (!areGooglePlayServicesAvailable(activity)) {
                Helpers.AlertDialogWithPositiveFunctionNegativeButton(activity, "Location components missing",
                        "You need to install GooglePlayServices to continue", "Install",
                        "Dismiss", Helpers.openInstallationActivityForPlayServices);
                ready = false;
            }
        }
        return ready;
    }

}
