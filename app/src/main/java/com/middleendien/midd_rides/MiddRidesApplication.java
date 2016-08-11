package com.middleendien.midd_rides;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.middleendien.midd_rides.R;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Peter on 10/15/15.
 * To avoid parse not being initialised upon quick log-back-in after terminating the app
 */
public class MiddRidesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        // The fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("TikalSans-Medium.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

    }
}
