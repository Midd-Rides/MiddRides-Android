package com.middleendien.middrides.models;

import java.io.Serializable;

/**
 * Created by Nosagie on 10/23/15.
 *
 * Represents a MiddRides Stop
 * Latitude and Longitude methods are used to find closest stop to the user
 */



public class Location implements Serializable{

    private String name;
    private double latitude;
    private double longitude;
    private String locationID;


    public Location(String name, double latitude, double longitude ){
        this.name = name;
        this.latitude =  latitude;
        this.longitude = longitude;
    }

    public String getLocationId() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public Location(String name, double latitude, double longitude, String locationID ){
        this.name = name;
        this.latitude =  latitude;
        this.longitude = longitude;

        this.locationID = locationID;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String toString(){
        return name;
    }
}
