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

import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.middleendien.middrides.models.Location;
import com.middleendien.middrides.utils.Synchronizer;
import com.middleendien.middrides.utils.Synchronizer.OnSynchronizeListener;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import pl.droidsonroids.gif.GifImageView;

public class MainScreen extends AppCompatActivity implements OnSynchronizeListener {

    private Synchronizer synchronizer;

    private FloatingActionButton callService;

    /**
     * request code for query: CLASSNAME_VAR_NAME_REQUEST_CODE;
     */
    private static final int STATUS_SERVICE_RUNNING_REQUEST_CODE            = 0x001;
    private static final int STATUS_LOCATION_VERSION_REQUEST_CODE           = 0x002;
    private static final int LOCATION_GET_LASTEST_VERSION_REQUEST_CODE      = 0x003;

    private static final int LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE        = 0x011;

    private static final int NOTIFICATION_ID                                = 0x026;

    private static final int SETTINGS_SCREEN_REQUEST_CODE                   = 0x201;
    private static final int LOGIN_REQUEST_CODE                             = 0x202;

    private static final int LOGIN_CANCEL_RESULT_CODE                       = 0x301;

    private static final int USER_LOGOUT_RESULT_CODE                        = 0x102;
    private static final int USER_CANCEL_REQUEST_RESULT_CODE                = 0x103;

    // for double click exit
    private long backFirstPressed;

    private int serverVersion;

    // location spinners
    private Spinner pickUpSpinner;
    private Location selectedLocation;

    private GifImageView mainImage;
    private Handler handler;
    private Runnable runnable;

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


        //TODO: check all status: e-mail verified and so on

        initData();

        initView();

        initEvent();
    }

    private void initData() {
        locationList = new ArrayList<>();

        synchronizer = Synchronizer.getInstance(this);
        synchronizer.getListObjectsLocal(getString(R.string.parse_class_locaton), LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE);

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

        // define the floating action button
        callService = (FloatingActionButton) findViewById(R.id.fab);

        callService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser() == null) {                   // not logged in
                    Snackbar.make(view, R.string.not_logged_in, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;                     // do nothing
                } else {
                    Snackbar.make(view, ParseUser.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                //If user has already requested the van
                if (ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {
                    Snackbar.make(view, R.string.pending_request_error, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {                        //initialize Location Dialog
                    showRequestDialog();
                }
            }
        });

        mainImage = (GifImageView) findViewById(R.id.main_screen_image);

        // check if there is request pending
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.parse_user_pending_request), false)) {      // yes
            showAnimation();
            setTitle(getString(R.string.title_activity_main_van_on_way));
        } else {                                                          // no
            cancelAnimation();
            mainImage.setImageResource(R.drawable.logo_with_background);
            setTitle(getString(R.string.title_activity_main_select_pickup_location));
        }
    }

    private void initEvent() {
        backFirstPressed = System.currentTimeMillis() - 2000;

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, locationList);

        pickUpSpinner.setAdapter(spinnerAdapter);

        pickUpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = (Location) spinnerAdapter.getItem(position);
                Log.d("PickupSpinner", position + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                pickUpSpinner.setSelection(0);
            }
        });

        spinnerAdapter.notifyDataSetChanged();
    }

    private void showRequestDialog() {
        new Builder(this)
                .setTitle(getString(R.string.dialog_title_request_confirm))
                .setMessage(getString(R.string.dialog_request_msg) + " " + selectedLocation.getName() + "?")
                .setPositiveButton(getString(R.string.dialog_btn_yes), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // perform request
                        onLocationSelected(selectedLocation);
                        setTitle(getString(R.string.title_activity_main_van_on_way));

                        // for spinner position when re-entering
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainScreen.this).edit();
                        editor.putInt(getString(R.string.pref_request_spinner_position), pickUpSpinner.getSelectedItemPosition())
                                .apply();

                        showAnimation();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_btn_cancel), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // sorry to see you go punk
                    }
                })
                .create().show();
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

    public void cancelAnimation() {
        if (runnable != null)
            handler.removeCallbacks(runnable);
        // enable spinner
        pickUpSpinner.setEnabled(true);
        mainImage.setImageResource(R.drawable.logo_with_background);
    }

    private void showAnimation() {
        mainImage.setImageResource(R.drawable.animation_gif);
        // disable spinner
        pickUpSpinner.setEnabled(false);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mainImage.setImageResource(R.drawable.animation_gif);
                } catch (Exception e) {
                    mainImage.setImageResource(R.drawable.animation_gif);
                } finally {
                    handler.postDelayed(this, 4000);
                }
            }
        };
        handler.postDelayed(runnable, 4000);
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
                Intent toSettingsScreen = new Intent(MainScreen.this, SettingsScreen.class);
                startActivityForResult(toSettingsScreen, SETTINGS_SCREEN_REQUEST_CODE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateLocations() {
        synchronizer.getListObjects(getString(R.string.parse_class_locaton), LOCATION_GET_LASTEST_VERSION_REQUEST_CODE);
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
                // TODO: disable app if not running
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

                synchronizer.getListObjectsLocal(getString(R.string.parse_class_locaton), LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE);

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putInt(getString(R.string.parse_status_location_version), serverVersion)         // should be initialised by now
                        .apply();

                break;

            case LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE:           // update from local
                Log.d("MainScreen", "updateFromLocal");

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
                        pickUpSpinner.setSelection(sharedPreferences.getInt(getString(R.string.pref_request_spinner_position), 0), true);
                } else {
                    synchronizer.getListObjects(getString(R.string.parse_class_locaton), LOCATION_GET_LASTEST_VERSION_REQUEST_CODE);
                }
                break;
        }

        Log.d("MainScreen", "locationList is null: " + (locationList == null) + "");
        Log.d("MainScreen", "Adapter Count " + spinnerAdapter.getCount() + "");
    }

    @Override
    public void onResetPasswordComplete(boolean resetSuccess, int requestCode) {
        // do nothing
    }

    public void onLocationSelected(Location locationSelected) {

        Toast.makeText(getApplicationContext(), locationSelected.toString(), Toast.LENGTH_SHORT).show();

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
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_SCREEN_REQUEST_CODE:
                if (resultCode == USER_LOGOUT_RESULT_CODE) {
                    Intent toLoginScreen = new Intent(MainScreen.this, LoginScreen.class);
                    startActivityForResult(toLoginScreen, LOGIN_REQUEST_CODE);
                }
                if (resultCode == USER_CANCEL_REQUEST_RESULT_CODE) {
                    setTitle(getString(R.string.title_activity_main_select_pickup_location));
                    cancelAnimation();
                }
            case LOGIN_REQUEST_CODE:
                if (resultCode == LOGIN_CANCEL_RESULT_CODE) {
                    finish();
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }

                if (ParseUser.getCurrentUser() != null) {
                    // TODO: check for pending request
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long backSecondPressed = System.currentTimeMillis();
                if(backSecondPressed - backFirstPressed >= 2000){
                    Snackbar.make(callService, getResources().getString(R.string.press_again_exit), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .show();
                    backFirstPressed = backSecondPressed;
                    return true;
                }
                else {
                    finish();
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                    //TODO: do something with backstack
                    // problem is that if someone switches between MainScreen and LoginScreen
                    // there will be multiple copies of the activites
                    // need to check backstack before start the intent          - Peter
                }
        }

        return super.onKeyDown(keyCode, event);
    }














    // for debugging

    @Override
    protected void onStart() {
        Log.d("MainScreen", "Start");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("MainScreen", "Resume");
        super.onResume();
    }

    @Override
    protected void onRestart() {
        Log.d("MainScreen", "Restart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.d("MainScreen", "Pause");
        super.onPause();
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
