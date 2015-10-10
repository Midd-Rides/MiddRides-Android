package com.middleendien.middrides.utils;

import java.io.Serializable;

/**
 * Created by Peter on 10/1/15.
 */
public class LoginAgent implements Serializable {
    private boolean loggedIn;

    public LoginAgent(){
        loggedIn = false;
    }

    private boolean attemptLogin(String uname, String password) {


        return false;
    }

    private boolean isUserNameValid(String uname) {
        //TODO: Check UserName
        return false;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Password Checking Logic
        return false;
    }
}
