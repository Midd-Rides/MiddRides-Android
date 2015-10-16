package com.middleendien.middrides.backup;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.middleendien.middrides.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by Peter on 10/1/15.
 * This class is currently useless now that we are using Parse,
 * but later if we are going to implement out own backend,
 * this would come in handy, so I'm keeping it
 * and changing all methods to static
 * as it is no longer representing a user but merely an interface between UI and Parse database
 *
 *
 * Lastest: this class is no longer in use until further notice
 *
 */
public class LoginAgent /*implements Serializable*/ {

    // formatted for local storage
    private static LoginAgent localUser;
    // for uploading to Parse
    private static ParseUser parseUser;

    private boolean registerSuccess;
    private boolean loginSuccess;
    private static Context context;                     // for accessing sharedPreference

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    private String passwd;

    protected LoginAgent(String email, String passwd, Context context){
        setEmail(email);
        setPasswd(passwd);
        this.context = context;
    }

    // use this method when you're certain that localUser has been initialised
    public static LoginAgent getInstance(){
        return localUser;
    }

    // use this when you haven't initialised a user or not sure about it (eg. logged out already)
    public static LoginAgent getInstance(String email, String passwd, Context context){
        if(localUser == null){
            localUser = new LoginAgent(email, passwd, context);          // if localUser is initialised
        }
        return localUser;
    }

    public boolean attemptLogin(String email, String passwd) {
        setEmail(email);
        setPasswd(passwd);
        loginSuccess = false;
//        Toast.makeText(context, getEmail() + " " + getPasswd(), Toast.LENGTH_SHORT).show();
        ParseUser.logInInBackground(getEmail(), getPasswd(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    parseUser = user;
                    loginSuccess = true;
                    Log.i("Login Success", "Login Success");
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    setLoginFailMessage(context.getResources().getString(R.string.connection_fail));
                    loginSuccess = false;
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
                } else if (e.getCode() == ParseException.ACCOUNT_ALREADY_LINKED) {
                    setLoginFailMessage(context.getResources().getString(R.string.account_linked));
                    loginSuccess = false;
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
                } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                    setLoginFailMessage(context.getResources().getString(R.string.inter_server_err));
                    loginSuccess = false;
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
                } else if (e.getCode() == ParseException.TIMEOUT) {
                    setLoginFailMessage(context.getResources().getString(R.string.time_out));
                    loginSuccess = false;
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
                } else if (e.getCode() == ParseException.VALIDATION_ERROR) {
                    setLoginFailMessage(context.getResources().getString(R.string.wrong_info));
                    loginSuccess = false;
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
                } else if (e.getCode() == 101) {
                    setLoginFailMessage(context.getResources().getString(R.string.wrong_info));
                    loginSuccess = false;
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
                } else {
                    Log.i("Login Error", e.getCode() + " " + e.getMessage());
//                    e.printStackTrace();
                    setLoginFailMessage(context.getResources().getString(R.string.other_failure));
                    loginSuccess = false;
                }
            }
        });

        if(loginSuccess) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean attemptRegister(String email, String passwd){
        setEmail(email);
        setPasswd(passwd);
        parseUser = new ParseUser();
        parseUser.setEmail(email);
        parseUser.setUsername(email);
        parseUser.setPassword(passwd);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    registerSuccess = true;
                    Log.i("Register Success", "Register Success");
                    Toast.makeText(context, "Reached 1" + registerSuccess, Toast.LENGTH_SHORT).show();
                } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                    setRegisterFailMessage(context.getResources().getString(R.string.connection_fail));
                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
                    registerSuccess = false;
                    Toast.makeText(context, "Reached 2", Toast.LENGTH_SHORT).show();
                } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                    setRegisterFailMessage(context.getResources().getString(R.string.inter_server_err));
                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
                    registerSuccess = false;
                    Toast.makeText(context, "Reached 3", Toast.LENGTH_SHORT).show();
                } else if (e.getCode() == ParseException.TIMEOUT) {
                    setRegisterFailMessage(context.getResources().getString(R.string.time_out));
                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
                    registerSuccess = false;
                    Toast.makeText(context, "Reached 4", Toast.LENGTH_SHORT).show();
                } else if (e.getCode() == 202) {
                    setRegisterFailMessage(context.getResources().getString(R.string.user_exists));
                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
                    registerSuccess = false;
                    Toast.makeText(context, "Reached 5", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                   e.printStackTrace();
                    setRegisterFailMessage(context.getResources().getString(R.string.other_failure));
                    registerSuccess = false;
                    Toast.makeText(context, "Reached 6", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        Toast.makeText(context, "Reached Here " + registerSuccess, Toast.LENGTH_SHORT).show();

        if(registerSuccess){
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isEmailValid(String email) {
        if(email.length() <= 15)
            return false;

        if(email.substring(email.length() - 15).equals("@middlebury.edu")
                && email.indexOf("@") == email.length() - 15){      // so that there is only one "@" in the e-mail address
            return true;
        }

        return false;
    }

    // static because I think this is a class level thing, feel free to change
    private static void updateLoginInfo (Context context){
        // use SharedPreferences to store the boolean value of whether logged in
        // NOTE: this is one-way street, you can only update the sharedpreferences, but not downloading from it!!!
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //TODO: change login status
        editor.putBoolean(context.getResources().getString(R.string.login_status), false)
                .apply();
    }

    private void setLoginFailMessage(String msg){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.login_fail_msg), msg)
                .apply();
    }

    private void setRegisterFailMessage(String msg){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getResources().getString(R.string.reg_fail_msg), msg)
                .apply();
    }
}
