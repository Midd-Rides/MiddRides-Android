package com.middleendien.middrides;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.middleendien.middrides.utils.LoginAgent;
import com.middleendien.middrides.utils.LoginAgent.OnRegisterListener;
import com.parse.ParseException;

/**
 * Created by Peter on 10/5/15.
 *
 */
public class RegisterScreen extends AppCompatActivity implements OnRegisterListener {
    private EditText usernameBox;
    private EditText passwdBox;
    private Button registerButton;

//    private LoginAgent loginAgent;

    private static final int REGISTER_SUCCESS_CODE = 0x101;
    private static final int REGISTER_FAILURE_CODE = 0x102;

    private ProgressDialog progressDialog;

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
        loginAgent = LoginAgent.getInstance();
        loginAgent.registerListener(LoginAgent.REGISTER, this);
    }

    private void initView() {
        usernameBox = (EditText) findViewById(R.id.reg_username);
        passwdBox = (EditText) findViewById(R.id.reg_passwd);

        //TODO: tap and hide keyboard


        registerButton = (Button) findViewById(R.id.reg_button);
    }

    private void initEvent() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check e-mail validity
                if (!LoginAgent.isEmailValid(usernameBox.getText().toString())) {
                    Toast.makeText(RegisterScreen.this, getResources().getString(R.string.wrong_email), Toast.LENGTH_SHORT).show();
                    return;
                }

                setDialogShowing(true);
                loginAgent.registerInBackground(usernameBox.getText().toString(), passwdBox.getText().toString());

                hideKeyboard();
            }
        });
    }

    private void setDialogShowing(boolean showing) {
        if (showing) {
            progressDialog = new ProgressDialog(RegisterScreen.this);
            progressDialog.setMessage(getString(R.string.dialog_registering));
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        } else {
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
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
}
