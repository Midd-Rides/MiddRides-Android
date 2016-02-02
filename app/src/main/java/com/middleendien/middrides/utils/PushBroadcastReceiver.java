package com.middleendien.middrides.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.middleendien.middrides.MainScreen;
import com.middleendien.middrides.R;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Peter on 1/14/16.
 *
 * To receive push and decides whether to pop up notifications
 */
public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    boolean isLoggedIn;
    boolean requestPending;
    boolean screenIsOn;
    String pickUpLocation;
    String arrivingLocation;

    boolean killActivity;

    static OnPushNotificationListener callback;

    Notification notification = null;
    Ringtone ringtone = null;

    public static final int NOTIFICATION_ID = 123;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String jsonData = intent.getExtras().getString("com.parse.Data");
        Log.d("PushReceiver", jsonData);

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            arrivingLocation = jsonObject.getString("location");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        screenIsOn = false;
        killActivity = false;

        Log.d("PushReceiver", context.getPackageName());
        if (isRunning(context)) {
            Log.i("PushReceiver", "App running");
            if (sharedPreferences.getBoolean(context.getString(R.string.screen_on), false)) {
                // screen is on
                Log.i("PushReceiver", "Screen on");
                screenIsOn = true;
                killActivity = false;
            } else {
                Log.i("PushReceiver", "Screen off");
                screenIsOn = false;
                killActivity = true;
            }
        } else {
            Log.i("PushReceiver", "App not running");
        }

        isLoggedIn = ParseUser.getCurrentUser() != null;
        requestPending = sharedPreferences.getBoolean(context.getString(R.string.parse_user_pending_request), false);
        pickUpLocation = sharedPreferences.getString(context.getString(R.string.parse_request_pickup_location), "Nowhere");

        if (isLoggedIn && requestPending
                && pickUpLocation.equals(arrivingLocation)) {
            long receivedTime = Calendar.getInstance().getTimeInMillis();
            editor.putLong(context.getString(R.string.push_receive_time), receivedTime)
                    // so that reset view will be run
                    .putBoolean(context.getString(R.string.request_notified), true).apply();

            Log.d("PushReceiver", "Notified");

            if (screenIsOn) {
                callback.onReceivePushWhileScreenOn(arrivingLocation);
                return;
            }

            showNotificationWithIntent(context);            // MainScreen kills self

            if (killActivity)
                callback.onReceivePushWhileDormant();
        } else {
            Log.d("PushReceiver", "Van not for me");
            notification = null;
        }
    }

    @TargetApi(16)
    private void showNotificationWithIntent(Context context) {
        Intent toMainScreen = new Intent(context, MainScreen.class);
        toMainScreen.putExtra(context.getString(R.string.parse_request_arriving_location), arrivingLocation);
        toMainScreen.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, toMainScreen, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.van_is_coming) + " " + arrivingLocation)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= 21) {
            notification.defaults |= Notification.VISIBILITY_PUBLIC;
            notification.category = Notification.CATEGORY_ALARM;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Boolean isRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                Log.d("PushReceiver", "Activity Running");
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        ringtone.stop();
        Log.d("PushReceiver", "Push opened");
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        Log.d("PushReceiver", "Push dismissed");
        super.onPushDismiss(context, intent);
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

    public static void registerPushListener(Context context) {
        callback = (OnPushNotificationListener) context;
    }

    public interface OnPushNotificationListener {

        // will be called if activity is on

        void onReceivePushWhileScreenOn(String arrivingLocation);

        void onReceivePushWhileDormant();

    }
}
