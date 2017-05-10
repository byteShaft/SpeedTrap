package com.byteshaft.speedtrap.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.byteshaft.speedtrap.MainActivity;
import com.byteshaft.speedtrap.R;
import com.byteshaft.speedtrap.utils.Helpers;
import com.byteshaft.speedtrap.utils.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class MapFragment extends Fragment implements View.OnClickListener {

    View baseViewMapFragment;

    int questionCount;
    boolean bMapCameraOnCurrentLocation;
    public static boolean bIsMapFragmentOpen;
    public static String mapLocationPoint;
    Marker mapLocationPointMarker;
    ImageButton ibMapFragmentMapType;
    ImageButton ibMapFragmentCurrentLocation;
    ImageButton ibMapFragmentSettings;
    private boolean mapMarkerAdded;
    private boolean cameraAnimatedToCurrentLocation;
    boolean simpleMapView = true;
    private static GoogleMap mMap = null;
    private LocationService mLocationService;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewMapFragment = inflater.inflate(R.layout.fragment_map, container, false);

        ibMapFragmentMapType = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_type);
        ibMapFragmentMapType.setOnClickListener(this);
        ibMapFragmentCurrentLocation = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_current_location);
        ibMapFragmentCurrentLocation.setOnClickListener(this);
        ibMapFragmentSettings = (ImageButton) baseViewMapFragment.findViewById(R.id.ib_map_settings);
        ibMapFragmentSettings.setOnClickListener(this);

        mLocationService = new LocationService();
        getActivity().startService(new Intent(getActivity(), LocationService.class));

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_for_questionnaire);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(24.7255553,46.5423343), 5.0f));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(final LatLng latLng) {
//                        if (isSearchEditTextVisible) {
//                            setSearchBarVisibility(false);
//                        }
//                        if (!mapMarkerAdded) {
//                            double latitude = latLng.latitude;
//                            double longitude = latLng.longitude;
//                            mapLocationPoint = latitude + "," + longitude;
//                            mapLocationPointMarker = mMap.addMarker(new MarkerOptions().position(latLng));
//                            mapMarkerAdded = true;
//                            llQuestionnaireBottomOverlayThree.setVisibility(View.GONE);
//                            new Handler().postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    tvQuestionnaireBottomOverlayThree.setText("Location Marked - Continue");
//                                    llQuestionnaireBottomOverlayThree.setVisibility(View.VISIBLE);
//                                }
//                            }, 750);
//                            btnQuestionnaireFragmentNext.setEnabled(true);
//                            btnQuestionnaireFragmentNext.setAlpha(1.0f);
//                            btnQuestionnaireFragmentRemove.setEnabled(true);
//                            btnQuestionnaireFragmentRemove.setAlpha(1.0f);
//                        }
                    }
                });

                mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        if (bMapCameraOnCurrentLocation) {
                            ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.ic_map_current_location_off);
                            bMapCameraOnCurrentLocation = false;
                        }
                    }
                });
            }
        });

        return baseViewMapFragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_map_current_location:
                    if (mMap != null) {
                        if (mLocationService.bLocationAcquired) {
//                            LatLng mLatLng = new LatLng(mLocationService.mLocation.getLatitude(), mLocationService.mLocation.getLongitude());
//                            CameraPosition cameraPosition =
//                                    new CameraPosition.Builder()
//                                            .target(mLatLng)
//                                            .zoom(16.0f)
//                                            .build();
//                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                            ibMapFragmentCurrentLocation.setBackgroundResource(R.drawable.ic_map_current_location_on);
                            bMapCameraOnCurrentLocation = true;
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
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        bIsMapFragmentOpen = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        bIsMapFragmentOpen = true;
        Helpers.isDeviceReadyForLocationAcquisition(getActivity());
    }

}
