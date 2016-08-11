package com.middleendien.midd_rides;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.middleendien.midd_rides.utils.HardwareUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Peter on 10/5/15.
 *
 */
public class RegisterScreen extends AppCompatActivity {
    private AutoCompleteTextView usernameBox;
    private EditText passwdBox;
    private EditText passwdConfirmBox;


    private Button registerButton;

    private static final int REGISTER_SUCCESS_CODE = 0x101;
    private static final int REGISTER_FAILURE_CODE = 0x102;

    private SweetAlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        initAnim();

        initData();

        initView();

        initEvent();
    }

    private void initAnim() {
        if (Build.VERSION.SDK_INT > 21) {
            getWindow().setEnterTransition(new Slide(Gravity.END)
                    .excludeTarget(android.R.id.statusBarBackground, true)
                    .excludeTarget(android.R.id.navigationBarBackground, true));

            getWindow().setReturnTransition(new Slide(Gravity.END)
                    .excludeTarget(android.R.id.statusBarBackground, true)
                    .excludeTarget(android.R.id.navigationBarBackground, true));
        }
    }

    private void initData() {
        // possibly nothing
        // TODO:
//        loginAgent = LoginAgent.getInstance(this);
//        loginAgent.registerListener(LoginAgent.REGISTER, this);
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
                // TODO:
//                if (!LoginAgent.isEmailValid(usernameBox.getText().toString())) {
//                    Toast.makeText(RegisterScreen.this, getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
//                    return;
//                }

                // check password match
                if (!passwdBox.getText().toString().equals(passwdConfirmBox.getText().toString())) {
                    Toast.makeText(RegisterScreen.this, getString(R.string.passwd_not_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!HardwareUtil.isNetworkAvailable(getApplicationContext())){
                    Toast.makeText(RegisterScreen.this, getResources().getString(R.string.no_internet_warning), Toast.LENGTH_SHORT).show();
                    return;
                }

                setDialogShowing(true);
                // TODO:
//                loginAgent.registerInBackground(usernameBox.getText().toString(), passwdBox.getText().toString());

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
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
