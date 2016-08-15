package com.middleendien.midd_rides.models;

/**
 * Created by Peter on 8/11/16.
 * User Model
 */

public class User {

    private String mEmail;
    private String mPassword;
    private boolean mVerified;

    public User(String email, String encryptedPassword, boolean verified) {
        mEmail = email;
        mPassword = encryptedPassword;
        mVerified = verified;
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

    public boolean isVerified() {
        return mVerified;
    }

    public User setVerified(boolean verified) {
        mVerified = verified;
        return this;
    }

    @Override
    public String toString() {
        return mEmail;
    }
}
