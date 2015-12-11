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
    private String objectId;
    private double latitude;
    private double longitude;

    public Location(String name, double latitude, double longitude ){
        this.name = name;
        this.latitude =  latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Location setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getName() {
        return name;
    }

    public Location setName(String name) {
        this.name = name;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public Location setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getObjectId() {
        return objectId;
    }

    public Location setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    public String toString(){
        return name;
    }
}
