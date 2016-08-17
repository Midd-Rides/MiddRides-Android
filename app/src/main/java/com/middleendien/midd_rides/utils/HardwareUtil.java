package com.middleendien.midd_rides.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.List;

/**
 * Created by Peter on 8/11/16.
 *
 */

public class HardwareUtil {

    private static final String TAG = "HardwareUtil";

    /***
     * Check if network (Internet) is available
     * @param context       activity or application context
     * @return              whether network is available
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /***
     * Check if application is running
     * @param context       context
     * @return              whether running
     */
    @SuppressWarnings("unused")
    public static boolean isAppRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                Log.d(TAG, "Activity Running");
                return true;
            }
        }
        return false;
    }

}
