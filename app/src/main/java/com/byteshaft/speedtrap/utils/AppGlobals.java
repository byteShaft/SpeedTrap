package com.byteshaft.speedtrap.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppGlobals extends Application {

    private static final String FIRST_RUN_MAP = "first_run_map";
    private static final String UNIQUE_DEVICE_ID = "unique_device_id";
    private static final String TRAP_ALERT_DISTANCE = "trap_alert_distance";
    private static final String LOGGED_IN = "logged_in";
    private static final String PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled";
    private static final String USER_NAME = "user_name";
    private static final String PERSON_NAME = "person_name";
    private static final String USER_ID = "user_id";
    private static final String USER_TYPE = "user_type";
    private static final String TOKEN = "token";
    private static final String GCM_TOKEN = "gcm_token";
    private static Context sContext;
    private static SharedPreferences sPreferences;

    public static Context getContext() {
        return sContext;
    }

    public static void setMapFirstRun(boolean firstRunMap) {
        sPreferences.edit().putBoolean(FIRST_RUN_MAP, firstRunMap).apply();
    }
    public static boolean isMapFirstRun() {
        return sPreferences.getBoolean(FIRST_RUN_MAP, true);
    }

    public static boolean isLoggedIn() {
        return sPreferences.getBoolean(LOGGED_IN, false);
    }

    public static void setPushNotificationsEnabled(boolean enabled) {
        sPreferences.edit().putBoolean(PUSH_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public static boolean isPushNotificationsEnabled() {
        return sPreferences.getBoolean(PUSH_NOTIFICATIONS_ENABLED, false);
    }

    public static void setLoggedIn(boolean loggedIn) {
        sPreferences.edit().putBoolean(LOGGED_IN, loggedIn).apply();
    }

    public static void putUsername(String username) {
        sPreferences.edit().putString(USER_NAME, username).apply();
    }

    public static String getUsername() {
        return sPreferences.getString(USER_NAME, null);
    }

    public static void putUserID(int userID) {
        sPreferences.edit().putInt(USER_ID, userID).apply();
    }

    public static int getUserID() {
        return sPreferences.getInt(USER_ID, -1);
    }

    public static void putTrapAlertDistance(int radius) {
        sPreferences.edit().putInt(TRAP_ALERT_DISTANCE, radius).apply();
    }

    public static int getTrapAlertDistance() {
        return sPreferences.getInt(TRAP_ALERT_DISTANCE, 1000);
    }

    public static void putToken(String token) {
        sPreferences.edit().putString(TOKEN, token).apply();
    }

    public static String getToken() {
        return sPreferences.getString(TOKEN, null);
    }

    public static void putGcmToken(String gcmToken) {
        sPreferences.edit().putString(GCM_TOKEN, gcmToken).apply();
    }

    public static String getGcmToken() {
        return sPreferences.getString(GCM_TOKEN, null);
    }

    public static String getPeronName() {
        return sPreferences.getString(PERSON_NAME, null);
    }

    public static void putPersonName(String name) {
        sPreferences.edit().putString(PERSON_NAME, name).apply();
    }

    public static int getUserType() {
        return sPreferences.getInt(USER_TYPE, 0);
    }

    public static void putUserType(int userType) {
        sPreferences.edit().putInt(USER_TYPE, userType).apply();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        sPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
