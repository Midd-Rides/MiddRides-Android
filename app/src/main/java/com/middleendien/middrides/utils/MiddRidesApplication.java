package com.middleendien.middrides.utils;

import android.app.Application;
import android.content.SharedPreferences;

import com.middleendien.middrides.R;
import com.parse.Parse;
import com.parse.ParseBroadcastReceiver;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;

/**
 * Created by Peter on 10/15/15.
 * To avoid parse not being initialised upon quick log-back-in after terminating the app
 */
public class MiddRidesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Parse Environment
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "II5Qw9I5WQ5Ezo9mL8TdYj3mEoiSFcdt8GFMAgsm", "EIepTgb590NQw5DDu1EccT7YvprP2ovLesj1t3Nd");

        // for test purposes, will send a test object to Parse database
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();
    }
}
