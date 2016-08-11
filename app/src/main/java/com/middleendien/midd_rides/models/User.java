package com.middleendien.midd_rides.models;

/**
 * Created by Peter on 8/11/16.
 * User Model
 */

public class User {

    private String mEmail;
    private String mPassword;

    public User(String email, String encryptedPassword) {
        mEmail = email;
        mPassword = encryptedPassword;
    }

    public String getEmail() {
        return mEmail;
    }

    public User setEmail(String email) {
        mEmail = email;
        return this;
    }

    public String getPassword() {
        return mPassword;
    }

    public User setPassword(String encryptedPassword) {
        mPassword = encryptedPassword;
        return this;
    }

    @Override
    public String toString() {
        return mEmail;
    }
}
