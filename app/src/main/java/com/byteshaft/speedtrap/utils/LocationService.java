package com.byteshaft.speedtrap.utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.byteshaft.speedtrap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by fi8er1 on 24/04/2017.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public GoogleApiClient mGoogleApiClient;
    public boolean isBackgroundServiceRunning;
    LocationRequest mLocationRequest;
    LocalBroadcastManager broadcaster;
    boolean isUserAlertInProcess;
    private int recursionCounter = 0;
    private int locationChangedCounter;
    private PowerManager.WakeLock mWakeLock;
    private boolean isLocationAcquired;
    private DatabaseHelpers mDatabaseHelpers;
    private int userAlertCalledCounter = 0;
    private SoundFX soundFX;

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mDatabaseHelpers = new DatabaseHelpers(AppGlobals.getContext());
        soundFX = new SoundFX(AppGlobals.getContext());
        if (this.mWakeLock == null) {
            this.mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationWakeLock");
        }
        if (!this.mWakeLock.isHeld()) {
            this.mWakeLock.acquire();
        }
        startLocationServices();
        if (!isBackgroundServiceRunning) {
            isBackgroundServiceRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        isBackgroundServiceRunning = false;
        isLocationAcquired = false;
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        locationChangedCounter = 0;
        recursionCounter = 0;
        userAlertCalledCounter = 0;
        isBackgroundServiceRunning = false;
        isLocationAcquired = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LocationChanged", "" + location);
        if (locationChangedCounter > 1) {
            isLocationAcquired = true;
            sendLocationDataToMapFragmentToUpdateUI(true, false, R.drawable.selector_map_current_location,
                    String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), (int) (location.getSpeed() * 3600) / 1000);
            if (!isUserAlertInProcess && AppGlobals.isSoundAlertEnabled()) {
//                alertUser(new LatLng(location.getLatitude(), location.getLongitude()), (int) (location.getSpeed() * 3600) / 1000);
                alertUser(new LatLng(location.getLatitude(), location.getLongitude()), 121);
            }
        } else {
            locationChangedCounter++;
        }
    }

    public void startLocationServices() {
        connectGoogleApiClient();
    }

    private void connectGoogleApiClient() {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    protected void createLocationRequest() {
        long INTERVAL = 0;
        long FASTEST_INTERVAL = 0;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
        recursionCounter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        broadcaster = LocalBroadcastManager.getInstance(AppGlobals.getContext());
    }

    public void stopLocationService() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        locationChangedCounter = 0;
        recursionCounter = 0;
        userAlertCalledCounter = 0;
        isLocationAcquired = false;
        isBackgroundServiceRunning = false;
        this.stopService(new Intent(AppGlobals.getContext(), LocationService.class));
    }

    void recursionCounter() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recursionCounter > 90 && mGoogleApiClient.isConnected()) {
                    stopLocationService();
                    sendLocationDataToMapFragmentToUpdateUI(false, true, R.drawable.selector_map_current_location, null, null, 0);
                } else if (mGoogleApiClient.isConnected() && !isLocationAcquired) {
                    recursionCounter();
                    recursionCounter++;
                    if ((recursionCounter % 2) == 0) {
                        sendLocationDataToMapFragmentToUpdateUI(false, false, 1, null, null, 0);
                    } else {
                        sendLocationDataToMapFragmentToUpdateUI(false, false, 0, null, null, 0);
                    }

                }
            }
        }, 1000);
    }

    private void alertUser(LatLng location, int travellingSpeed) {
        isUserAlertInProcess = true;
        if (travellingSpeed > AppGlobals.getAlertSpeedLimit() / 2) {
            ArrayList<HashMap> listOfTrapsFromDatabase = mDatabaseHelpers.getAllRecords();
            ArrayList<Integer> listOfLocationDistancesInAlertRadius = new ArrayList<>();
            for (int i = 0; i < listOfTrapsFromDatabase.size(); i++) {
                String[] locationFromDatabase = listOfTrapsFromDatabase.get(i).get("location").toString().split(",");
                int distanceMeasured = (int) SphericalUtil.computeDistanceBetween(location,
                        new LatLng(Double.parseDouble(locationFromDatabase[0]), Double.parseDouble(locationFromDatabase[1])));
                if (distanceMeasured < AppGlobals.getAlertDistance()) {
                    listOfLocationDistancesInAlertRadius.add(distanceMeasured);
                    Log.i("DistanceLessThanLimit", "" + distanceMeasured);
                }
            }
            if (!listOfLocationDistancesInAlertRadius.isEmpty()) {
                int minimumDistanceMeasured = Collections.min(listOfLocationDistancesInAlertRadius);
                if (minimumDistanceMeasured < AppGlobals.getAlertDistance() / 3) {
                    Log.e("Condition1st", "" + minimumDistanceMeasured);
                    if (travellingSpeed > AppGlobals.getAlertSpeedLimit() && (!soundFX.isAlertInProgress || soundFX.typeOfAlertInProgress != 3)) {
                        soundFX.playSound(AppGlobals.getContext(), SoundFX.soundEffectThree, AppGlobals.getAlertVolume(), true);
                        soundFX.typeOfAlertInProgress = 3;
                        Log.e("Condition1st", "One " + travellingSpeed);
                    } else if (travellingSpeed < AppGlobals.getAlertSpeedLimit() && (!soundFX.isAlertInProgress || soundFX.typeOfAlertInProgress != 1)) {
                        soundFX.playSound(AppGlobals.getContext(), SoundFX.soundEffectOne, AppGlobals.getAlertVolume(), true);
                        soundFX.typeOfAlertInProgress = 1;
                        Log.e("Condition1st", "Two " + travellingSpeed);
                    }
                } else if (minimumDistanceMeasured < AppGlobals.getAlertDistance() / 2) {
                    Log.e("Condition2nd", "" + minimumDistanceMeasured);
                    if (travellingSpeed > AppGlobals.getAlertSpeedLimit() && (!soundFX.isAlertInProgress || soundFX.typeOfAlertInProgress != 2)) {
                        soundFX.playSound(AppGlobals.getContext(), SoundFX.soundEffectTwo, AppGlobals.getAlertVolume(), true);
                        soundFX.typeOfAlertInProgress = 2;
                    } else if (travellingSpeed < AppGlobals.getAlertSpeedLimit() && !soundFX.isAlertInProgress || soundFX.typeOfAlertInProgress != 1) {
                        soundFX.playSound(AppGlobals.getContext(), SoundFX.soundEffectOne, AppGlobals.getAlertVolume(), true);
                        soundFX.typeOfAlertInProgress = 1;
                    }
                } else if (minimumDistanceMeasured < AppGlobals.getAlertDistance() && (!soundFX.isAlertInProgress || soundFX.typeOfAlertInProgress != 1)) {
                    soundFX.playSound(AppGlobals.getContext(), SoundFX.soundEffectOne, AppGlobals.getAlertVolume(), true);
                    soundFX.typeOfAlertInProgress = 1;
                    Log.e("Condition3rd", "" + minimumDistanceMeasured);
                }
            } else {
                soundFX.stopSound();
            }
            isUserAlertInProcess = false;
        } else {
            if (soundFX.isAlertInProgress) {
                soundFX.stopSound();
            }
        }

    }


    private void sendLocationDataToMapFragmentToUpdateUI(boolean locationAcquired, boolean showLocationAcquisitionFailedDialog,
                                                         int currentLocationIconResourceType, String latitude, String longitude,
                                                         int travellingSpeed) {
        Intent intent = new Intent("LocationData");
        intent.putExtra("location_acquired", String.valueOf(locationAcquired));
        intent.putExtra("location_acquisition_failed_dialog", String.valueOf(showLocationAcquisitionFailedDialog));
        intent.putExtra("current_location_icon_resource_type", currentLocationIconResourceType);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("travelling_speed", travellingSpeed);
        broadcaster.sendBroadcast(intent);
    }

}
