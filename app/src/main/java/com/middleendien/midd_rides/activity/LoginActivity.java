package com.middleendien.midd_rides.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.User;
import com.middleendien.midd_rides.utils.HardwareUtil;
import com.middleendien.midd_rides.utils.NetworkUtil;
import com.middleendien.midd_rides.Privacy;
import com.middleendien.midd_rides.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Peter on some earlier time
 *
 * Activity where user logs himself in
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Button btnLogIn;
    private Button btnToRegister;
    private AutoCompleteTextView emailBox;
    private EditText passwordBox;

    private static final int REGISTER_REQUEST_CODE = 0x100;
    private static final int PERMISSION_INTERNET_REQUEST_CODE = 0x201;

    private SweetAlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        initData();

        initView();

        initEvent();
    }

    private void initData() {
        // TODO: what is this?
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean(getString(R.string.waiting_to_log_out), false).apply();
    }

    private void initView() {
        btnLogIn = (Button) findViewById(R.id.login_login);
        btnToRegister = (Button) findViewById(R.id.login_register);

        emailBox = (AutoCompleteTextView) findViewById(R.id.login_email);
        passwordBox = (EditText) findViewById(R.id.login_passwd);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setExitTransition(null);
            getWindow().setReenterTransition(null);
        }

        // adjust logo
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ImageView splashLogo = (ImageView) findViewById(R.id.login_app_logo);

        splashLogo.getLayoutParams().width = (int)(metrics.widthPixels * 0.5);
        splashLogo.getLayoutParams().height = (int)(metrics.heightPixels * 0.4);
    }

    private void initEvent() {
        emailBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0 && s.charAt(s.length() - 1) == '@') {             // ends with "@"
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(
                            LoginActivity.this,
                            android.R.layout.simple_dropdown_item_1line, new String[] { s + "middlebury.edu" });
                    emailBox.setAdapter(autoCompleteAdapter);
                } else if (s.toString().length() > 2 && !s.toString().contains("@")) {         // "sth"
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<>(
                            LoginActivity.this,
                            android.R.layout.simple_dropdown_item_1line, new String[] { s + "@middlebury.edu" });
                    emailBox.setAdapter(autoCompleteAdapter);
                } else if (s.toString().length() > 15 && s.toString().substring(s.length() - 15).equals("@middlebury.edu")) {
                    // completed format
                    emailBox.dismissDropDown();
                } else if (s.toString().length() == 0) {
                    // cleared everything or initial state, without @
                    emailBox.setAdapter(null);
                }               // else do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().endsWith("@middlebury.edu")) {
                    passwordBox.clearFocus();
                    passwordBox.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(passwordBox, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // login logic is implemented in NetworkUtil
                if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED) {
                    final String email = emailBox.getText().toString();
                    final String password = passwordBox.getText().toString();

                    // check e-mail validity
                    if (!UserUtil.isEmailValid(email)) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.incorrect_email_format), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!HardwareUtil.isNetworkAvailable(getApplicationContext())){
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet_warning), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setDialogShowing(true);
                    NetworkUtil.getInstance().login(email, Privacy.encodePassword(password), LoginActivity.this, new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            setDialogShowing(false);
                            try {
                                JSONObject body;
                                if (!response.isSuccessful()) {     // not successful
                                    body = new JSONObject(response.errorBody().string());
                                    Toast.makeText(LoginActivity.this, body.getString(getString(R.string.res_param_error)), Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, body.toString());
                                } else {                            // login success
                                    body = new JSONObject(response.body().string());
                                    // save to local storage
                                    UserUtil.setCurrentUser(LoginActivity.this, new User(
                                            email,
                                            Privacy.encodePassword(password),
                                            body.getJSONObject(getString(R.string.res_param_user)).getBoolean(getString(R.string.res_param_verified))));
                                    Intent toMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(toMainActivity);
                                    finish();
                                }
                            } catch (JSONException | IOException e) {
                                setDialogShowing(false);
                                e.printStackTrace();
                                Toast.makeText(LoginActivity.this, getString(R.string.unexpected_server_response), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(LoginActivity.this, getString(R.string.failed_to_talk_to_server), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {        // no internet permission
                    requestPermission(Manifest.permission.INTERNET, PERMISSION_INTERNET_REQUEST_CODE);
                }

                hideKeyboard();
            }
        });

        btnToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                        // go to register page
                Intent toRegisterActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    startActivityForResult(
                            toRegisterActivity,
                            REGISTER_REQUEST_CODE,
                            ActivityOptionsCompat.makeSceneTransitionAnimation(LoginActivity.this).toBundle());
                else
                    startActivityForResult(toRegisterActivity, REGISTER_REQUEST_CODE);
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

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
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
                    Toast.makeText(LoginActivity.this, getString(R.string.permission_internet_denied), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REGISTER_REQUEST_CODE:
                if (resultCode == RegisterActivity.REGISTER_SUCCESS_CODE) {
                    Intent toMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(toMainActivity);
                    finish();
                }
                return;
        }

        super.onActivityResult(requestCode, resultCode, data);
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

