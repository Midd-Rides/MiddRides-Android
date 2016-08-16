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
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.firebase.MiddRidesMessagingService;
import com.middleendien.midd_rides.firebase.MiddRidesMessagingService.OnNotificationReceiveListener;
import com.middleendien.midd_rides.models.Stop;
import com.middleendien.midd_rides.models.User;
import com.middleendien.midd_rides.utils.HardwareUtil;
import com.middleendien.midd_rides.utils.NetworkUtil;
import com.middleendien.midd_rides.models.SpinnerAdapter;
import com.middleendien.midd_rides.utils.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.middleendien.midd_rides.utils.RequestUtil.*;

public class MainActivity extends AppCompatActivity implements OnNotificationReceiveListener {

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

    // location spinners
    private Spinner pickUpSpinner;
    private Stop selectedStop;
    private TextView vanArrivingText;
    private TextView vanArrivingLocation;

    private GifImageView mainImage;

    // to logout view a while after notification
    private Handler resetViewHandler;
    private Runnable resetViewRunnable;
//    private static final int RESET_TIMEOUT = 5 * 60000;         // 5 minutes
    private static final int RESET_TIMEOUT = 5000;              // for testing

    private List<Stop> stopList;
    private SpinnerAdapter spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // TODO: periodically check for verification status if not verified

        if (getIntent().getExtras() != null) {
            try {
                String arrivingAt = getIntent().getExtras().getString(getString(R.string.key_arriving_stop));
                showVanComingDialog(arrivingAt);
                Log.d(TAG, "Coming to " + arrivingAt);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Not from push");
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

        // don't accidentally logout when later requests are made
        if (resetViewHandler != null)
            resetViewHandler.removeCallbacks(resetViewRunnable);
    }

    private void initEvent() {
        backFirstPressed = System.currentTimeMillis() - 2000;

        spinnerAdapter = new SpinnerAdapter(this, android.R.layout.simple_list_item_activated_1, stopList);
        pickUpSpinner.setAdapter(spinnerAdapter);
        pickUpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStop = spinnerAdapter.getItem(position);
                vanArrivingLocation.setText(selectedStop.getName());
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
                        .putInt(getString(R.string.saved_spinner_position), position)
                        .apply();
                Log.d(TAG, "Spinner selected: " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pickUpSpinner.setSelection(0);
            }
        });

        spinnerAdapter.notifyDataSetChanged();

        // load saved spinner position if available
        pickUpSpinner.setSelection(PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.saved_spinner_position), 0), true);

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
//                            Log.d(TAG, "Email verified: " + currentUser.isVerified());
//                            showWarningDialog(
//                                    getString(R.string.dialog_title_email_verification),
//                                    getString(R.string.dialog_msg_not_verified),
//                                    getString(R.string.dialog_btn_dismiss));
//                            return;
                        } else if (warnIfDisconnected())
                            return;

                        if (getPendingRequest(MainActivity.this) != null) {
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
        User currentUser = UserUtil.getCurrentUser(this);
        final Stop pendingRequestStop = getPendingRequest(this);
        NetworkUtil.getInstance().cancelRequest(
                currentUser.getEmail(),
                currentUser.getPassword(),
                pendingRequestStop.getStopId(),
                this,
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            JSONObject body;
                            if (!response.isSuccessful()) {
                                body = new JSONObject(response.errorBody().string());
                                Toast.makeText(MainActivity.this, body.getString(getString(R.string.res_param_error)), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "Cancel unsuccessful - " + body.toString());
                            } else {
                                resetView();
                                putPendingRequest(null, MainActivity.this);
                                setNotified(false, MainActivity.this);
                                switch (flag) {
                                    case CANCEL_REQUEST_FLAG_MANUAL:
                                        showCancelDialog();
                                    case CANCEL_REQUEST_FLAG_TIMEOUT:
                                        showTimeoutDialog();
                                }
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }

                        FirebaseMessaging.getInstance().unsubscribeFromTopic(pendingRequestStop.getStopId());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this, getString(R.string.failed_to_talk_to_server), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showCancelDialog() {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_request_cancelled))
                .setConfirmText(getString(R.string.dialog_btn_dismiss))
                .show();
    }

    private void showTimeoutDialog() {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_request_timeout))
                .setContentText(getString(R.string.dialog_msg_have_you_caught_the_can))
                .setConfirmText(getString(R.string.dialog_btn_yes))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        // TODO:
                    }
                })
                .setCancelText(getString(R.string.dialog_btn_no))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        // TODO:
                    }
                }).show();
    }

    private void showRequestDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_request_confirm))
                .setContentText(getString(R.string.dialog_request_message) + " " + selectedStop.getName() + "?")
                .setConfirmText(getString(R.string.dialog_btn_yes))
                .setCancelText(getString(R.string.dialog_btn_cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(final SweetAlertDialog sweetAlertDialog) {
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
                                        try {
                                            JSONObject body;
                                            if (!response.isSuccessful()) {     // request failed
                                                body = new JSONObject(response.errorBody().string());
                                                Toast.makeText(MainActivity.this, body.getString(getString(R.string.res_param_error)), Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "Request unsuccessful - " + body.toString());
                                            } else {                            // request success
                                                putPendingRequest(selectedStop, MainActivity.this);

                                                // listen for van coming update
                                                String channelName = selectedStop.getStopId();
                                                FirebaseMessaging.getInstance().subscribeToTopic(channelName);

                                                setTitle(getString(R.string.title_activity_main_van_on_way));
                                                toggleCallButton(BUTTON_CANCEL_REQUEST);

                                                // for spinner position when re-entering
                                                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit()
                                                        .putInt(getString(R.string.saved_spinner_position), pickUpSpinner.getSelectedItemPosition())
                                                        .putString(getString(R.string.saved_stop_name), selectedStop.getName())
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
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        t.printStackTrace();
                                        Toast.makeText(MainActivity.this, getString(R.string.failed_to_talk_to_server), Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
                }).show();
    }

    private void showVanComingDialog(final String arrivingStop) {
        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.dialog_title_coming))
                .setContentText(getString(R.string.van_is_coming) + " " + arrivingStop)
                .setConfirmText(getString(R.string.dialog_btn_got_it))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        // Replace whitespaces and forward slashes in location name with hyphens
                        String channelName = selectedStop.getStopId();
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(channelName);

                        displayVanArrivingMessages();
                    }
                }).show();

        // logout the view after 5 minutes
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
                        User currentUser = UserUtil.getCurrentUser(MainActivity.this);
                        if (currentUser != null && getPendingRequest(MainActivity.this) != null
                                && hasBeenNotified()) {
                            // logged in && has pending request && notified
                            cancelCurrentRequest(CANCEL_REQUEST_FLAG_TIMEOUT);
                        }
                    }
                });
            }
        };
        resetViewHandler.postDelayed(resetViewRunnable, RESET_TIMEOUT);     // 5 minutes
        Log.d(TAG, "Reset countdown restarting... " + RESET_TIMEOUT / 1000 + " seconds left");
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

    /**
     * Displays info messages that say the van is heading to the requested stop.
     * Resets the view after 5 minutes.
     */
    private void displayVanArrivingMessages(){
        Log.d(TAG, "DisplayVanArrivingMessages");
        // Display messages informing the user that the van is coming
        vanArrivingLocation.setAlpha(1);
        vanArrivingText.setAlpha(1);

        Log.i(TAG, vanArrivingText.getText() + " " + vanArrivingLocation.getText());
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
        switch (item.getItemId()) {
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
     * Whether user has been notified for current request
     * @return              whether notified
     */
    private boolean hasBeenNotified() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.have_been_notified), false);
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
                Log.d(TAG, "Entering from SettingsActivity, 0x" + Integer.toHexString(resultCode).toUpperCase());
                if (resultCode == USER_LOGOUT_RESULT_CODE) {
                    UserUtil.logout(this);
                    cancelAnimation();
                    Intent toLoginActivity = new Intent(this, LoginActivity.class);
                    startActivity(toLoginActivity);
                    finish();
                }
                if (resultCode == USER_CANCEL_REQUEST_RESULT_CODE) {
                    setTitle(getString(R.string.title_activity_main_select_pickup_location));
                    cancelAnimation();
                    Log.d(TAG, "Cancelled");
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
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
        final User currentUser = UserUtil.getCurrentUser(MainActivity.this);

        // check if there is request pending
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (currentUser != null && getPendingRequest(this) != null) {      // yes
            if (hasBeenNotified()) {
                displayVanArrivingMessages();
            }
            showAnimation();
            toggleCallButton(BUTTON_CANCEL_REQUEST);
        } else {                                                          // no
            cancelAnimation();
            toggleCallButton(BUTTON_MAKE_REQUEST);
            mainImage.setImageResource(R.drawable.logo_with_background);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.screen_on), true).apply();

        // for push notification
        MiddRidesMessagingService.registerListener(this);

        // checking for request timeout
        if (hasBeenNotified() && getPendingRequest(this) != null) {
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
                                if (currentUser != null && getPendingRequest(MainActivity.this) != null && hasBeenNotified()) {
                                    // logged in && has pending request && notified
                                    cancelCurrentRequest(CANCEL_REQUEST_FLAG_TIMEOUT);
                                    showTimeoutDialog();
                                }
                            }
                        });
                    }
                };
                // keep counting down
                resetViewHandler.postDelayed(resetViewRunnable, RESET_TIMEOUT - (currentTime - receivedTime));
                Log.d(TAG, "Reset countdown restarting... " + (RESET_TIMEOUT - (currentTime - receivedTime)) / 1000 + " seconds left");
            } else {                            // something is wrong
                // sod off
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Pause");

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

    @Override
    public void onReceiveWithMainActivityActive(final String stopId) {
        Log.d(TAG, "Received Push while active");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showVanComingDialog(spinnerAdapter.getStopById(stopId).getName());
            }
        });

        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(this, alarm);
        ringtone.play();

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(800);
    }

    @Override
    public void onReceiveWithMainActivityDormant() {
        Log.d(TAG, "Received Push while dormant");
        killSelf();
    }

    /***
     * Change button text colour on touch
     */
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

}