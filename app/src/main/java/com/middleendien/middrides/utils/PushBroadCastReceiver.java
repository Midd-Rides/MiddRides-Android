package com.middleendien.middrides.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.middleendien.middrides.MainScreen;
import com.middleendien.middrides.R;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Peter on 1/14/16.
 *
 * To receive push and decides whether to pop up notifications
 */
public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    private Context context;

    boolean isLoggedIn;
    boolean requestPending;
    String pickUpLoaction;
    String arrivingLocation;

    Notification notification = null;

    private static final int NOTIFICATION_ID = 0x026;

    @TargetApi(16)
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        this.context = context;

        String jsonData = intent.getExtras().getString("com.parse.Data");
        Log.d("PushReceiver", jsonData);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            arrivingLocation = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        isLoggedIn = ParseUser.getCurrentUser() != null;
        requestPending = sharedPreferences.getBoolean(context.getString(R.string.parse_user_pending_request), false);
        pickUpLoaction = sharedPreferences.getString(context.getString(R.string.parse_request_pickup_location), "Nowhere");

        if (isLoggedIn && requestPending
                && pickUpLoaction.equals(arrivingLocation)) {
            Intent toMainScreen = new Intent(context, MainScreen.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(toMainScreen);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.van_is_coming) + " " + arrivingLocation)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(resultPendingIntent)
                    .build();
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        } else {
            Log.d("PushReceiver", "Van not for me");
            notification = null;
        }
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        // probably just do nothing
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return notification;
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.mipmap.ic_launcher;
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
    }

    @Override
    protected Class<? extends Activity> getActivity(Context context, Intent intent) {
        return MainScreen.class;
    }
}
