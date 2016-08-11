package com.middleendien.midd_rides.utils;

import android.content.Context;

/**
 * Created by Peter on 11/17/15.
 * Used to sync all sorts of stuff between local storage and Parse
 * since parse uses async itself, we don't need to implement that
 * callback is OnSynchronizeListener
 * All classes that wish to sync data will implement this
 *
 * Singleton pattern
 *
 * Also, at this stage, we don't have a list of callbacks,
 * which can be done with a registerPushListener function
 */
public class Synchronizer {

    private static Synchronizer synchronizer;
    private Context context;                    // for editing SharedPreferences
    private OnSynchronizeListener callback;

    public static Synchronizer getInstance(Context context) {
        if (synchronizer == null) {
            synchronizer = new Synchronizer(context);
        }
        return synchronizer;
    }

    public Synchronizer (Context context) {
        this.context = context;
        this.callback = (OnSynchronizeListener) context;
    }

    @SuppressWarnings("all")
    public void getObject (String varName, String objectId, String className, final int requestCode) {

    }

    public void getListObjects (String className, final int requestCode) {

    }

    public void getListObjectsLocal (String className, final int requestCode) {

    }

    public void resetPassword (String email, final int requestCode) {

    }

    public void incrementFieldBy(String className, final String objectId, final String fieldName, final int increment) {

    }

    /***
     * Not implemented
     * @param className
     */
    public void refreshListObjects (String className) {

    }

    interface OnSynchronizeListener {

    }

}
