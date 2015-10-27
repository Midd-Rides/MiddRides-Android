package com.middleendien.middrides.models;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Nosagie on 10/23/15.
 *
 * Represents a user request for a van
 * Stores the time of the Request, the User's ID, and the Location's name
 */
public class UserRequest {

    Date timeOfRequest;
    String userID;
    String locationName;

    //Custom constructor takes location ID and user ID as arguments
    public UserRequest(String user, String location){

        timeOfRequest =  new Date();
        userID = user;
        locationName = location;
    }

    public Date getTimeOfRequest() {
        return timeOfRequest;
    }

    public String getUserID() {
        return userID;
    }


}

