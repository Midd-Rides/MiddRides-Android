package com.middleendien.middrides;

import android.app.ActionBar;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;

/**
 * A login screen that offers login via email/password.
 */
public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    private void attemptLogin() {

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

