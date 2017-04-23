package com.byteshaft.speedtrap;

import android.app.Service;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.byteshaft.speedtrap.fragments.WelcomeFragment;
import com.byteshaft.speedtrap.utils.Helpers;
import com.byteshaft.speedtrap.utils.SoftKeyboard;

public class MainActivity extends FragmentActivity {

    public static FragmentManager fragmentManager;
    public static SoftKeyboard mSoftKeyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Helpers.setStatusBarTranslucent(this);
        fragmentManager = getSupportFragmentManager();
        RelativeLayout rlMainLayout = (RelativeLayout) findViewById(R.id.rl_main_layout);
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        mSoftKeyboard = new SoftKeyboard(rlMainLayout, im);
        Helpers.loadFragment(fragmentManager, new WelcomeFragment(), "WelcomeFragment");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
