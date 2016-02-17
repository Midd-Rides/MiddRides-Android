package com.middleendien.middrides.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.middleendien.middrides.MainScreen;
import com.middleendien.middrides.R;

/**
 * Created by Sherif on 1/24/2016.
 *
 */
public class MiddRidesUtils {

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
            Log.d("MiddRidesUtils", "Location GPS Provider unavailable");
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MiddRidesUtils", "Location Network Provider unavailable");
        }

        return GPSEnabled && networkEnabled;
    }

    /***
     * create and show dummy notification for debug purposes
     * @param context Activity or Application context
     */
    public static void showDummyNotification(Context context) {
        Intent toMainScreen = new Intent(context.getApplicationContext(), MainScreen.class);
        toMainScreen.putExtra(context.getString(R.string.parse_request_arriving_location), "Good God");
        toMainScreen.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, toMainScreen, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.van_is_coming) + " " + "E Lot")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if (Build.VERSION.SDK_INT >= 21) {
            notification.defaults |= Notification.VISIBILITY_PUBLIC;
            notification.category = Notification.CATEGORY_ALARM;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123, notification);
    }
}
