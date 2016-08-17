package com.middleendien.midd_rides.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;

import com.google.gson.Gson;
import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Peter on 8/15/16.
 *
 * Anything user related, done here
 */

public class UserUtil {

    private static final String TAG = "UserUtil";

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
    public static void logout(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.current_user), null)
                .putInt(context.getString(R.string.saved_spinner_position), 0)
                .putString(context.getString(R.string.pending_request), null)
                .putString(context.getString(R.string.saved_stop_name), null)
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

    /***
     * Synchronize local user with server for verification status
     */
    public static void syncCurrentUser(final Context context) {
        User currentUser = getCurrentUser(context);
        NetworkUtil.getInstance().syncUser(
                currentUser.getEmail(),
                currentUser.getPassword(),
                context,
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if (response.isSuccessful()) {
                                JSONObject body = new JSONObject(response.body().string());
                                boolean verified = body.getBoolean(context.getString(R.string.res_param_verified));
                                setCurrentUser(context,             // save to local storage
                                        getCurrentUser(context).setVerified(verified));
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        Log.d(TAG, "Sync user failed");
                    }
                });
    }
}
