package com.middleendien.midd_rides.utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Peter on 8/11/16.
 *
 */

public class HardwareUtil {

    private static final String TAG = "HardwareUtil";

    /***
     * Check if location service is available
     * @param context Activity or Application context
     * @return whether location service is available
     */
    public static boolean isLocationAvailable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean GPSEnabled = false;
        boolean networkEnabled = false;

        try {
            GPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Location GPS Provider unavailable");
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Location Network Provider unavailable");
        }

        return GPSEnabled && networkEnabled;
    }

    /***
     * Check if network (Internet) is available
     * @param context Activity or Application context
     * @return whether network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
