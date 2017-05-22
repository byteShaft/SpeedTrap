package com.byteshaft.speedtrap.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteshaft.requests.HttpRequest;
import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.AppGlobals;
import com.byteshaft.speedtrap.utils.DatabaseHelpers;
import com.byteshaft.speedtrap.utils.EndPoints;
import com.byteshaft.speedtrap.utils.Helpers;
import com.byteshaft.speedtrap.utils.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class MapFragment extends Fragment implements View.OnClickListener {


    boolean markerBounce;
    private DatabaseHelpers mDatabaseHelpers;
    public static boolean isMapFragmentOpen;
    public static String mapLocationPoint;
    static ImageButton ibMapFragmentCurrentLocation;
    private static GoogleMap mMap = null;
    View baseViewMapFragment;
    Animation animMapInfoWindowIn;
    Animation animMapInfoWindowOut;
    ImageView ivMapFragmentInfoLogo;
    TextView tvMapFragmentInfoLayout;
    TextView tvMapFragmentHUDSpeedometer;
    ImageView ivMapFragmentHUDLights;
    LinearLayout llMapFragmentHUDInfo;
    LinearLayout llMapFragmentHUD;
    LinearLayout llMapFragmentHUDSpeedometer;
    LinearLayout llMapFragmentButtonsOverlay;
    int questionCount;
    boolean isMapCameraSetToCurrentLocationByTheUser;
    Marker markerNewTrap;
    ImageButton ibMapFragmentMapType;
    ImageButton ibMapFragmentSettings;
    ImageButton ibMapFragmentRegister;
    boolean simpleMapView = true;
    RelativeLayout rlMapFragmentInfoWindow;
    Animation animMapInfoLogoCompleteFading;
    String sRegisterTrapLocation;
    String sRegistrationType;
    final Runnable registerPermanentTrap = new Runnable() {
        public void run() {
            sRegistrationType = "0";
            sendTrapRegistrationRequest();
        }
    };
    final Runnable registerMobileTrap = new Runnable() {
        public void run() {
            sRegistrationType = "1";
            sendTrapRegistrationRequest();
        }
    };
    boolean isLocationAcquiredMessageShown;
    boolean isCameraAutoAnimating;
    BroadcastReceiver locationDataReceiver;
    private boolean cameraAnimatedToCurrentLocation;
    private LocationService mLocationService;
    final Runnable retryLocationAcquisition = new Runnable() {
        public void run() {
            if (Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
                mLocationService = new LocationService();
                getActivity().startService(new Intent(getActivity(), LocationService.class));
                Helpers.showSnackBar(getString(R.string.messageAcquiringCurrentLocation), Snackbar.LENGTH_LONG, Color.WHITE);
            }
        }
    };
    private boolean isLocationAcquired;
    private double latitude;
    private double longitude;

    private static void onTrapRegistrationSuccess(String message, String responseText) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.GREEN);
        try {
            JSONObject jsonObjectTrapRegistrationResponse = new JSONObject(responseText);
            String[] trapLocation = jsonObjectTrapRegistrationResponse.getString("location").split(",");
            MainActivity.mDatabaseHelpers.createNewEntry(jsonObjectTrapRegistrationResponse.getString("id"),
                    jsonObjectTrapRegistrationResponse.getString("trap_type"), trapLocation[0] + "," + trapLocation[1]);
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(trapLocation[0]), Double.parseDouble(trapLocation[1])))
                    .snippet(jsonObjectTrapRegistrationResponse.getString("id")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void onTrapRegistrationFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.RED);
    }

    public static String getTrapRegistrationString(String location, String trapType) {
        JSONObject json = new JSONObject();
        try {
            json.put("location", location);
            json.put("trap_type", trapType);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    private static void onTrapDeletionSuccess(String message, String responseText) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.GREEN);
        Log.i("trapDeletionSuccessful", "" + responseText);
        try {
            JSONObject jsonObjectTrapRegistrationResponse = new JSONObject(responseText);
            String[] trapLocation = jsonObjectTrapRegistrationResponse.getString("location").split(",");
            MainActivity.mDatabaseHelpers.createNewEntry(jsonObjectTrapRegistrationResponse.getString("id"),
                    jsonObjectTrapRegistrationResponse.getString("trap_type"), trapLocation[0] + "," + trapLocation[1]);
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(trapLocation[0]), Double.parseDouble(trapLocation[1])))
                    .snippet(jsonObjectTrapRegistrationResponse.getString("id")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void onTrapDeletionFailed(String message) {
        Helpers.showSnackBar(message, Snackbar.LENGTH_LONG, Color.RED);
    }

    public static String getTrapDeletionString(String id) {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewMapFragment = inflater.inflate(R.layout.fragment_map, container, false);

        rlMapFragmentInfoWindow = (RelativeLayout) baseViewMapFragment.findViewById(R.id.rl_map_info_window);
        rlMapFragmentInfoWindow.setOnClickListener(this);
        mDatabaseHelpers = new DatabaseHelpers(getActivity());

        llMapFragmentHUD = (LinearLayout) baseViewMapFragment.findViewById(R.id.ll_map_hud);
        llMapFragmentHUDSpeedometer = (LinearLayout) baseViewMapFragment.findViewById(R.id.ll_map_hud_speedometer);
        tvMapFragmentHUDSpeedometer = (TextView) baseViewMapFragment.findViewById(R.id.tv_map_hud_speedometer);
        llMapFragmentHUDInfo = (LinearLayout) baseViewMapFragment.findViewById(R.id.ll_map_hud_info);
        llMapFragmentButtonsOverlay = (LinearLayout) baseViewMapFragment.findViewById(R.id.ll_map_buttons_overlay);
        ivMapFragmentHUDLights = (ImageView) baseViewMapFragment.findViewById(R.id.iv_map_hud_lights);

        locationDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isLocationAcquired = Boolean.parseBoolean(intent.getStringExtra("location_acquired"));
                if (isLocationAcquired) {
                    latitude = Double.parseDouble(intent.getStringExtra("latitude"));
                    longitude = Double.parseDouble(intent.getStringExtra("longitude"));
                }
                updateMapUI(Boolean.parseBoolean(intent.getStringExtra("location_acquisition_failed_dialog")),
                        intent.getIntExtra("current_location_icon_resource_type", 0), intent.getIntExtra("travelling_speed", 0));
            }
        };

        animMapInfoLogoCompleteFading = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_text_complete_fading);
        ivMapFragmentInfoLogo = (ImageView) baseViewMapFragment.findViewById(R.id.iv_map_fragment_info_logo);
        tvMapFragmentInfoLayout = (TextView) baseViewMapFragment.findViewById(R.id.tv_map_fragment_info_layout);
        animMapInfoWindowIn = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_transition_fragment_slide_left_enter);
        animMapInfoWindowIn.setFillAfter(true);
        animMapInfoWindowOut = AnimationUtils.loadAnimation(MainActivity.getInstance(), R.anim.anim_transition_fragment_slide_right_exit);
        animMapInfoWindowOut.setFillAfter(true);
        ibMapFragmentMapType = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_type);
        ibMapFragmentMapType.setOnClickListener(this);
        ibMapFragmentCurrentLocation = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_current_location);
        ibMapFragmentCurrentLocation.setOnClickListener(this);
        ibMapFragmentSettings = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_settings);
        ibMapFragmentSettings.setOnClickListener(this);
        ibMapFragmentRegister = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_register);
        ibMapFragmentRegister.setOnClickListener(this);

        animMapInfoWindowIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivMapFragmentInfoLogo.startAnimation(animMapInfoLogoCompleteFading);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        animMapInfoWindowOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ivMapFragmentInfoLogo.clearAnimation();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlMapFragmentInfoWindow.setVisibility(View.GONE);
                rlMapFragmentInfoWindow.setOnClickListener(null);
                rlMapFragmentInfoWindow.setClickable(false);
                AppGlobals.setMapFirstRun(false);
                llMapFragmentHUD.setVisibility(View.VISIBLE);
                llMapFragmentButtonsOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
            mLocationService = new LocationService();
            getActivity().startService(new Intent(getActivity(), LocationService.class));
            Helpers.showSnackBar(getString(R.string.messageAcquiringCurrentLocation), Snackbar.LENGTH_LONG, Color.WHITE);
        }

        final SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_for_questionnaire);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24.7255553, 46.5423343), 5.0f));
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);

                if (AppGlobals.getUserType() == 2) {
                    mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(final LatLng latLng) {
                            sRegisterTrapLocation = latLng.latitude + "," + latLng.longitude;
                            showRegisterTrapPrompt(getString(R.string.messageRegisterTrapAtSpecifiedLocation));
                        }
                    });
                }

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        if (isMapCameraSetToCurrentLocationByTheUser && !isCameraAutoAnimating) {
                            ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location);
                            isMapCameraSetToCurrentLocationByTheUser = false;
                        }


                    }
                });
            }
        });

        setupUI();
        return baseViewMapFragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_map_current_location:
                if (mMap != null) {
                    if (isLocationAcquired) {
                        isCameraAutoAnimating = true;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                .target(new LatLng(latitude, longitude))
                                .zoom(14.5f)
                                .build()), new GoogleMap.CancelableCallback() {
                            @Override
                            public void onFinish() {
                                ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location_on);
                                isMapCameraSetToCurrentLocationByTheUser = true;
                                isCameraAutoAnimating = false;
                            }

                            @Override
                            public void onCancel() {
                                isCameraAutoAnimating = false;
                            }
                        });
                    } else {
                        Helpers.showSnackBar(getString(R.string.errorLocationNotAvailable), Snackbar.LENGTH_SHORT, Color.RED);
                    }
                } else {
                    Helpers.showSnackBar(getString(R.string.errorMapNotReady), Snackbar.LENGTH_SHORT, Color.RED);
                }
                break;
            case R.id.ib_map_type:
                if (simpleMapView) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    ibMapFragmentMapType.setBackgroundResource(R.drawable.selector_map_type_simple);
                    simpleMapView = false;
                } else {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    ibMapFragmentMapType.setBackgroundResource(R.drawable.selector_map_type_satellite);
                    simpleMapView = true;
                }
                break;
            case R.id.ib_map_settings:
                Helpers.loadFragment(MainActivity.fragmentManager, new SettingsFragment(), false, "SettingsFragment");
                break;
            case R.id.ib_map_register:
                if (isLocationAcquired) {
                    sRegisterTrapLocation = latitude + "," + longitude;
                    showRegisterTrapPrompt(getString(R.string.messageRegisterTrapAtCurrentLocation));
                } else {
                    Helpers.showSnackBar(getString(R.string.errorLocationNotAvailable), Snackbar.LENGTH_SHORT, Color.RED);
                }
                break;
            case R.id.rl_map_info_window:
                rlMapFragmentInfoWindow.startAnimation(animMapInfoWindowOut);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isMapFragmentOpen = false;
        LocalBroadcastManager.getInstance(MainActivity.getInstance()).unregisterReceiver(locationDataReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        isMapFragmentOpen = true;
//        if (!mLocationService.isBackgroundServiceRunning && Helpers.isDeviceReadyForLocationAcquisition(getActivity())) {
//            mLocationService = new LocationService();
//            getActivity().startService(new Intent(getActivity(), LocationService.class));
//            Helpers.showSnackBar(getString(R.string.messageAcquiringCurrentLocation), Snackbar.LENGTH_LONG, Color.WHITE);
//        }
        LocalBroadcastManager.getInstance(MainActivity.getInstance()).registerReceiver((locationDataReceiver),
                new IntentFilter("LocationData"));
    }

    private void setupUI() {

        if (AppGlobals.isMapFirstRun()) {
            llMapFragmentHUD.setVisibility(View.GONE);
            llMapFragmentButtonsOverlay.setVisibility(View.GONE);
        }

        if (AppGlobals.getUserType() == 2) {
            ibMapFragmentRegister.setVisibility(View.VISIBLE);
        } else {
            ibMapFragmentRegister.setVisibility(View.GONE);
        }

        if (AppGlobals.isMapFirstRun()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isMapFragmentOpen) {
                        rlMapFragmentInfoWindow.setAnimation(animMapInfoWindowIn);
                        rlMapFragmentInfoWindow.setVisibility(View.VISIBLE);
                        if (AppGlobals.getUserType() == 1) {
                            tvMapFragmentInfoLayout.setText(getString(R.string.messageMapInfoUser));
                        } else {
                            tvMapFragmentInfoLayout.setText(getString(R.string.messageMapInfoStaff));
                        }
                    }
                }
            }, 5000);
        }
    }

    private void showRegisterTrapPrompt(String message) {
        Helpers.AlertDialogWithPositiveNegativeFunctionsNeutralButton(getActivity(), getString(R.string.textRegisterTrap),
                message, getString(R.string.textMobileTrap), getString(R.string.textPermanentTrap), getString(R.string.buttonCancel),
                registerMobileTrap, registerPermanentTrap);
    }

    private void sendTrapRegistrationRequest() {
        HttpRequest request = new HttpRequest(MainActivity.getInstance());
        Helpers.showProgressDialog(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.messageSendingTrapRegistrationRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_CREATED:
                                onTrapRegistrationSuccess(MainActivity.getInstance().getString(R.string.messageTrapRegistrationSuccessful),
                                        request.getResponseText());
                                break;
                            default:
                                onTrapRegistrationFailed(MainActivity.getInstance().getString(R.string.messageTrapRegistrationFailed) + "\n" +
                                        request.getResponseText());
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onTrapRegistrationFailed(MainActivity.getInstance().getString(R.string.messageTrapRegistrationFailed));
            }
        });
        request.open("POST", EndPoints.TRAPS);
        request.setRequestHeader("Authorization", "Token " + AppGlobals.getToken());
        request.send(getTrapRegistrationString(sRegisterTrapLocation, sRegistrationType));
    }

    private void sendTrapDeletionRequest() {
        HttpRequest request = new HttpRequest(MainActivity.getInstance());
        Helpers.showProgressDialog(MainActivity.getInstance(), MainActivity.getInstance().getString(R.string.messageSendingTrapRegistrationRequest));
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        Helpers.dismissProgressDialog();
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                onTrapRegistrationSuccess(MainActivity.getInstance().getString(R.string.messageTrapDeletionSuccessful),
                                        request.getResponseText());
                                break;
                            default:
                                onTrapRegistrationFailed(MainActivity.getInstance().getString(R.string.messageTrapDeletionFailed) + "\n" +
                                        request.getResponseText());
                                break;
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, short error, Exception exception) {
                onTrapRegistrationFailed(MainActivity.getInstance().getString(R.string.messageTrapDeletionFailed));
            }
        });
        request.open("PUT", EndPoints.TRAPS);
        request.setRequestHeader("Authorization", "Token " + AppGlobals.getToken());
//        request.send(getTrapDeletionString());
    }

    private void updateMapUI(boolean showLocationAcquisitionFailedDialog, int currentLocationImageType, int travellingSpeed) {
        if (isMapFragmentOpen) {
            if (!isLocationAcquired) {
                if (llMapFragmentHUDInfo.getVisibility() == View.GONE) {
                    llMapFragmentHUDSpeedometer.setVisibility(View.GONE);
                    llMapFragmentHUDInfo.setVisibility(View.VISIBLE);
                    llMapFragmentHUDInfo.startAnimation(animMapInfoLogoCompleteFading);
                    ivMapFragmentHUDLights.setVisibility(View.GONE);
                }
                if (currentLocationImageType == 0) {
                    ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location_acquiring);
                } else {
                    ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location);
                }
                if (showLocationAcquisitionFailedDialog) {
                    Helpers.AlertDialogWithPositiveNegativeFunctions(MainActivity.getInstance(), null,
                            MainActivity.getInstance().getString(R.string.messageFailedToAcquireCurrentLocation), MainActivity.getInstance().getString(R.string.buttonRetry),
                            MainActivity.getInstance().getString(R.string.buttonExit), retryLocationAcquisition, Helpers.exitApp);
                    ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location);
                    llMapFragmentHUDInfo.clearAnimation();
                    llMapFragmentHUDInfo.setVisibility(View.GONE);
                }
            } else {
                if (!isLocationAcquiredMessageShown) {
                    Helpers.showSnackBar(getString(R.string.messageCurrentLocationAcquired), Snackbar.LENGTH_LONG, Color.GREEN);
                    ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location);
                    isLocationAcquiredMessageShown = true;
                    isCameraAutoAnimating = true;
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude))
                            .zoom(14.0f)
                            .build()), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location_on);
                            isMapCameraSetToCurrentLocationByTheUser = true;
                            isCameraAutoAnimating = false;
                            populateTrapMarkers(mDatabaseHelpers.getAllRecords());
                        }

                        @Override
                        public void onCancel() {
                            isCameraAutoAnimating = false;
                            populateTrapMarkers(mDatabaseHelpers.getAllRecords());
                        }
                    });
                }
                if (llMapFragmentHUDSpeedometer.getVisibility() == View.GONE) {
                    llMapFragmentHUDInfo.clearAnimation();
                    llMapFragmentHUDInfo.setVisibility(View.GONE);
                    llMapFragmentHUDSpeedometer.setVisibility(View.VISIBLE);
                    ivMapFragmentHUDLights.setVisibility(View.VISIBLE);
                }
                if (travellingSpeed > AppGlobals.getAlertSpeedLimit()) {
                    llMapFragmentHUD.setBackgroundResource(R.drawable.background_map_hud_over_limit);
                    tvMapFragmentHUDSpeedometer.setTextColor(Color.RED);
                } else {
                    llMapFragmentHUD.setBackgroundResource(R.drawable.background_map_hud);
                    tvMapFragmentHUDSpeedometer.setTextColor(Color.WHITE);
                }
                if (!isCameraAutoAnimating && isMapCameraSetToCurrentLocationByTheUser) {
                    isCameraAutoAnimating = true;
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(latitude, longitude))
                            .zoom(mMap.getCameraPosition().zoom)
                            .build()), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.selector_map_current_location_on);
                            isMapCameraSetToCurrentLocationByTheUser = true;
                            isCameraAutoAnimating = false;
                        }

                        @Override
                        public void onCancel() {
                            isCameraAutoAnimating = false;
                        }
                    });
                }
                tvMapFragmentHUDSpeedometer.setText(String.valueOf(travellingSpeed));
            }
        }
    }

    private void populateTrapMarkers(ArrayList<HashMap> trapsArrayList) {
        for (int i = 0; i < trapsArrayList.size(); i++) {
            String[] stringTrapLocationLatLngArray = trapsArrayList.get(i).get("location").toString().split(",");
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(stringTrapLocationLatLngArray[0]),
                    Double.parseDouble(stringTrapLocationLatLngArray[1])))
                    .icon(BitmapDescriptorFactory.fromResource(getAppropriateMapMarkerIconImageID(
                            Integer.parseInt(trapsArrayList.get(i).get("trap_type").toString()))))
                    .snippet(trapsArrayList.get(i).get("id").toString()));
        }
    }
    private int getAppropriateMapMarkerIconImageID(int trapType) {
        int id;
        if (trapType == 0) {
            id = R.drawable.ic_map_current_location_on;
        } else  {
            id = R.drawable.ic_map_current_location_off;
        }
        return id;
    }

    private void setMarkerBounce(final Marker marker) {
        final Handler handler = new Handler();
        final long startTime = SystemClock.uptimeMillis();
        final long duration = 650;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (markerBounce) {
                    long elapsed = SystemClock.uptimeMillis() - startTime;
                    float t = Math.max(interpolator.getInterpolation((float) elapsed/duration), 0);
                    marker.setAnchor(0.5f, 1.0f +  t);
                    handler.postDelayed(this, 12);
                } else {
                    handler.removeCallbacks(this);
                    marker.setAnchor(0.5f, 1.0f);
                }
            }
        });
    }

}