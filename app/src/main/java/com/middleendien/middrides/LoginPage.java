package com.middleendien.middrides;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.middleendien.middrides.backup.LoginAgent;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A login screen that offers login via email/password.
 *
 * Note: This class is the entry point of the app
 *
 */
public class LoginPage extends AppCompatActivity{

    // UI
    private Button btnLogIn;
    private Button btnRegister;
    private Button btnSkipLogIn;

    private EditText usernameBox;
    private EditText passwdBox;

//    LoginAgent loginAgent;

    private static final int REGISTER_REQUEST_CODE = 0x001;

    private static final int REGISTER_SUCCESS_CODE = 0x101;
    private static final int REGISTER_FAILURE_CODE = 0x102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null){
            Intent toMainScreen = new Intent(LoginPage.this, MainScreen.class);
            startActivity(toMainScreen);
            finish();
        }

        initView();

        initData();

        initEvent();

        requestPerm();
    }

    private void initView() {
        btnLogIn = (Button) findViewById(R.id.login_button);
        btnRegister = (Button) findViewById(R.id.register_button);
        btnSkipLogIn = (Button) findViewById(R.id.skip_login_button);

        usernameBox = (EditText) findViewById(R.id.usernameBox);
        passwdBox = (EditText) findViewById(R.id.passwdBox);
    }

    private void initData() {
        // currently nothing
    }

    private void initEvent() {
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {               // login business is implemented with the LoginAgent class
                // check e-mail validity
                if(!LoginAgent.isEmailValid(usernameBox.getText().toString())){
                    Toast.makeText(LoginPage.this, getResources().getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                    return;
                }
                 ParseUser.logInInBackground(usernameBox.getText().toString(), passwdBox.getText().toString(), new LogInCallback() {
                     @Override
                     public void done(ParseUser user, ParseException e) {
                         if (user != null) {
                             Log.i("Login Success", "Login Success");
                             Toast.makeText(LoginPage.this, "Login Success", Toast.LENGTH_SHORT).show();
                             Intent toMainScreen = new Intent(LoginPage.this, MainScreen.class);
                             startActivity(toMainScreen);
                             finish();
                         } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                         } else if (e.getCode() == ParseException.ACCOUNT_ALREADY_LINKED) {
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.account_linked), Toast.LENGTH_SHORT).show();
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                         } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.inter_server_err), Toast.LENGTH_SHORT).show();
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                         } else if (e.getCode() == ParseException.TIMEOUT) {
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                         } else if (e.getCode() == ParseException.VALIDATION_ERROR) {
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                         } else if (e.getCode() == 101) {
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                         } else {
                             Log.i("Login Error", e.getCode() + " " + e.getMessage());
                             Toast.makeText(LoginPage.this, getResources().getString(R.string.other_failure), Toast.LENGTH_SHORT).show();
                         }
                     }
                 });



                // until I figure out how to do Async
                // Windows Phone is the best

//                loginAgent = LoginAgent.getInstance(usernameBox.getText().toString(), passwdBox.getText().toString(), LoginPage.this);
//
//                boolean loginSuccess = loginAgent.attemptLogin(usernameBox.getText().toString(), passwdBox.getText().toString());
//
//                if(!loginSuccess){
//                    String errorMessage = sharedPreferences.getString(getResources().getString(R.string.login_fail_msg),
//                            getResources().getString(R.string.other_failure));
//
//                    Toast.makeText(LoginPage.this, errorMessage, Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(LoginPage.this, "Login Success", Toast.LENGTH_SHORT).show();         // this toast is temporary
//                }
//
//
//
//
//                if(loginSuccess){
//                    Intent toMainScreen = new Intent(LoginPage.this, MainScreen.class);
//                    startActivity(toMainScreen);
//                    finish();
//                }
//                else {
//                    // do nothings
//                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                        // switch to register page
                Intent toRegisterScreen = new Intent(LoginPage.this, RegisterPage.class);
                startActivityForResult(toRegisterScreen, REGISTER_REQUEST_CODE);
            }
        });

        // This button will lead program to attempt to login
        btnSkipLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMainScreen = new Intent(LoginPage.this, MainScreen.class);
                toMainScreen.putExtra(getResources().getString(R.string.is_logged_in), false);
                startActivity(toMainScreen);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REGISTER_REQUEST_CODE:
                if(resultCode == REGISTER_SUCCESS_CODE){
                    Intent toMainScreen = new Intent(LoginPage.this, MainScreen.class);
                    startActivity(toMainScreen);
                    finish();
                }
                else {
                    // Register not successful, do nothing
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestPerm() {
        // TODO:
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // TODO: SDK 23 and above, I think we just need Internet permission, that should be all. (location adds a bunch of work)
    }
}

