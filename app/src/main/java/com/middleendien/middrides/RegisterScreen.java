package com.middleendien.middrides;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.middleendien.middrides.utils.LoginAgent;
import com.middleendien.middrides.utils.LoginAgent.OnRegisterListener;
import com.middleendien.middrides.utils.MiddRidesUtils;
import com.parse.ParseException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Peter on 10/5/15.
 *
 */
public class RegisterScreen extends AppCompatActivity implements OnRegisterListener {
    private AutoCompleteTextView usernameBox;
    private EditText passwdBox;
    private EditText passwdConfirmBox;


    private Button registerButton;

    private static final int REGISTER_SUCCESS_CODE = 0x101;
    private static final int REGISTER_FAILURE_CODE = 0x102;

    private SweetAlertDialog progressDialog;

    private LoginAgent loginAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        initData();

        initView();

        initEvent();
    }

    private void initData() {
        // possibly nothing
        loginAgent = LoginAgent.getInstance(this);
        loginAgent.registerListener(LoginAgent.REGISTER, this);
    }

    private void initView() {
        usernameBox = (AutoCompleteTextView) findViewById(R.id.register_email);
        passwdBox = (EditText) findViewById(R.id.register_passwd);
        passwdConfirmBox = (EditText) findViewById(R.id.register_passwd_confirm);

        registerButton = (Button) findViewById(R.id.register_register);
    }

    private void initEvent() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check e-mail validity
                if (!LoginAgent.isEmailValid(usernameBox.getText().toString())) {
                    Toast.makeText(RegisterScreen.this, getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                // check password match
                if (!passwdBox.getText().toString().equals(passwdConfirmBox.getText().toString())) {
                    Toast.makeText(RegisterScreen.this, getString(R.string.passwd_not_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!MiddRidesUtils.isNetworkAvailable(getApplicationContext())){
                    Toast.makeText(RegisterScreen.this, getResources().getString(R.string.no_internet_warning), Toast.LENGTH_SHORT).show();
                    return;
                }

                setDialogShowing(true);
                loginAgent.registerInBackground(usernameBox.getText().toString(), passwdBox.getText().toString());

                hideKeyboard();
            }
        });

        usernameBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0 && s.charAt(s.length() - 1) == '@') {             // ends with "@"
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(
                            RegisterScreen.this,
                            android.R.layout.simple_dropdown_item_1line, new String[]{s + "middlebury.edu"});
                    usernameBox.setAdapter(autoCompleteAdapter);
                } else if (s.toString().length() > 2 && !s.toString().contains("@")) {         // "sth"
                    ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(
                            RegisterScreen.this,
                            android.R.layout.simple_dropdown_item_1line, new String[]{s + "@middlebury.edu"});
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
    }

    /**
     * Displays an animation while a background process is taking place
     * @param showing
     */
    private void setDialogShowing(boolean showing) {
        if (showing) {
            progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText(getString(R.string.dialog_registering));
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
            // again, don't worry
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                setResult(REGISTER_FAILURE_CODE);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRegisterComplete(boolean registerSuccess, ParseException e) {
        setDialogShowing(false);

        if (registerSuccess) {
            Toast.makeText(RegisterScreen.this, "Register Success", Toast.LENGTH_SHORT).show();   // This toast is temporary
            setResult(REGISTER_SUCCESS_CODE);           // go back to LoginScreen and check this
            finish();
            Log.i("Register Success", "Register Success");
        } else {
            if (e.getCode() == ParseException.CONNECTION_FAILED) {
                Toast.makeText(RegisterScreen.this, getResources().getString(R.string.connection_fail), Toast.LENGTH_SHORT).show();
                Log.i("Register Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                Toast.makeText(RegisterScreen.this, getResources().getString(R.string.inter_server_err), Toast.LENGTH_SHORT).show();
                Log.i("Register Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == ParseException.TIMEOUT) {
                Toast.makeText(RegisterScreen.this, getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                Log.i("Register Error", e.getCode() + " " + e.getMessage());
            } else if (e.getCode() == 202) {
                Toast.makeText(RegisterScreen.this, getResources().getString(R.string.user_exists), Toast.LENGTH_SHORT).show();
                Log.i("Register Error", e.getCode() + " " + e.getMessage());
            } else {
                Log.i("Register Error", e.getCode() + " " + e.getMessage());
                Toast.makeText(RegisterScreen.this, getResources().getString(R.string.other_failure), Toast.LENGTH_SHORT).show();
            }
            setResult(REGISTER_FAILURE_CODE);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
