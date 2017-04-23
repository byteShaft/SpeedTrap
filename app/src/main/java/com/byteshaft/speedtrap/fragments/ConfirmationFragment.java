package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.byteshaft.speedtrap.R;

/**
 * Created by fi8er1 on 24/04/2017.
 */

public class ConfirmationFragment extends Fragment {

    View baseViewConfirmationFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewConfirmationFragment = inflater.inflate(R.layout.fragment_confirmation, container, false);

        return baseViewConfirmationFragment;
    }

}
