package com.middleendien.midd_rides.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.Stop;

/**
 * Created by Peter on 8/16/16.
 *
 */

public class RequestUtil {

    /***
     * Get the pending request from local storage
     * @return              stop that current pending request is for
     */
    public @Nullable static Stop getPendingRequest(Context context) {
        return new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pending_request), null), Stop.class);
    }

    /***
     * Save a pending request to local storage
     * @param stop          stop that the request is for
     */
    public static void putPendingRequest(@Nullable Stop stop, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(context.getString(R.string.pending_request), new Gson().toJson(stop, Stop.class))
                .apply();
    }

}
