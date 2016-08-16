package com.middleendien.midd_rides.firebase;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.activity.MainActivity;
import com.middleendien.midd_rides.models.Stop;
import com.middleendien.midd_rides.utils.NotificationUtil;
import com.middleendien.midd_rides.utils.RequestUtil;
import com.middleendien.midd_rides.utils.UserUtil;

import java.util.Map;

/**
 * Created by Peter on 8/15/16.
 *
 */

public class MiddRidesMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    private static final String TYPE_ARRIVE = "arrive";
    private static final String TYPE_ANNOUNCE = "announce";

    private static final int ARRIVING_NOTIFICATION_REQUEST_CODE = 0x51;

    private boolean isLoggedIn;
    private Stop pendingRequest;
    private boolean screenIsOn;

    private static OnNotificationReceiveListener mListener;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            Log.i(TAG, "Received Message From: " + remoteMessage.getFrom());
            if (remoteMessage.getNotification() != null)
                Log.i(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
            else
                Log.i(TAG, "Notification Message Body: " + null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Data: " + data.toString());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        isLoggedIn = UserUtil.getCurrentUser(this) != null;
        pendingRequest = RequestUtil.getPendingRequest(this);
        screenIsOn = sharedPreferences.getBoolean(getString(R.string.screen_on), false);

        String type = data.get("type");
        if (type.equals(TYPE_ARRIVE)) {
            if (isLoggedIn && pendingRequest != null
                    && data.get("stopId").equals(pendingRequest.getStopId())) {
                Log.d(TAG, "Arriving at my stop");

                // save received time for timeout
                sharedPreferences.edit()
                        .putLong(getString(R.string.push_receive_time), System.currentTimeMillis())
                        .putBoolean(getString(R.string.have_been_notified), true)
                        .apply();

                if (mListener != null && screenIsOn)
                    mListener.onReceiveWithMainActivityActive(pendingRequest.getStopId());
                else if (mListener != null)
                    mListener.onReceiveWithMainActivityDormant();

                showArrivingNotification();
            }
        } else if (type.equals(TYPE_ANNOUNCE)) {
            // TODO: much later actually, maybe version 2
        }
    }

    private void showArrivingNotification() {
        Intent toMainActivity = new Intent(this, MainActivity.class);
        toMainActivity.putExtra(getString(R.string.key_arriving_stop), pendingRequest.getName());
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                ARRIVING_NOTIFICATION_REQUEST_CODE,
                toMainActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationUtil.showNotification(
                pendingIntent,
                PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.saved_stop_name), getString(R.string.your_place)),
                this);
    }

    public static void registerListener(OnNotificationReceiveListener listener) {
        mListener = listener;
    }

    public interface OnNotificationReceiveListener {

        void onReceiveWithMainActivityActive(String stopId);

        void onReceiveWithMainActivityDormant();

    }
}
