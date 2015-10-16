package com.middleendien.middrides.utils;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Peter on 10/15/15.
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Set up Parse Environment
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "II5Qw9I5WQ5Ezo9mL8TdYj3mEoiSFcdt8GFMAgsm", "EIepTgb590NQw5DDu1EccT7YvprP2ovLesj1t3Nd");

        // TODO: remove this when publishing
        // this would log out every time a session starts
        ParseUser.logOut();

        // for test purposes, will send a test object to Parse database
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();
    }
}
