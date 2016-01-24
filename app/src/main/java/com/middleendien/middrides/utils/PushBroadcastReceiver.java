package com.middleendien.middrides.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

import com.middleendien.middrides.MainScreen;
import com.middleendien.middrides.R;
import com.middleendien.middrides.SplashScreen;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

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

    private static final int NOTIFICATION_ID = 0x128;

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.push_received), true).apply();

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

        Log.d("PushBroadcastReceiver", context.getPackageName());
        if (isRunning(context)) {
            System.out.println("RUNNING");
            callback.onReceivePushWhileActive(arrivingLocation);
            if (sharedPreferences.getBoolean(context.getString(R.string.screen_on), false)) {
                // screen is on
                System.out.println("SCREEN ON");
                screenIsOn = true;
                return;
            } else {
                System.out.println("SCREEN OFF");
                screenIsOn = false;
                killActivity = true;
            }
        }else{
            System.out.println("NOT RUNNING");
        }

        isLoggedIn = ParseUser.getCurrentUser() != null;
        requestPending = sharedPreferences.getBoolean(context.getString(R.string.parse_user_pending_request), false);
        pickUpLocation = sharedPreferences.getString(context.getString(R.string.parse_request_pickup_location), "Nowhere");

        if (isLoggedIn && requestPending
                && pickUpLocation.equals(arrivingLocation)) {
            if (screenIsOn) {
                callback.onReceivePushWhileActive(arrivingLocation);
                return;
            }

            showNotificationWithIntent(context);

            if (killActivity)
                callback.onReceivePushWhileDormant();
        } else {
            Log.d("PushReceiver", "Van not for me");
            notification = null;
        }
    }

    @TargetApi(16)
    private void showNotificationWithIntent(Context context) {
        Intent toMainScreen = new Intent(context, SplashScreen.class);
        toMainScreen.putExtra(context.getString(R.string.parse_request_arriving_location), arrivingLocation);
        toMainScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(toMainScreen);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Builder builder = new Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.van_is_coming) + " " + arrivingLocation)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);

        notification = builder.build();
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if (Build.VERSION.SDK_INT >= 21) {
            notification.defaults |= Notification.VISIBILITY_PUBLIC;
            notification.category = Notification.CATEGORY_ALARM;
        }

        // show notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }

    private Boolean isRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        for (RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) {
                Log.d("PushBroadcastReceiver", "Activity Running");
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        ringtone.stop();
        Log.d("PushBroadcastReceiver", "Push opened");
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        Log.d("PushBroadcastReceiver", "Push dismissed");
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

    public static void registerListener(Context context) {
        callback = (OnPushNotificationListener) context;
    }

    public interface OnPushNotificationListener {

        // will be called if activity is on

        void onReceivePushWhileActive(String arrivingLocation);

        void onReceivePushWhileDormant();

    }
}
