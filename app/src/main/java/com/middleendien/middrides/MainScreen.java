package com.middleendien.middrides;

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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.middleendien.middrides.models.Location;
import com.middleendien.middrides.utils.LoginAgent;
import com.middleendien.middrides.utils.LoginAgent.OnLogoutListener;
import com.middleendien.middrides.utils.PushBroadcastReceiver;
import com.middleendien.middrides.utils.Synchronizer;
import com.middleendien.middrides.utils.Synchronizer.OnSynchronizeListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import info.hoang8f.widget.FButton;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.middleendien.middrides.utils.PushBroadcastReceiver.*;

public class MainScreen extends AppCompatActivity implements OnSynchronizeListener,
        OnLogoutListener, OnPushNotificationListener {

    private Synchronizer synchronizer;

    private FButton callService;

    /**
     * request code for query: CLASSNAME_VAR_NAME_REQUEST_CODE;
     */
    private static final int STATUS_SERVICE_RUNNING_REQUEST_CODE            = 0x001;
    private static final int STATUS_LOCATION_VERSION_REQUEST_CODE           = 0x002;
    private static final int LOCATION_GET_LASTEST_VERSION_REQUEST_CODE      = 0x003;

    private static final int LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE        = 0x011;
    private static final int INCREMENT_FIELD_REQUEST_CODE                   = 0x100;
    private static final int USER_RESET_PASSWORD_REQUEST_CODE               = 0x101;

    private static final int NOTIFICATION_ID                                = 0x128;

    private static final int SETTINGS_SCREEN_REQUEST_CODE                   = 0x201;
    private static final int LOGIN_REQUEST_CODE                             = 0x202;

    private static final int LOGIN_CANCEL_RESULT_CODE                       = 0x301;

    private static final int USER_LOGOUT_RESULT_CODE                        = 0x102;
    private static final int USER_CANCEL_REQUEST_RESULT_CODE                = 0x103;

    private static final int BUTTON_MAKE_REQUEST                            = 0x26;
    private static final int BUTTON_CANCEL_REQUEST                          = 0x09;



    // for double click exit
    private long backFirstPressed;

    private int serverVersion;

    // location spinners
    private Spinner pickUpSpinner;
    private Location selectedLocation;

    private GifImageView mainImage;

    // to periodically check email verification status
    private Handler checkEmailHandler;
    private Runnable checkEmailRunnable;

    private List<Location> locationList;
    ArrayAdapter spinnerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Log.d("MainScreen", "Create");

        if (ParseUser.getCurrentUser() == null) {
            Intent toLoginScreen = new Intent(MainScreen.this, LoginScreen.class);
            startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE);
        }

        if (getIntent().getExtras() != null) {
            try {
                String arrivingAt = getIntent().getExtras().getCharSequence(getString(R.string.parse_request_arriving_location)).toString();
                showVanComingDialog(arrivingAt);
                Log.d("PushNotification", "Coming to " + arrivingAt);
            } catch (Exception e) {
                // forget it
            }
        } else {
            Log.d("PushNotification", "Not from push");
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);

        initView();

        initData();

        initEvent();
    }

    private void initData() {
        locationList = new ArrayList<>();

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

        // make request button
        callService = (FButton) findViewById(R.id.flat_button);

        mainImage = (GifImageView) findViewById(R.id.main_screen_image);
    }

    private void initEvent() {
        backFirstPressed = System.currentTimeMillis() - 2000;

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, locationList);

        pickUpSpinner.setAdapter(spinnerAdapter);

        pickUpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = (Location) spinnerAdapter.getItem(position);
                Log.d("PickupSpinner", "Selected: " + position + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pickUpSpinner.setSelection(0);
            }
        });

        spinnerAdapter.notifyDataSetChanged();

        toggleCallButton(BUTTON_MAKE_REQUEST);
    }

    private void toggleCallButton(int changeTo) {
        switch (changeTo) {
            case BUTTON_MAKE_REQUEST:
                callService.setText(getString(R.string.request_pick_up));
                callService.setButtonColor(ContextCompat.getColor(this, R.color.colorButtons));

                callService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (ParseUser.getCurrentUser() == null) {                   // not logged in
                            showWarningDialog(
                                    getString(R.string.not_logged_in),
                                    null,
                                    getString(R.string.dialog_btn_dismiss));
                            return;                     // do nothing
                        } else if (!ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {
                            Log.d("MainScreen", "Email verified: " + ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified)));
                            showWarningDialog(
                                    getString(R.string.not_logged_in),
                                    null,
                                    getString(R.string.dialog_btn_dismiss));
                            return;
                        }

                        // I guess we won't be needing this part of the code
                        // if user has already requested the van
                        if (ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {
                            showWarningDialog(
                                    getString(R.string.pending_request_error),
                                    null,
                                    getString(R.string.dialog_btn_dismiss));
                        } else {                        //initialize Location Dialog
                            showRequestDialog();
                        }
                    }
                });
                break;
            case BUTTON_CANCEL_REQUEST:
                callService.setText(getString(R.string.cancel_request));
                callService.setButtonColor(ContextCompat.getColor(this, R.color.colorAccent));

                callService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cancelCurrentRequest();
                    }
                });
                break;
        }
    }

    private void cancelCurrentRequest() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(getString(R.string.parse_class_request));          // class name
        parseQuery.getInBackground(ParseUser.getCurrentUser().getString(getString(R.string.parse_request_request_id)),
                new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject requestToBeDeleted, ParseException e) {
                        if (e == null) {
                            //Delete pending requests and set pending requests to false
                            requestToBeDeleted.deleteInBackground();
                            ParseUser.getCurrentUser().put(getString(R.string.parse_user_pending_request), false);
                            ParseUser.getCurrentUser().saveInBackground();

                            // I don't remember where else I use this preference
                            // but I'm too scared to take it out
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainScreen.this).edit();
                            editor.putBoolean(getString(R.string.parse_user_pending_request), false).apply();

                            Synchronizer.getInstance(MainScreen.this).getObject(
                                    null,
                                    requestToBeDeleted.getString(getString(R.string.parse_request_locationID)),
                                    getString(R.string.parse_class_location),
                                    INCREMENT_FIELD_REQUEST_CODE);

                            new SweetAlertDialog(MainScreen.this, SweetAlertDialog.NORMAL_TYPE)
                                    .setTitleText(getString(R.string.request_cancelled))
                                    .setConfirmText(getString(R.string.dialog_btn_dismiss))
                                    .show();

                            Log.i("SettingsFragment", "Request Cancelled");

                            toggleCallButton(BUTTON_MAKE_REQUEST);
                            cancelAnimation();
                        } else {
                            e.printStackTrace();
                            Toast.makeText(MainScreen.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showRequestDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.dialog_title_request_confirm))
                .setContentText(getString(R.string.dialog_request_message) + " " + selectedLocation.getName() + "?")
                .setConfirmText(getString(R.string.dialog_btn_yes))
                .setCancelText(getString(R.string.dialog_btn_cancel))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        // perform request
                        makeRequest(selectedLocation);
                        setTitle(getString(R.string.title_activity_main_van_on_way));
                        toggleCallButton(BUTTON_CANCEL_REQUEST);

                        // for spinner position when re-entering
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainScreen.this).edit();
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
        dialog.setTitleText(getString(R.string.van_is_coming) + " " + arrivingLocatoin);
        dialog.setConfirmText(getString(R.string.i_got_it));
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                dialog.dismissWithAnimation();
            }
        });


        dialog.show();
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
    public void onReceivePushWhileActive(String arrivingLocation) {
        showVanComingDialog(arrivingLocation);
        Log.d("MainScreen", "Received Push");
    }

    @Override
    public void onReceivePushWhileDormant() {
        killSelf();
        Log.d("MainScreen", "Received Push");
    }

    private void killSelf() {
        finish();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Action Bar items' click events
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                if (ParseUser.getCurrentUser() != null) {
                    Intent toSettingsScreen = new Intent(MainScreen.this, SettingsScreen.class);
                    startActivityForResult(toSettingsScreen, SETTINGS_SCREEN_REQUEST_CODE);
                }
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
    public void onGetObjectComplete(ParseObject object, int requestCode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        switch (requestCode) {

            case STATUS_SERVICE_RUNNING_REQUEST_CODE:
                editor.putBoolean(getString(R.string.parse_status_is_running),
                        object.getBoolean(getString(R.string.parse_status_is_running))).apply();
                Log.i("QueryInfo", "Service Running: " + object.getBoolean(getString(R.string.parse_status_is_running)));

                // disable app when MiddRides service is down
                if (!object.getBoolean(getString(R.string.parse_status_is_running))) {
                    callService.setEnabled(false);
                    setTitle(getString(R.string.title_activity_main_service_down));
                    cancelAnimation();
                } else {
                    callService.setEnabled(true);
                }

                break;

            case STATUS_LOCATION_VERSION_REQUEST_CODE:
                int localVersion = sharedPreferences.getInt(getString(R.string.parse_status_location_version), 0);
                serverVersion = object.getInt(getString(R.string.parse_status_location_version));
                if (serverVersion > localVersion) {
                    // server has newer version
                    Log.i("QueryInfo", "Location Update Available");
                    updateLocations();                      // pull from server
                } else {
                    Log.i("QueryInfo", "Location No Update");
                }
                break;

            case INCREMENT_FIELD_REQUEST_CODE:
                // this step seems redundant but I don't know how to make it shorter
                String pointer = object.getParseObject(getString(R.string.parse_location_status)).getObjectId();
                synchronizer.incrementFieldBy(
                        getString(R.string.parse_class_locationstatus),
                        pointer,
                        getString(R.string.parse_locationstatus_passengers_waiting),
                        sharedPreferences.getBoolean(getString(R.string.parse_user_pending_request), false) ? 1 : -1
                );
        }
    }

    @Override
    public void onGetListObjectsComplete(List<ParseObject> objectList, int requestCode) {
        Log.d("MainScreen", "onGetListObjectsComplete");
        switch (requestCode) {
            case LOCATION_GET_LASTEST_VERSION_REQUEST_CODE:         // update from server
                for (ParseObject obj : objectList) {
                    obj.pinInBackground();      // save locally
//                    Log.d("Updated Locations", obj.getDouble(getString(R.string.parse_location_lat)) + "");
                }

                synchronizer.getListObjectsLocal(getString(R.string.parse_class_location), LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putInt(getString(R.string.parse_status_location_version), serverVersion)         // should be initialised by now
                        .apply();

                break;

            case LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE:           // update from local
                Log.d("MainScreen", "Update From Local");

                if (objectList.size() > 1) {
                    locationList.clear();
                    for (ParseObject obj : objectList) {
                        locationList.add(new Location(obj.getString(getString(R.string.parse_location_name)),
                                obj.getDouble(getString(R.string.parse_location_lat)),
                                obj.getDouble(getString(R.string.parse_location_lng)),
                                obj.getObjectId()));
                    }

                    spinnerAdapter.notifyDataSetChanged();

                    // if has pending request, set spinner position accordingly
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainScreen.this);
                    if (sharedPreferences.getBoolean(getString(R.string.parse_user_pending_request), false))
                        pickUpSpinner.setSelection(sharedPreferences.getInt(getString(R.string.request_spinner_position), 0), true);
                } else {
                    synchronizer.getListObjects(getString(R.string.parse_class_location), LOCATION_GET_LASTEST_VERSION_REQUEST_CODE);
                }
                break;
        }

        Log.d("MainScreen", "locationList is null: " + (locationList == null));
        Log.d("MainScreen", "locationList Count" + locationList.size());
        Log.d("MainScreen", "Adapter Count " + spinnerAdapter.getCount());
    }

    @Override
    public void onResetPasswordComplete(boolean resetSuccess, int requestCode) {
        switch (requestCode) {
            case USER_RESET_PASSWORD_REQUEST_CODE:
                Toast.makeText(
                        MainScreen.this,
                        resetSuccess ? getString(R.string.reset_email_sent) : getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

    @Override
    public void onIncrementComplete() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.waiting_to_log_out), false)) {
            LoginAgent.getInstance(this).logOutInBackground();
        }
    }

    @Override
    public void onLogoutComplete() {
        Intent toLoginScreen = new Intent(MainScreen.this, LoginScreen.class);
        startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE);
    }

    public void makeRequest(final Location locationSelected) {

        Log.i("RequestMade", locationSelected.toString());

        final ParseObject parseUserRequest = new ParseObject(getString(R.string.parse_class_request));
        parseUserRequest.put(getString(R.string.parse_request_request_time), new Date());                       // time
        parseUserRequest.put(getString(R.string.parse_request_user_id),
                ParseUser.getCurrentUser().getObjectId());                                                      // userId
        parseUserRequest.put(getString(R.string.parse_request_user_email),
                ParseUser.getCurrentUser().get(getString(R.string.parse_user_email)));                          // email
        parseUserRequest.put(getString(R.string.parse_request_pickup_location), locationSelected.getName());    // origin
        parseUserRequest.put(getString(R.string.parse_request_locationID), locationSelected.getLocationId());

        // save to sharedPreference
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString(getString(R.string.parse_request_pickup_location), locationSelected.getName())
                .putBoolean(getString(R.string.parse_user_pending_request), true)
                .apply();

        parseUserRequest.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Update user entries when done
                    ParseUser setPendingRequestUser = ParseUser.getCurrentUser();
                    setPendingRequestUser.put(getString(R.string.parse_user_pending_request), true);
                    setPendingRequestUser.put(getString(R.string.parse_request_request_id), parseUserRequest.getObjectId());
                    setPendingRequestUser.saveInBackground();
                    synchronizer.getObject(
                            null,
                            locationSelected.getLocationId(),
                            getString(R.string.parse_class_location),
                            INCREMENT_FIELD_REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_SCREEN_REQUEST_CODE:
                Log.d("MainScreen", "Entering from SettingsScreen, 0x" + Integer.toHexString(resultCode).toUpperCase());
                if (resultCode == USER_LOGOUT_RESULT_CODE) {
                    cancelAnimation();
                    // do nothing because we will deal with the log out in the callback
                }
                if (resultCode == USER_CANCEL_REQUEST_RESULT_CODE) {
                    cancelAnimation();
                }
                return;
            case LOGIN_REQUEST_CODE:
                Log.d("MainScreen", "Entering from LoginScreen, 0x" + Integer.toHexString(resultCode).toUpperCase());
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
        Log.d("MainScreen", "onNewIntent");
        Synchronizer.getInstance(this).getListObjectsLocal(getString(R.string.parse_class_location), LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE);
        super.onNewIntent(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long backSecondPressed = System.currentTimeMillis();
                if(backSecondPressed - backFirstPressed >= 2000){
                    Toast.makeText(MainScreen.this, getString(R.string.press_again_exit), Toast.LENGTH_SHORT).show();
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
        Log.d("MainScreen", "Resume");

        LoginAgent.getInstance(this).registerListener(LoginAgent.LOGOUT, this);

        // if email not verified, periodically check for email verification status
        if (ParseUser.getCurrentUser() != null && !ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {
            Log.i("MainScreen", "Handler started, checking email verification status...");

            checkEmailHandler = new Handler();

            checkEmailRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {
                        // email still not verified
                        synchronizer.refreshObject(ParseUser.getCurrentUser());
                        checkEmailHandler.postDelayed(this, 1000);
                        Log.i("MainScreen", "Email still not verified " + (new Date()).toString());
                    } else {
                        // email verified now
                        checkEmailHandler.removeCallbacks(this);
                        showEmailVerifiedDialog();
                        Log.i("MainScreen", "Finally verified email");
                    }
                }
            };
            checkEmailHandler.postDelayed(checkEmailRunnable, 1000);           // check every minute
            // for testing purpose, we can change the 30000's here to 1000's just to see
        }

        // check if there is request pending
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.parse_user_pending_request), false)) {      // yes
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
        registerListener(this);

        // TODO: will be beneficial to add another task to constantly check how many people are waiting at one station

        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("MainScreen", "Pause");

        if (checkEmailHandler != null) {
            checkEmailHandler.removeCallbacks(checkEmailRunnable);
            Log.i("MainScreen", "Handler stopped");
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.screen_on), false).apply();

        super.onPause();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }












    // for debugging

    @Override
    protected void onStart() {
        Log.d("MainScreen", "Start");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d("MainScreen", "Restart");
        super.onRestart();
    }

    @Override
    protected void onStop() {
        Log.d("MainScreen", "Stop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("MainScreen", "Destroy");
        super.onDestroy();
    }
}
