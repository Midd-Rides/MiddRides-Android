package com.middleendien.middrides;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.middleendien.middrides.utils.LoginAgent;
import com.middleendien.middrides.utils.LoginAgent.OnLoginListener;
import com.parse.ParseException;
import com.parse.ParseUser;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 *
 * Note: This class is the entry point of the app
 *
 */
public class LoginScreen extends AppCompatActivity implements OnLoginListener {

    // UI
    private Button btnLogIn;
    private Button btnRegister;
    private AutoCompleteTextView usernameBox;
    private EditText passwdBox;

    private static final int REGISTER_REQUEST_CODE = 0x001;

    private static final int REGISTER_SUCCESS_CODE = 0x101;
    private static final int REGISTER_FAILURE_CODE = 0x102;

    private ProgressDialog progressDialog;

    private LoginAgent loginAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        if(ParseUser.getCurrentUser() != null){
            Intent toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
            startActivity(toMainScreen);
            finish();
        }

        initData();

        initView();

        initEvent();

        requestPermission();
    }

    private void initData() {
        loginAgent = LoginAgent.getInstance();
        loginAgent.registerListener(LoginAgent.LOGIN, this);
    }

    private void initView() {
        btnLogIn = (Button) findViewById(R.id.login_button);
        btnRegister = (Button) findViewById(R.id.register_button);

        usernameBox = (AutoCompleteTextView) findViewById(R.id.usernameBox);
        passwdBox = (EditText) findViewById(R.id.passwdBox);
    }

    private void initEvent() {
        usernameBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0 && s.charAt(s.length() - 1) == '@') {             // ends with "@"
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(
                            LoginScreen.this,
                            android.R.layout.simple_dropdown_item_1line, new String[] { s + "middlebury.edu" });
                    usernameBox.setAdapter(autoCompleteAdapter);
                } else if (s.toString().length() > 2 && !s.toString().contains("@")) {         // "sth"
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(
                            LoginScreen.this,
                            android.R.layout.simple_dropdown_item_1line, new String[] { s + "@middlebury.edu" });
                    usernameBox.setAdapter(autoCompleteAdapter);
                } else if (s.toString().length() > 15 && s.toString().substring(s.length() - 15).equals("@middlebury.edu")) {
                    // completed format
                    usernameBox.dismissDropDown();
                } else if (s.toString().length() == 0) {
                    // cleared everything or initial state, without @
                    usernameBox.setAdapter(null);
                }   // else do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {               // login business is implemented with the LoginAgent class
                // check e-mail validity
                if (!LoginAgent.isEmailValid(usernameBox.getText().toString())) {
                    Toast.makeText(LoginScreen.this, getResources().getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                setDialogShowing(true);
                loginAgent.loginInBackground(usernameBox.getText().toString(), passwdBox.getText().toString());

                hideKeyboard();
                // Windows Phone is the best
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                        // switch to register page
                Intent toRegisterScreen = new Intent(LoginScreen.this, RegisterScreen.class);
                startActivityForResult(toRegisterScreen, REGISTER_REQUEST_CODE);
            }
        });
    }

    private void setDialogShowing(boolean showing) {
        if (showing) {
            progressDialog = new ProgressDialog(LoginScreen.this);
            progressDialog.setMessage(getString(R.string.dialog_logging_in));
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REGISTER_REQUEST_CODE:
                if(resultCode == REGISTER_SUCCESS_CODE){
                    Intent toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
                    startActivity(toMainScreen);
                    finish();
                } else {
                    // Register not successful, do nothing
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void requestPermission() {
        // TODO:
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // TODO: SDK 23 and above, I think we just need Internet permission, that should be all. (location adds a bunch of work)
    }

    /**
     * Automatically hide keyboard if touches background
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideKeyboard();
        return super.onTouchEvent(event);
    }

    @Override
    public void onLoginComplete(boolean loginSuccess, ParseException e) {
        setDialogShowing(false);

        if (loginSuccess) {
            Log.i("Login Success", "Login Success");
            Toast.makeText(LoginScreen.this, "Login Success", Toast.LENGTH_SHORT).show();
            Intent toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
            startActivity(toMainScreen);

            if (ParseUser.getCurrentUser().getBoolean(getString(R.string.is_dispatcher))) {
                // TODO: go to dispatcher page (or begin that fragment or what)
            } else {
                // cancel pending status
                ParseUser currentUser = ParseUser.getCurrentUser();
                currentUser.put(getString(R.string.parse_user_pending_request), false);
                currentUser.saveInBackground();
                toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
                startActivity(toMainScreen);
            }
            finish();
        } else {            // login failure
            if (e.getCode() == ParseException.CONNECTION_FAILED) {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == ParseException.ACCOUNT_ALREADY_LINKED) {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.account_linked), Toast.LENGTH_SHORT).show();
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.inter_server_err), Toast.LENGTH_SHORT).show();
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == ParseException.TIMEOUT) {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == ParseException.VALIDATION_ERROR) {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == 101) {
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.wrong_info), Toast.LENGTH_SHORT).show();
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
            } else {
                Log.i("Login Error", e.getCode() + " " + e.getMessage());
                Toast.makeText(LoginScreen.this, getResources().getString(R.string.other_failure), Toast.LENGTH_SHORT).show();
            }
        }
    }
}

