package com.middleendien.middrides.utils;

import android.content.Context;
import android.util.Patterns;

import com.parse.ParseUser;

/**
 * Created by Peter on 10/1/15.
 * This class is currently useless now that we are using Parse,
 * but later if we are going to implement out own backend,
 * this would come in handy, so I'm keeping it
 * and changing all methods to static
 * as it is no longer representing a user but merely an interface between UI and Parse database
 *
 * Legacy comments above, ignore        - Peter
 *
 */
public class LoginAgent /*implements Serializable*/ {

    // formatted for local storage
    private static LoginAgent loginAgent;

    private Context context;                     // for accessing sharedPreference

    private LoginAgent(Context context){
        this.context = context;
    }

    public static LoginAgent getInstance(Context context){
        if (loginAgent != null)
            loginAgent = new LoginAgent(context);

        return loginAgent;
    }

    public static boolean isEmailValid(String email) {
        if(email.length() <= 15)
            return false;

        return email.substring(email.length() - 15).equals("@middlebury.edu")       // ends with middlebury.edu
                && email.indexOf("@") == email.length() - 15                        // doesn't have other @ in it's email
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();                 // Android's default check
    }














    /**
     * Log in is already Async, below code might not be needed
     * TODO: delete when first version is released
     */


//    public boolean attemptLogin(String email, String passwd) {
//        setEmail(email);
//        setPasswd(passwd);
//        loginSuccess = false;
////        Toast.makeText(context, getEmail() + " " + getPasswd(), Toast.LENGTH_SHORT).show();
//        ParseUser.logInInBackground(getEmail(), getPasswd(), new LogInCallback() {
//            @Override
//            public void done(ParseUser user, ParseException e) {
//                if (user != null) {
//                    parseUser = user;
//                    loginSuccess = true;
//                    Log.i("Login Success", "Login Success");
//                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
//                    setLoginFailMessage(context.getResources().getString(R.string.connection_fail));
//                    loginSuccess = false;
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                } else if (e.getCode() == ParseException.ACCOUNT_ALREADY_LINKED) {
//                    setLoginFailMessage(context.getResources().getString(R.string.account_linked));
//                    loginSuccess = false;
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
//                    setLoginFailMessage(context.getResources().getString(R.string.inter_server_err));
//                    loginSuccess = false;
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                } else if (e.getCode() == ParseException.TIMEOUT) {
//                    setLoginFailMessage(context.getResources().getString(R.string.time_out));
//                    loginSuccess = false;
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                } else if (e.getCode() == ParseException.VALIDATION_ERROR) {
//                    setLoginFailMessage(context.getResources().getString(R.string.wrong_info));
//                    loginSuccess = false;
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                } else if (e.getCode() == 101) {
//                    setLoginFailMessage(context.getResources().getString(R.string.wrong_info));
//                    loginSuccess = false;
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                } else {
//                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
////                    e.printStackTrace();
//                    setLoginFailMessage(context.getResources().getString(R.string.other_failure));
//                    loginSuccess = false;
//                }
//            }
//        });
//
//        if(loginSuccess) {
//            return true;
//        }
//        else {
//            return false;
//        }
//    }

//    public boolean attemptRegister(String email, String passwd){
//        setEmail(email);
//        setPasswd(passwd);
//        parseUser = new ParseUser();
//        parseUser.setEmail(email);
//        parseUser.setUsername(email);
//        parseUser.setPassword(passwd);
//
//        parseUser.signUpInBackground(new SignUpCallback() {
//            @Override
//            public void done(ParseException e) {
//                if (e == null) {
//                    registerSuccess = true;
//                    Log.i("Register Success", "Register Success");
//                    Toast.makeText(context, "Reached 1" + registerSuccess, Toast.LENGTH_SHORT).show();
//                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
//                    setRegisterFailMessage(context.getResources().getString(R.string.connection_fail));
//                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                    registerSuccess = false;
//                    Toast.makeText(context, "Reached 2", Toast.LENGTH_SHORT).show();
//                } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
//                    setRegisterFailMessage(context.getResources().getString(R.string.inter_server_err));
//                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                    registerSuccess = false;
//                    Toast.makeText(context, "Reached 3", Toast.LENGTH_SHORT).show();
//                } else if (e.getCode() == ParseException.TIMEOUT) {
//                    setRegisterFailMessage(context.getResources().getString(R.string.time_out));
//                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                    registerSuccess = false;
//                    Toast.makeText(context, "Reached 4", Toast.LENGTH_SHORT).show();
//                } else if (e.getCode() == 202) {
//                    setRegisterFailMessage(context.getResources().getString(R.string.user_exists));
//                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                    registerSuccess = false;
//                    Toast.makeText(context, "Reached 5", Toast.LENGTH_SHORT).show();
//                } else {
//                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
////                   e.printStackTrace();
//                    setRegisterFailMessage(context.getResources().getString(R.string.other_failure));
//                    registerSuccess = false;
//                    Toast.makeText(context, "Reached 6", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
////        Toast.makeText(context, "Reached Here " + registerSuccess, Toast.LENGTH_SHORT).show();
//
//        if(registerSuccess){
//            return true;
//        }
//        else {
//            return false;
//        }
//    }

//    // static because I think this is a class level thing, feel free to change
//    private static void updateLoginInfo (Context context){
//        // use SharedPreferences to store the boolean value of whether logged in
//        // NOTE: this is one-way street, you can only update the sharedpreferences, but not downloading from it!!!
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        //TODO: change login status
//        editor.putBoolean(context.getResources().getString(R.string.login_status), false)
//                .apply();
//    }

//    private void setLoginFailMessage(String msg){
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(context.getResources().getString(R.string.login_fail_msg), msg)
//                .apply();
//    }
//
//    private void setRegisterFailMessage(String msg){
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(context.getResources().getString(R.string.reg_fail_msg), msg)
//                .apply();
//    }
}
