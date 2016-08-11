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
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
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

import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.Stop;
import com.middleendien.midd_rides.utils.HardwareUtil;
import com.middleendien.midd_rides.network.Synchronizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.middleendien.midd_rides.network.PushBroadcastReceiver.*;

public class MainActivity extends AppCompatActivity implements OnPushNotificationListener {

    private Synchronizer synchronizer;

    private Button callService;

    /**
     * request code for query: CLASSNAME_VAR_NAME_REQUEST_CODE;
     */
    private static final int STATUS_SERVICE_RUNNING_REQUEST_CODE            = 0x001;
    private static final int STATUS_LOCATION_VERSION_REQUEST_CODE           = 0x002;
    private static final int LOCATION_GET_LASTEST_VERSION_REQUEST_CODE      = 0x003;

    private static final int LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE        = 0x011;
    private static final int INCREMENT_FIELD_REQUEST_CODE                   = 0x100;
    private static final int USER_RESET_PASSWORD_REQUEST_CODE               = 0x101;

    private static final int SETTINGS_SCREEN_REQUEST_CODE                   = 0x201;
    private static final int LOGIN_REQUEST_CODE                             = 0x202;

    private static final int LOGIN_CANCEL_RESULT_CODE                       = 0x301;

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

        // TODO:
//        if (ParseUser.getCurrentUser() == null) {
//            Intent toLoginScreen = new Intent(MainActivity.this, LoginActivity.class);
//            startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE);
//        }

        if (getIntent().getExtras() != null) {
            try {
                String arrivingAt = getIntent().getExtras().getCharSequence(getString(R.string.parse_request_arriving_location)).toString();
                showVanComingDialog(arrivingAt);
                Log.d("MainActivity", "Coming to " + arrivingAt);
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
        stopList = new ArrayList<>();

        synchronizer = Synchronizer.getInstance(this);
        synchronizer.getListObjectsLocal(getString(R.string.parse_class_location), LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE);

        /**
         * Deal with everything in callback
         */

        // check service running status
        // Status should be the only hardcoded query
        synchronizer.getObject(null, "Xn18IdIQJj", getString(R.string.parse_class_status), STATUS_SERVICE_RUNNING_REQUEST_CODE);

        // check location list version
        synchronizer.getObject(null, "Xn18IdIQJj", getString(R.string.parse_class_status), STATUS_LOCATION_VERSION_REQUEST_CODE);
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
                Log.d("PickupSpinner", "Selected: " + position + "");
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
//        mainImage.setOnTouchListener(onTouchListener);
    }

    private void toggleCallButton(int changeTo) {
        switch (changeTo) {
            case BUTTON_MAKE_REQUEST:
                callService.setText(getString(R.string.request_pick_up));

                callService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO:
//                        if (ParseUser.getCurrentUser() == null) {                   // not logged in
//                            showWarningDialog(
//                                    getString(R.string.not_logged_in),
//                                    null,
//                                    getString(R.string.dialog_btn_dismiss));
//                            return;                     // do nothing
//                        } else if (!ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {
//                            Log.d("MainActivity", "Email verified: " + ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified)));
//                            showWarningDialog(
//                                    getString(R.string.not_logged_in),
//                                    null,
//                                    getString(R.string.dialog_btn_dismiss));
//                            return;
//                        } else if (warnIfDisconnected())
//                            return;
//
//                        if (ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {
//                            showWarningDialog(
//                                    getString(R.string.pending_request_error),
//                                    null,
//                                    getString(R.string.dialog_btn_dismiss));
//                        } else {                        //initialize Stop Dialog
//                            showRequestDialog();
//                        }
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
                        // TODO:
//                        makeRequest(selectedStop);

                        // Replace whitespaces and forward slashes in location name with hyphens
                        String channelName = selectedStop.getName().replace('/', '-').replace(' ', '-');
                        // TODO: subscribe to fcm channel
//                        ParsePush.subscribeInBackground(channelName);

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
                String channelName = selectedStop.getName().replace('/', '-').replace(' ', '-');
                // TODO: unsubscribe to fcm channel
//                ParsePush.unsubscribeInBackground(channelName);

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

    private void showEmailVerifiedDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_congrats))
                .setContentText(getString(R.string.dialog_msg_email_verified))
                .setConfirmText(getString(R.string.dialog_btn_dismiss))
                .show();
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

    private void killSelf() {
        Log.i("MainActivity", "I'm dead");
        finish();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
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

    @SuppressWarnings("unused")
    private void bringSelfToFront() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (RunningTaskInfo task : tasks) {
            if (task.baseActivity.getPackageName().equalsIgnoreCase(getPackageName())) {
                activityManager.moveTaskToFront(task.id, 0);
                break;
            }
        }

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
                // TODO:
//                if (ParseUser.getCurrentUser() != null) {
//                    Intent toSettingsScreen = new Intent(MainActivity.this, SettingsActivity.class);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                        startActivityForResult(toSettingsScreen, SETTINGS_SCREEN_REQUEST_CODE,
//                                ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
//                    } else {
//                        startActivityForResult(toSettingsScreen, SETTINGS_SCREEN_REQUEST_CODE);
//                    }
//                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateLocations() {
        synchronizer.getListObjects(getString(R.string.parse_class_location), LOCATION_GET_LASTEST_VERSION_REQUEST_CODE);
        if (spinnerAdapter != null)
            spinnerAdapter.notifyDataSetChanged();
        Log.d("updateLocations()", "Called");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_SCREEN_REQUEST_CODE:
                Log.d("MainActivity", "Entering from SettingsActivity, 0x" + Integer.toHexString(resultCode).toUpperCase());
                if (resultCode == USER_LOGOUT_RESULT_CODE) {
                    cancelAnimation();
                    // do nothing because we will deal with the log out in the callback
                }
                if (resultCode == USER_CANCEL_REQUEST_RESULT_CODE) {
                    setTitle(getString(R.string.title_activity_main_select_pickup_location));
                    cancelAnimation();
                }
                return;
            case LOGIN_REQUEST_CODE:
                Log.d("MainActivity", "Entering from LoginActivity, 0x" + Integer.toHexString(resultCode).toUpperCase());
                if (resultCode == LOGIN_CANCEL_RESULT_CODE) {
                    finish();
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
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
