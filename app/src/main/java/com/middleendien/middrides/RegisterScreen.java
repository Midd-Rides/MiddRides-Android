package com.middleendien.middrides;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.middleendien.middrides.backup.LoginAgent;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * Created by Peter on 10/5/15.
 */
public class RegisterScreen extends AppCompatActivity {
    private EditText usernameBox;
    private EditText passwdBox;
    private Button regButton;

//    private LoginAgent loginAgent;

    private static final int REGISTER_SUCCESS_CODE = 0x101;
    private static final int REGISTER_FAILURE_CODE = 0x102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();

        initData();

        initEvent();
    }

    private void initView() {
        usernameBox = (EditText) findViewById(R.id.reg_username);
        passwdBox = (EditText) findViewById(R.id.reg_passwd);

        regButton = (Button) findViewById(R.id.reg_button);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // use sharedpreference to access the register error message if any
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                // check e-mail validity
                if(!LoginAgent.isEmailValid(usernameBox.getText().toString()) || !Patterns.EMAIL_ADDRESS.matcher(usernameBox.getText().toString()).matches()){
                    Toast.makeText(RegisterScreen.this, getResources().getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                ParseUser parseUser = new ParseUser();
                parseUser.setEmail(usernameBox.getText().toString());
                parseUser.setUsername(usernameBox.getText().toString());
                parseUser.setPassword(passwdBox.getText().toString());

                parseUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
//                            registerSuccess = true;
                            Toast.makeText(RegisterScreen.this, "Register Success", Toast.LENGTH_SHORT).show();   // This toast is temporary
                            setResult(REGISTER_SUCCESS_CODE);
                            finish();
                            Log.i("Register Success", "Register Success");
                        } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                            Toast.makeText(RegisterScreen.this, getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                            Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                            registerSuccess = false;
                        } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                            Toast.makeText(RegisterScreen.this, getResources().getString(R.string.inter_server_err), Toast.LENGTH_SHORT).show();
                            Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                            registerSuccess = false;
                        } else if (e.getCode() == ParseException.TIMEOUT) {
                            Toast.makeText(RegisterScreen.this, getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                            Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                            registerSuccess = false;
                        } else if (e.getCode() == 202) {
                            Toast.makeText(RegisterScreen.this, getResources().getString(R.string.user_exists), Toast.LENGTH_SHORT).show();
                            Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                            registerSuccess = false;
                        } else {
                            Log.i("Register Error", e.getCode() + " " + e.getMessage());
//                   e.printStackTrace();
                            Toast.makeText(RegisterScreen.this, getResources().getString(R.string.other_failure), Toast.LENGTH_SHORT).show();
//                            registerSuccess = false;
                        }
                    }
                });


                // I'll reuse the code below when I figure out how to do Async Tasks on Android
                // Point of information, it is much simpler on .NET platform, just saying

//                loginAgent = LoginAgent.getInstance(usernameBox.getText().toString(), passwdBox.getText().toString(), RegisterScreen.this);
//
//                boolean registerSuccess = loginAgent.attemptRegister(usernameBox.getText().toString(), passwdBox.getText().toString());
//                if(!registerSuccess){
//                    String errorMessage = sharedPreferences.getString(getResources().getString(R.string.reg_fail_msg),
//                            getResources().getString(R.string.other_failure));
//
////                    Toast.makeText(RegisterScreen.this, errorMessage, Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(RegisterScreen.this, "Register Success", Toast.LENGTH_SHORT).show();   // This toast is temporary
//                    setResult(REGISTER_SUCCESS_CODE);
//                    finish();
//                }
            }
        });
    }

    private void initData() {
        // possibly nothing
    }

    private void initEvent() {

    }


}
