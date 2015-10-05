package com.middleendien.middrides.utils;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.middleendien.middrides.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
