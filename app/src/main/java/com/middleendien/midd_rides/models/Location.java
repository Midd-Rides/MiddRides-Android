package com.middleendien.midd_rides.models;

/**
 * Created by Nosagie on 10/23/15.
 *
 * Represents a MiddRides Stop
 * Latitude and Longitude methods are used to find closest stop to the user
 */

public class Location {

    private String name;
    private String locationID;
    private int passengersWaiting;


    public Location() {

    }

    public String getLocationId() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public Location(String name){
        this.name = name;
        passengersWaiting = 0;
    }

    public Location(String name, String locationID){
        this.name = name;
        this.locationID = locationID;
        passengersWaiting = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassengersWaiting(int value) {
        this.passengersWaiting = value;
    }

    public int getPassengersWaiting() {
        return passengersWaiting;
    }

    public String toString(){
        return name;
    }
}
