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
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by fi8er1 on 24/04/2017.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private static int recursionCounter = 0;
    private PowerManager.WakeLock mWakeLock;
    public boolean bLocationAcquired;

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        PowerManager mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
        if (this.mWakeLock == null) {
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LocationWakeLock");
        }

        if (!this.mWakeLock.isHeld()) { //**Added this
            this.mWakeLock.acquire();
        }
        startLocationServices();
        return START_STICKY;
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
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        Log.i("locationChanged", "" + location);
        if (!bLocationAcquired) {
            bLocationAcquired = true;
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
    }

    public void stopLocationService() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        recursionCounter = 0;
        bLocationAcquired = false;
    }

    void recursionCounter() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (recursionCounter > 160 && mGoogleApiClient.isConnected()) {
                    stopLocationService();
                } else if (mGoogleApiClient.isConnected()) {
                    recursionCounter();
                    recursionCounter++;
                }
            }
        },1000);
    }
}
