package com.middleendien.middrides.utils;

import android.content.Context;
import android.util.Patterns;

import com.middleendien.middrides.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

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
 * Now takes care of logging in and registering
 *
 */
public class LoginAgent {

    private static LoginAgent loginAgent;
    private OnLoginListener loginListener;
    private OnRegisterListener registerListener;

    public static final int LOGIN       = 0x021;
    public static final int REGISTER    = 0x022;

    private Context context;

    private LoginAgent() { }

    public static LoginAgent getInstance(Context context){
        if (loginAgent == null)
            loginAgent = new LoginAgent();

        loginAgent.context = context;

        return loginAgent;
    }

    public static boolean isEmailValid(String email) {
        return email.length() > 15                                                      // length longer than 15
                && email.substring(email.length() - 15).equals("@middlebury.edu")       // ends with middlebury.edu
                && email.indexOf("@") == email.length() - 15                            // doesn't have other @ in it's email
                && Patterns.EMAIL_ADDRESS.matcher(email).matches();                     // Android's default check
    }

    public void loginInBackground(String email, String password) {
        ParseUser.logInInBackground(email, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                loginListener.onLoginComplete(e == null, e);
                if(e==null){
                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                    installation.put("user", ParseUser.getCurrentUser().getObjectId());
                    installation.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {

                            } else {

                            }
                        }
                    });
                }
            }
        });
    }

    public void registerInBackground(String email, String password) {
        ParseUser parseUser = new ParseUser();
        parseUser.setEmail(email);
        parseUser.setUsername(email);
        parseUser.setPassword(password);
        parseUser.put(context.getString(R.string.parse_user_is_dispatcher), false);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                registerListener.onRegisterComplete(e == null, e);
            }
        });
    }

    public void registerListener(int requestCode, Context context) {
        switch (requestCode) {
            case LOGIN:
                loginListener = (OnLoginListener) context;
                break;
            case REGISTER:
                registerListener = (OnRegisterListener) context;
                break;
        }
    }



    public interface OnLoginListener {

        void onLoginComplete (boolean loginSuccess, ParseException e);

    }

    public interface OnRegisterListener {

        void onRegisterComplete (boolean registerSuccess, ParseException e);

    }
}
