package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byteshaft.speedtrap.R;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class MapFragment extends Fragment {

    View baseViewMapFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewMapFragment = inflater.inflate(R.layout.fragment_map, container, false);

        return baseViewMapFragment;
    }

}
