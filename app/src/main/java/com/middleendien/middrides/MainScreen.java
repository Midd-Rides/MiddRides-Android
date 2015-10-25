package com.middleendien.middrides;

///////////////////////////////////////////////////////////////////
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.middleendien.middrides.models.Location;
import com.middleendien.middrides.models.UserRequest;
import com.middleendien.middrides.utils.AnnouncementDialogFragment;
import com.middleendien.middrides.utils.LocationSelectDialogFragment;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainScreen extends AppCompatActivity implements LocationSelectDialogFragment.SelectLocationDialogListener{

    private AnnouncementDialogFragment announcementDialogFragment;

    // Constant strings for Parse Requests
    public static final String USER_REQUESTS_PARSE_OBJECT = "UserRequest";
    private static final String REQUEST_TIME__PARSE_OBJECT = "RequestTime";
    private static final String USER_ID_PARSE_OBJECT = "UserId";
    private static final String USER_EMAIL_PARSE_OBJECT = "UserEmail";
    private static final String USER_EMAIL_KEY_PARSE_OBJECT = "email";
    private static final String LOCATION_NAME__PARSE_OBJECT = "Location_Name";
    public static final String PENDING_USER_REQUEST_PARSE_KEY = "PendingRequest";
    public static final String PENDING_USER_REQUESTID_PARSE_KEY = "requestID";



    // for settings such as announcement, user name and login status and so on
    private SharedPreferences sharedPreferences;

    private FloatingActionButton callService;
//    private Button callService;

    private long backFirstPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        initEvent();
    }

    private void initView() {
        // temporary announcement

        // define the floating action button
        callService = (FloatingActionButton) findViewById(R.id.fab);
        //callService.setEnabled(false); //DISABLED UNTIL USER LOGS IN
//        callService = (Button) findViewById(R.id.fab);
        callService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ParseUser.getCurrentUser() == null) {
                    Snackbar.make(view, R.string.not_logged_in, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, ParseUser.getCurrentUser().getEmail(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                //If request pending, then show message
                if((boolean)ParseUser.getCurrentUser().get(PENDING_USER_REQUEST_PARSE_KEY) == true){
                    Snackbar.make(view, R.string.pending_request_error, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                }else { //choose location from dialog
                    showLocationDialog();
                }

            }
        });
    }

    private void showLocationDialog(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DialogFragment locationFragment =  new LocationSelectDialogFragment();
        locationFragment.show(fm, "Select Location");
    }

    private void initEvent() {
        backFirstPressed = System.currentTimeMillis() - 2000;

        // check for announcements
        if(hasAnnouncement()){
//            announcementDialogFragment = new AnnouncementDialogFragment();
//            announcementDialogFragment
//                    .show(getFragmentManager(), "Announcement");

            //TODO: Update Pending request field in parse user object when request is satisfied
        }
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

        switch (id){
            case R.id.action_settings:
                Intent intentSettings = new Intent(MainScreen.this, Settings.class);
                startActivity(intentSettings);
                return true;

            case R.id.action_login:
                if(ParseUser.getCurrentUser() == null){
                    Intent toLoginScreen = new Intent(MainScreen.this, LoginPage.class);
                    startActivity(toLoginScreen);
                }
                else {
                    Intent toUserScreen = new Intent(MainScreen.this, UserScreen.class);
                    startActivity(toUserScreen);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
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
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onLocationSelected(Location locationSelected){

        Toast.makeText(getApplicationContext(), locationSelected.toString(), Toast.LENGTH_SHORT).show();

        //Make new userRequest and send to Parse
        UserRequest newRequest = new UserRequest(ParseUser.getCurrentUser().getObjectId(),locationSelected.getName());

        final ParseObject parseUserRequest = new ParseObject(USER_REQUESTS_PARSE_OBJECT);
        parseUserRequest.put(REQUEST_TIME__PARSE_OBJECT,newRequest.getTimeOfRequest());
        parseUserRequest.put(USER_ID_PARSE_OBJECT,newRequest.getUserID());
        parseUserRequest.put(USER_EMAIL_PARSE_OBJECT,ParseUser.getCurrentUser().get(USER_EMAIL_KEY_PARSE_OBJECT));
        parseUserRequest.put(LOCATION_NAME__PARSE_OBJECT, locationSelected.getName());
        parseUserRequest.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    //Update user entries when done
                    ParseUser setPendingRequestUser = ParseUser.getCurrentUser();
                    setPendingRequestUser.put(PENDING_USER_REQUEST_PARSE_KEY,true);
                    setPendingRequestUser.put(PENDING_USER_REQUESTID_PARSE_KEY,parseUserRequest.getObjectId());
                    setPendingRequestUser.saveInBackground();
                }
            }
        });

    }
}
