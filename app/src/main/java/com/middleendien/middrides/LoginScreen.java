package com.middleendien.middrides;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.middleendien.middrides.utils.LoginAgent;
import com.middleendien.middrides.utils.LoginAgent.OnLoginListener;
import com.middleendien.middrides.utils.MiddRidesUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

import cn.pedant.SweetAlert.SweetAlertDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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

    private static final int REGISTER_SUCCESS_RESULT_CODE = 0x101;

    private static final int LOGIN_CANCEL_RESULT_CODE = 0x301;

    private static final int PERMISSION_INTERNET_REQUEST_CODE = 0x201;

    private SweetAlertDialog progressDialog;

    private LoginAgent loginAgent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        if(ParseUser.getCurrentUser() != null){
            Log.d("LoginScreen", "Already has user");
            Intent toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
            toMainScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            toMainScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, toMainScreen);
        }

        // adjust logo
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ImageView splashLogo = (ImageView) findViewById(R.id.login_app_logo);

        splashLogo.getLayoutParams().width = (int)(metrics.widthPixels * 0.5);
        splashLogo.getLayoutParams().height = (int)(metrics.heightPixels * 0.4);

        initData();

        initView();

        initEvent();
    }

    private void initData() {
        loginAgent = LoginAgent.getInstance(this);
        loginAgent.registerListener(LoginAgent.LOGIN, this);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.waiting_to_log_out), false).apply();
    }

    private void initView() {
        btnLogIn = (Button) findViewById(R.id.login_login);
        btnRegister = (Button) findViewById(R.id.login_register);

        usernameBox = (AutoCompleteTextView) findViewById(R.id.login_email);
        passwdBox = (EditText) findViewById(R.id.login_passwd);
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
                }               // else do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().endsWith("@middlebury.edu")) {
                    passwdBox.clearFocus();
                    passwdBox.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(passwdBox, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // login logic is implemented with the LoginAgent class
                if (ContextCompat.checkSelfPermission(LoginScreen.this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED) {
                    // check e-mail validity
                    if (!LoginAgent.isEmailValid(usernameBox.getText().toString())) {
                        Toast.makeText(LoginScreen.this, getResources().getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!MiddRidesUtils.isNetworkAvailable(getApplicationContext())){
                        Toast.makeText(LoginScreen.this, getResources().getString(R.string.no_internet_warning), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setDialogShowing(true);
                    loginAgent.loginInBackground(usernameBox.getText().toString(), passwdBox.getText().toString());
                } else {        // no internet permission
                    requestPermission(Manifest.permission.INTERNET, PERMISSION_INTERNET_REQUEST_CODE);
                }

                hideKeyboard();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                        // switch to register page
                if (ContextCompat.checkSelfPermission(LoginScreen.this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent toRegisterScreen = new Intent(LoginScreen.this, RegisterScreen.class);
                    startActivityForResult(toRegisterScreen, REGISTER_REQUEST_CODE);
                } else {        // no internet permission
                    requestPermission(Manifest.permission.INTERNET, PERMISSION_INTERNET_REQUEST_CODE);
                }
            }
        });
    }

    private void setDialogShowing(boolean showing) {
        if (showing) {
            progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText(getString(R.string.dialog_logging_in));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.showCancelButton(false);
            progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(this, R.color.colorAccent));
            progressDialog.show();
        } else {
            if (progressDialog.isShowing())
                progressDialog.dismissWithAnimation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REGISTER_REQUEST_CODE:
                if(resultCode == REGISTER_SUCCESS_RESULT_CODE){
                    Intent toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
                    toMainScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    toMainScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    NavUtils.navigateUpTo(this, toMainScreen);
                } else {
                    // Register not successful, do nothing
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // so you have a keyboard, so what?
        }
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_INTERNET_REQUEST_CODE:
                if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)   // not granted
                    Toast.makeText(LoginScreen.this, getString(R.string.permission_internet_denied), Toast.LENGTH_SHORT).show();
                break;
        }
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                setResult(LOGIN_CANCEL_RESULT_CODE);
                finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLoginComplete(boolean loginSuccess, ParseException e) {
        setDialogShowing(false);

        if (loginSuccess) {
            Log.i("Login Success", "Login Success");
            Toast.makeText(LoginScreen.this, "Login Success", Toast.LENGTH_SHORT).show();
            Intent toMainScreen = new Intent(LoginScreen.this, MainScreen.class);
            toMainScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            toMainScreen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, toMainScreen);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

