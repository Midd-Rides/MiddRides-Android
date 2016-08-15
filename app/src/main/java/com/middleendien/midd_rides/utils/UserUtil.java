package com.middleendien.midd_rides.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Patterns;
import android.widget.Toast;

import com.google.gson.Gson;
import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.User;

/**
 * Created by Peter on 8/15/16.
 *
 */

public class UserUtil {

    /***
     * Get current user
     * @param context       context
     * @return              current user
     */
    public static @Nullable User getCurrentUser(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new Gson().fromJson(sharedPreferences.getString(context.getString(R.string.current_user), null), User.class);
    }

    /***
     * Save user to local
     * @param context       context
     * @param user          user
     */
    public static void setCurrentUser(Context context, User user) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.current_user), new Gson().toJson(user, User.class))
                .apply();
    }

    /***
     * Clean up local user
     * @param context       context
     */
    public static void reset(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.current_user), null)
                .apply();
    }

    /***
     * Check if user email is valid
     * @param email         email to check
     * @return              whether valid
     */
    public static boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
