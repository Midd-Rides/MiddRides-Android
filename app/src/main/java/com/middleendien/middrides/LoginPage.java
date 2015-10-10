package com.middleendien.middrides;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.middleendien.middrides.utils.LoginAgent;

import java.util.List;

/**
 * A login screen that offers login via email/password.
 *
 * Note: This class is the entry point of the app
 *
 */
public class LoginPage extends AppCompatActivity{

    private Button btnLogIn;
    private Button btnRegister;
    private Button btnSkipLogIn;

    LoginAgent currentUser;

    private static final int REGISTER_REQUEST_CODE = 0x001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        MenuInflater inflater = getMenuInflater();

        initView();

        initData();

        initEvent();
    }

    private void initView() {
        btnLogIn = (Button) findViewById(R.id.login_button);
        btnRegister = (Button) findViewById(R.id.register_button);
        btnSkipLogIn = (Button) findViewById(R.id.skip_login_button);
    }

    private void initData() {
        currentUser = new LoginAgent();
        //TODO: login through LoginAgent
    }

    private void initEvent() {
        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {               // login business is implemented with the LoginAgent class
                //TODO: if successfully logs in
                Intent toMainScreen = new Intent(LoginPage.this, MainScreen.class);
                toMainScreen.putExtra(getResources().getString(R.string.is_logged_in), true);       // MainScreen will check this first
                toMainScreen.putExtra(getResources().getString(R.string.current_user), currentUser);                                        // if not logged, wouldn't check LoginAgent
                startActivity(toMainScreen);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {           // switch to register page
                Intent toRegisterScreen = new Intent(LoginPage.this, RegisterPage.class);
                startActivityForResult(toRegisterScreen, REGISTER_REQUEST_CODE);
            }
        });

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

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // TODO: SDK 23 and above, I think we just Internet permission, that should be all.
    }
}

