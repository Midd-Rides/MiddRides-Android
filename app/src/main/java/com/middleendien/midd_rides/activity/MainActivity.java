package com.middleendien.midd_rides.activity;

////////////////////////////////////////////////////////////////////
//                            _ooOoo_                             //
//                           o8888888o                            //
//                           88" . "88                            //
//                           (| ^_^ |)                            //
//                           O\  =  /O                            //
//                        ____/`---'\____                         //
//                      .'  \\|     |//  `.                       //
//                     /  \\|||  :  |||//  \                      //
//                    /  _||||| -:- |||||-  \                     //
//                    |   | \\\  -  /// |   |                     //
//                    | \_|  ''\---/''  |   |                     //
//                    \  .-\__  `-`  ___/-. /                     //
//                  ___`. .'  /--.--\  `. . ___                   //
//                ."" '<  `.___\_<|>_/___.'  >'"".                //
//              | | :  `- \`.;`\ _ /`;.`/ - ` : | |               //
//              \  \ `-.   \_ __\ /__ _/   .-` /  /               //
//        ========`-.____`-.___\_____/___.-`____.-'========       //
//                             `=---='                            //
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^      //
//                    Buddha Keeps Bugs Away                      //
////////////////////////////////////////////////////////////////////

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.Stop;
import com.middleendien.midd_rides.models.User;
import com.middleendien.midd_rides.utils.HardwareUtil;
import com.middleendien.midd_rides.utils.NetworkUtil;
import com.middleendien.midd_rides.utils.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.middleendien.midd_rides.firebase.PushBroadcastReceiver.*;

public class MainActivity extends AppCompatActivity implements OnPushNotificationListener {

    private static final String TAG = "MainActivity";

    private Button callService;

    private static final int SETTINGS_SCREEN_REQUEST_CODE                   = 0x201;

    private static final int USER_LOGOUT_RESULT_CODE                        = 0x102;
    private static final int USER_CANCEL_REQUEST_RESULT_CODE                = 0x103;

    private static final int CANCEL_REQUEST_FLAG_MANUAL                     = 0x111;
    private static final int CANCEL_REQUEST_FLAG_TIMEOUT                    = 0x112;

    private static final int BUTTON_MAKE_REQUEST                            = 0x26;
    private static final int BUTTON_CANCEL_REQUEST                          = 0x09;

    // for double click exit
    private long backFirstPressed;

    private int serverVersion;

    // location spinners
    private Spinner pickUpSpinner;
    private Stop selectedStop;
    private TextView vanArrivingText;
    private TextView vanArrivingLocation;

    private GifImageView mainImage;

    // to periodically check email verification status
    private Handler checkEmailHandler;
    private Runnable checkEmailRunnable;
    private static final long CHECK_EMAIL_INTERVAL = 30000;

    // to reset view a while after notification
    private Handler resetViewHandler;
    private Runnable resetViewRunnable;
    private static final int RESET_TIMEOUT = 5 * 60000;      // 5 minutes
//    private static final int RESET_TIMEOUT = 20000;

    private List<Stop> stopList;
    ArrayAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Log.d("MainActivity", "Create");

        if (getIntent().getExtras() != null) {
            try {
                // TODO:
//                String arrivingAt = getIntent().getExtras().getCharSequence(getString(R.string.parse_request_arriving_location)).toString();
//                showVanComingDialog(arrivingAt);
//                Log.d("MainActivity", "Coming to " + arrivingAt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d("MainActivity", "Not from push");
        }

        initView();

        initData();

        initEvent();
    }

    private void initData() {
        stopList = getLocalStops();
        Log.i(TAG, "StopList: " + stopList.toString());
        syncStops();
    }

    private void initView() {
        pickUpSpinner = (Spinner) findViewById(R.id.pick_up_spinner);
        vanArrivingText = (TextView) findViewById(R.id.vanArrivingText);
        vanArrivingLocation = (TextView) findViewById(R.id.van_arriving_location);

        // make request button
        callService = (Button) findViewById(R.id.flat_button);

        mainImage = (GifImageView) findViewById(R.id.main_screen_image);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(null);
            getWindow().setReenterTransition(null);
        }
    }

    /**
     * Resets everything in the current view to its initial state
     */
    private void resetView(){
        Log.i("MainActivity", "Reset view");

        cancelAnimation();

        toggleCallButton(BUTTON_MAKE_REQUEST);
        vanArrivingText.setAlpha(0);
        vanArrivingLocation.setAlpha(0);

        // don't accidentally reset when later requests are made
        if (resetViewHandler != null)
            resetViewHandler.removeCallbacks(resetViewRunnable);
    }

    private void initEvent() {
        backFirstPressed = System.currentTimeMillis() - 2000;

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, stopList);
        pickUpSpinner.setAdapter(spinnerAdapter);
        pickUpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStop = (Stop) spinnerAdapter.getItem(position);
                vanArrivingLocation.setText(selectedStop.getName());
                Log.d("PickupSpinner", "Selected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pickUpSpinner.setSelection(0);
            }
        });

        spinnerAdapter.notifyDataSetChanged();

        toggleCallButton(BUTTON_MAKE_REQUEST);

        RequestOnTouchListener onTouchListener = new RequestOnTouchListener();

        callService.setOnTouchListener(onTouchListener);
    }

    private void toggleCallButton(int changeTo) {
        switch (changeTo) {
            case BUTTON_MAKE_REQUEST:
                callService.setText(getString(R.string.request_pick_up));

                callService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        User currentUser = UserUtil.getCurrentUser(MainActivity.this);
                        if (currentUser == null) {                   // not logged in
                            showWarningDialog(
                                    getString(R.string.dialog_msg_not_logged_in),
                                    null,
                                    getString(R.string.dialog_btn_dismiss));
                            return;                     // do nothing
                            // TODO: turn this back on after email verification is implemented
//                        } else if (!currentUser.isVerified()) {
//                            Log.d("MainActivity", "Email verified: " + currentUser.isVerified());
//                            showWarningDialog(
//                                    getString(R.string.dialog_title_email_verification),
//                                    getString(R.string.dialog_msg_not_verified),
//                                    getString(R.string.dialog_btn_dismiss));
//                            return;
                        } else if (warnIfDisconnected())
                            return;

                        if (getPendingRequest() != null) {
                            showWarningDialog(
                                    getString(R.string.dialog_msg_can_only_make_one_request),
                                    null,
                                    getString(R.string.dialog_btn_dismiss));
                        } else {
                            showRequestDialog();
                        }
                    }
                });
                break;

            case BUTTON_CANCEL_REQUEST:
                callService.setText(getString(R.string.cancel_request));

                callService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: check out if this still works
                        if(warnIfDisconnected())
                            return;

                        cancelCurrentRequest(CANCEL_REQUEST_FLAG_MANUAL);
                    }
                });
                break;
        }
    }


    /**
     * Displays a warning if there is no internet connection.
     * @return true if disconnected, false if connected
     */
    private boolean warnIfDisconnected(){
        if (!HardwareUtil.isNetworkAvailable(getApplicationContext())){
            showWarningDialog(
                    getString(R.string.no_internet_warning),
                    null,
                    getString(R.string.dialog_btn_dismiss));
            return true;
        }
        return false;
    }

    private void cancelCurrentRequest(final int flag) {
        // TODO: remember to unsubscribe
    }

    private void showCancelDialog() {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_request_cancelled))
                .setConfirmText(getString(R.string.dialog_btn_dismiss))
                .show();
    }

    private void showTimeoutDialog(String pickUpLocation, Date requestTime) {

    }

    private void showRequestDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_request_confirm))
                .setContentText(getString(R.string.dialog_request_message) + " " + selectedStop.getName() + "?")
                .setConfirmText(getString(R.string.dialog_btn_yes))
                .setCancelText(getString(R.string.dialog_btn_cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        // make request
                        User currentUser = UserUtil.getCurrentUser(MainActivity.this);
                        if (currentUser == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.dialog_msg_not_logged_in), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        NetworkUtil.getInstance().makeRequest(
                                currentUser.getEmail(),
                                currentUser.getPassword(),
                                selectedStop.getStopId(),
                                MainActivity.this,
                                new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        // TODO:
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        // TODO:
                                    }
                                }
                        );

                        // Replace whitespaces and forward slashes in location name with hyphens
                        String channelName = selectedStop.getStopId();
                        FirebaseMessaging.getInstance().subscribeToTopic(channelName);

                        setTitle(getString(R.string.title_activity_main_van_on_way));
                        toggleCallButton(BUTTON_CANCEL_REQUEST);

                        // for spinner position when re-entering
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                        editor.putInt(getString(R.string.request_spinner_position), pickUpSpinner.getSelectedItemPosition())
                                .apply();

                        // change alert type
                        sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        sweetAlertDialog.setConfirmText(getString(R.string.dialog_btn_dismiss))
                                .setTitleText(getString(R.string.dialog_title_request_success))
                                .setContentText(getString(R.string.dialog_msg_you_will_be_notified))
                                .showCancelButton(false)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        showAnimation();
                                        sweetAlertDialog.dismissWithAnimation();
                                    }
                                });
                    }
                }).show();
    }

    private void showVanComingDialog(String arrivingLocatoin) {
        final SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText(getString(R.string.dialog_title_coming));
        dialog.setContentText(getString(R.string.van_is_coming) + " " + arrivingLocatoin);
        dialog.setConfirmText(getString(R.string.i_got_it));
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
                // Replace whitespaces and forward slashes in location name with hyphens
                String channelName = selectedStop.getStopId();
                FirebaseMessaging.getInstance().unsubscribeFromTopic(channelName);

                displayVanArrivingMessages();
            }
        });

        dialog.show();


        // reset the view after 5 minutes
        // resetView needs to be run from the main thread because it modifies views created
        // from the main thread. If we try to edit it using a different thread it will throw errors.
        // in resetView(), check whether a request is pending and whether notified

        resetViewHandler = new Handler();

        resetViewRunnable = new Runnable() {
            @Override
            public void run(){
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        // TODO:
//                        if (ParseUser.getCurrentUser() != null &&
//                                ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request)) &&
//                                sharedPreferences.getBoolean(getString(R.string.request_notified), false)) {
//                            // logged in && has pending request && notified
//                            cancelCurrentRequest(CANCEL_REQUEST_FLAG_TIMEOUT);
//                        }
                    }
                });
            }
        };
        resetViewHandler.postDelayed(resetViewRunnable, RESET_TIMEOUT);     // 5 minutes
        Log.d("MainActivity", "Reset countdown restarting... " + RESET_TIMEOUT / 1000 + " seconds left");
    }

    private void showWarningDialog(String title, String contentText, String confirmText) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(contentText)
                .showContentText(contentText != null)
                .setConfirmText(confirmText)
                .show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void cancelAnimation() {
        // enable spinner
        pickUpSpinner.setEnabled(true);
        mainImage.setImageResource(R.drawable.logo_with_background);
        mainImage.setBackground(null);

        // in case is showing
        vanArrivingText.setAlpha(0);
        vanArrivingLocation.setAlpha(0);

        setTitle(getString(R.string.title_activity_main_select_pickup_location));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showAnimation() {
        try {
            GifDrawable newDrawable = new GifDrawable(getResources(), R.drawable.animation_gif);
            mainImage.setBackground(newDrawable);
            mainImage.setImageResource(0);
            newDrawable.start();
            setTitle(getString(R.string.title_activity_main_van_on_way));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // disable spinner
        pickUpSpinner.setEnabled(false);
    }

    @Override
    public void onReceivePushWhileScreenOn(String arrivingLocation) {
        Log.d("MainActivity", "Received Push while active");
        showVanComingDialog(arrivingLocation);

        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(this, alarm);
        ringtone.play();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(800);
    }

    @Override
    public void onReceivePushWhileDormant() {
        Log.d("MainActivity", "Received Push while dormant");
        killSelf();
    }

    /**
     * Displays info messages that say the van is heading to the requested stop.
     * Resets the view after 5 minutes.
     */
    private void displayVanArrivingMessages(){
        Log.d("MainActivity", "DisplayVanArrivingMessages");
        // Display messages informing the user that the van is coming
        vanArrivingLocation.setAlpha(1);
        vanArrivingText.setAlpha(1);

        Log.i("MainActivity", vanArrivingText.getText() + " " + vanArrivingLocation.getText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Action Bar items' click events
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                if (UserUtil.getCurrentUser(this) != null) {
                    Intent toSettingsScreen = new Intent(MainActivity.this, SettingsActivity.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        startActivityForResult(toSettingsScreen, SETTINGS_SCREEN_REQUEST_CODE,
                                ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                    } else {
                        startActivityForResult(toSettingsScreen, SETTINGS_SCREEN_REQUEST_CODE);
                    }
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /***
     * Pull stops from server if out of date
     */
    private void syncStops() {
        long lastUpdated = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(getString(R.string.last_updated), 0);
        NetworkUtil.getInstance().syncStops(lastUpdated, this, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        JSONObject body = new JSONObject(response.body().string());
                        if (body.getBoolean(getString(R.string.res_param_has_updates))) {
                            JSONArray stops = body.getJSONArray(getString(R.string.res_param_stops));

                            Log.d(TAG, stops.toString());

                            stopList.clear();
                            for (int i = 0; i < stops.length(); i++) {
                                String stopName = stops.getJSONObject(i).getString(getString(R.string.res_param_name));
                                String stopId = stops.getJSONObject(i).getString(getString(R.string.res_param_stop_id));
                                stopList.add(new Stop(stopName, stopId));
                            }
                            // save all updated stops to local storage
                            putLocalStops(stopList);
                            if (spinnerAdapter != null)
                                spinnerAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /***
     * Save all stops to {@link} SharedPreference
     * @param stopList      list of stops
     */
    private void putLocalStops(List<Stop> stopList) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.all_stops), new Gson().toJson(
                        stopList,
                        new TypeToken<ArrayList<Stop>>() {}.getType()))
                .putLong(getString(R.string.last_updated), System.currentTimeMillis())
                .apply();
    }

    /***
     * Get all stops from {@link} SharedPreference
     * @return              list of stops
     */
    private List<Stop> getLocalStops() {
        String allStopsString = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.all_stops), null);
        if (allStopsString == null)
            return new ArrayList<>();
        else
            return new Gson().fromJson(allStopsString, new TypeToken<ArrayList<Stop>>() {}.getType());
    }

    /***
     * Save a pending request to local storage
     * @param stop          stop that the request is for
     */
    private void putPendingRequest(Stop stop) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.pending_request), new Gson().toJson(stop, Stop.class))
                .apply();
    }

    /***
     * Get the pending request from local storage
     * @return
     */
    private @Nullable Stop getPendingRequest() {
        return new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pending_request), null), Stop.class);
    }

    private void killSelf() {
        Log.i("MainActivity", "I'm dead");
        finish();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_SCREEN_REQUEST_CODE:
                Log.d("MainActivity", "Entering from SettingsActivity, 0x" + Integer.toHexString(resultCode).toUpperCase());
                if (resultCode == USER_LOGOUT_RESULT_CODE) {
                    cancelAnimation();
                    Intent toLoginActivity = new Intent(this, LoginActivity.class);
                    startActivity(toLoginActivity);
                    finish();
                }
                if (resultCode == USER_CANCEL_REQUEST_RESULT_CODE) {
                    setTitle(getString(R.string.title_activity_main_select_pickup_location));
                    cancelAnimation();
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity", "onNewIntent");
//        Synchronizer.getInstance(this).getListObjectsLocal(getString(R.string.parse_class_location), LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE);
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long backSecondPressed = System.currentTimeMillis();
                if(backSecondPressed - backFirstPressed >= 2000){
                    Toast.makeText(MainActivity.this, getString(R.string.press_again_exit), Toast.LENGTH_SHORT).show();
                    backFirstPressed = backSecondPressed;
                    return true;
                }
                else {
                    finish();
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        Log.d("MainActivity", "Resume");

        // TODO:
//        LoginAgent.getInstance(this).registerListener(LoginAgent.LOGOUT, this);

        // if email not verified, periodically check for email verification status
        // TODO:
//        if (ParseUser.getCurrentUser() != null && !ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {
//            Log.i("MainActivity", "Handler started, checking email verification status...");
//
//            checkEmailHandler = new Handler();
//
//            checkEmailRunnable = new Runnable() {
//                @Override
//                public void run() {
//                    if (!ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {
//                        // email still not verified
//                        // TODO: re-check with server
//                        checkEmailHandler.postDelayed(this, CHECK_EMAIL_INTERVAL);
//                        Log.i("MainActivity", "Email still not verified " + (new Date()).toString());
//                    } else {
//                        // email verified now
//                        checkEmailHandler.removeCallbacks(this);
//                        showEmailVerifiedDialog();
//                        Log.i("MainActivity", "Finally verified email");
//                    }
//                }
//            };
//            checkEmailHandler.postDelayed(checkEmailRunnable, CHECK_EMAIL_INTERVAL);           // check every half minute
//        }

        // check if there is request pending
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO:
//        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {      // yes
//            if (sharedPreferences.getBoolean(getString(R.string.request_notified), false)) {
//                displayVanArrivingMessages();
//            }
//            showAnimation();
//            toggleCallButton(BUTTON_CANCEL_REQUEST);
//        } else {                                                          // no
//            cancelAnimation();
//            toggleCallButton(BUTTON_MAKE_REQUEST);
//            mainImage.setImageResource(R.drawable.logo_with_background);
//        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.screen_on), true).apply();

        // for push notification
        registerPushListener(this);

        // checking for request timeout
        if (sharedPreferences.getBoolean(getString(R.string.request_notified), false)) {
            // notified, now check when the user was notified
            long currentTime = Calendar.getInstance().getTimeInMillis();
            long receivedTime = sharedPreferences.getLong(getString(R.string.push_receive_time), currentTime);

            if (currentTime - receivedTime >= RESET_TIMEOUT) {      // past timeout time
                cancelCurrentRequest(CANCEL_REQUEST_FLAG_TIMEOUT);
            } else if (currentTime > receivedTime) {        // not past timeout yet
                // restart countdown
                resetViewHandler = new Handler();
                resetViewRunnable = new Runnable() {
                    @Override
                    public void run(){
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                                // TODO:
//                                if (ParseUser.getCurrentUser() != null &&
//                                        ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request)) &&
//                                        sharedPreferences.getBoolean(getString(R.string.request_notified), false)) {
//                                    // logged in && has pending request && notified
//                                    cancelCurrentRequest(CANCEL_REQUEST_FLAG_TIMEOUT);
//                                }
                            }
                        });
                    }
                };
                // keep counting down
                resetViewHandler.postDelayed(resetViewRunnable, RESET_TIMEOUT - (currentTime - receivedTime));
                Log.d("MainActivity", "Reset countdown restarting... " + (RESET_TIMEOUT - (currentTime - receivedTime)) / 1000 + " seconds left");
            } else {                            // something is wrong
                // sod off
            }
        }

        // TODO: will be beneficial to add another task to constantly check how many people are waiting at one station

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "Pause");

        if (checkEmailHandler != null) {
            checkEmailHandler.removeCallbacks(checkEmailRunnable);
            Log.i("MainActivity", "Handler stopped");
        }

        if (resetViewHandler != null)
            resetViewHandler.removeCallbacks(resetViewRunnable);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.screen_on), false).apply();

        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    class RequestOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    callService.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
                    break;
                case MotionEvent.ACTION_UP:
                    callService.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorWhite));
                    break;
            }

            return false;
        }
    }














    // for debugging

    @Override
    protected void onStart() {
        Log.d("MainActivity", "Start");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d("MainActivity", "Restart");
        super.onRestart();
    }

    @Override
    protected void onStop() {
        Log.d("MainActivity", "Stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("MainActivity", "Destroy");
        super.onDestroy();
    }
}
