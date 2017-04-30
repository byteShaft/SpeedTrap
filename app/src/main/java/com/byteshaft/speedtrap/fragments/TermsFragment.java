package com.byteshaft.speedtrap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.byteshaft.speedtrap.R;

/**
 * Created by fi8er1 on 23/04/2017.
 */

public class TermsFragment extends Fragment {

    View baseViewTermsFragment;
    Button btnTermsBack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseViewTermsFragment = inflater.inflate(R.layout.fragment_terms, container, false);

        btnTermsBack = (Button) baseViewTermsFragment.findViewById(R.id.btn_terms_back);
        btnTermsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        return baseViewTermsFragment;
    }

}
