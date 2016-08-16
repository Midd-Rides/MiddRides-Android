package com.middleendien.midd_rides.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.media.RingtoneManager;
import android.os.Build;

import com.middleendien.midd_rides.R;

/**
 * Created by Peter on 8/11/16.
 *
 * Middleware for displaying notifications
 */

public class NotificationUtil {

    private static final int NOTIFICATION_ID = 0x123;

    public static void showNotification(PendingIntent pendingIntent, String stopName, Context context) {
        Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.van_is_coming) + " " + stopName)
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

}
