package com.middleendien.midd_rides.models;

/**
 * Created by Nosagie on 10/23/15.
 * Modified to an extent where the previous line should be ignored, blur
 *
 * MiddRides Stop
 * Latitude and Longitude methods are used to find closest stop to the user
 */

public class Stop {

    private String mName;
    private String mStopId;

    public Stop(String name) {
        this(name, null);
    }

    public Stop(String name, String stopId){
        mName = name;
        mStopId = stopId;
    }

    public String getName() {
        return mName;
    }

    public Stop setName(String name) {
        mName = name;
        return this;
    }

    public String getStopId() {
        return mStopId;
    }

    public void setStopId(String stopId) {
        mStopId = stopId;
    }

    public String toString(){
        return mName;
    }
}
