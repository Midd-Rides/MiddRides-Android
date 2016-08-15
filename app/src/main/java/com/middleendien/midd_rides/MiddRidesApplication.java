package com.middleendien.midd_rides;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.middleendien.midd_rides.firebase.MiddRidesInstanceIdService;
import com.middleendien.midd_rides.firebase.MiddRidesMessagingService;

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

        // Subscribe to Firebase global channel
        FirebaseMessaging.getInstance().subscribeToTopic("global");

        // the fonts
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("TikalSans-Medium.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
