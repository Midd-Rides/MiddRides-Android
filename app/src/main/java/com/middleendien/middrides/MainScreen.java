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

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.middleendien.middrides.models.Location;
import com.middleendien.middrides.fragment.LocationSelectDialogFragment;
import com.middleendien.middrides.fragment.LocationSelectDialogFragment.SelectLocationDialogListener;
import com.middleendien.middrides.utils.Synchronizer;
import com.middleendien.middrides.utils.Synchronizer.OnSynchronizeListener;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

public class MainScreen extends AppCompatActivity implements SelectLocationDialogListener,
        OnSynchronizeListener {

    private Synchronizer synchronizer;

    private FloatingActionButton callService;

    /**
     * request code for query: CLASSNAME_VAR_NAME_REQUEST_CODE;
     */
    private static final int STATUS_SERVICE_RUNNING_REQUEST_CODE            = 0x001;
    private static final int STATUS_LOCATION_VERSION_REQUEST_CODE           = 0x002;
    private static final int LOCATION_GET_LASTEST_VERSION_REQUEST_CODE      = 0x003;

    private static final int LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE        = 0x011;

    // for double click exit
    private long backFirstPressed;

    // I only do this nasty thing because the dialog was not made with an id attached to it
    private int locationDialogFragmentId;
    private int serverVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainScreen", "Create");

        //TODO: check all status: e-mail verified and so on

        initData();

        initView();

        initEvent();
    }

    private void initData() {
        synchronizer = Synchronizer.getInstance(this);

        /**
         * Deal with everything in callback
         */

        // check service running status
        // Status should be the only hardcoded query
        synchronizer.getObject(null, "Xn18IdIQJj", getString(R.string.parse_class_status), STATUS_SERVICE_RUNNING_REQUEST_CODE);

        // check location list version
        synchronizer.getObject(null, "Xn18IdIQJj", getString(R.string.parse_class_status), STATUS_LOCATION_VERSION_REQUEST_CODE);

        // TODO: we should react to these results
        // - Peter
    }

    private void initView() {
        // temporary announcement

        // define the floating action button
        callService = (FloatingActionButton) findViewById(R.id.fab);

        callService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser() == null) {                   // not logged in
                    Snackbar.make(view, R.string.not_logged_in, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;             // do nothing
                } else {
                    Snackbar.make(view, ParseUser.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                //If user has already requested the van
                if (ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {
                    Snackbar.make(view, R.string.pending_request_error, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                } else { //initialize Location Dialog
                    showLocationDialog();
                }
            }
        });
    }

    private void initEvent() {
        backFirstPressed = System.currentTimeMillis() - 2000;
    }

    private void showLocationDialog(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DialogFragment locationFragment =  new LocationSelectDialogFragment();

        locationDialogFragmentId = locationFragment.getId();
        locationFragment.show(fm, "Select Location");
    }

    private boolean hasAnnouncement() {
        return true;
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
                Intent intentSettings = new Intent(MainScreen.this, SettingsScreen.class);
                startActivity(intentSettings);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateLocations() {
        synchronizer.getListObjects(getString(R.string.parse_class_locaton), LOCATION_GET_LASTEST_VERSION_REQUEST_CODE);
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
        switch (requestCode) {
            case LOCATION_GET_LASTEST_VERSION_REQUEST_CODE:         // update from server
                for (ParseObject obj : objectList) {
                    obj.pinInBackground();      // save locally
//                    Log.d("Updated Locations", obj.getDouble(getString(R.string.parse_location_lat)) + "");
                }
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putInt(getString(R.string.parse_status_location_version), serverVersion)         // should be initialised by now
                        .apply();

                break;

            case LOCATION_UPDATE_FROM_LOCAL_REQUEST_CODE:           // update from local
                LocationSelectDialogFragment fragment = (LocationSelectDialogFragment) getSupportFragmentManager().
                        findFragmentById(locationDialogFragmentId);

                if (fragment != null)
                    fragment.updateLocations(objectList);
                break;
        }
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


    private AlertDialog showDialog(String title, String msg, String btnPosTxt, String btnNegTxt,
                                   OnClickListener btnPosListener, OnClickListener btnNegListener) {

        Builder builder = new Builder(this);

        return builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(btnPosTxt, btnPosListener)
                .setNegativeButton(btnNegTxt, btnNegListener)
                .create();
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
