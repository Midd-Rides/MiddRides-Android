package com.middleendien.middrides;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.middleendien.middrides.utils.Synchronizer;
import com.middleendien.middrides.utils.Synchronizer.OnSynchronizeListener;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Peter on 10/16/15.
 *
 */
public class UserScreen extends AppCompatActivity implements OnSynchronizeListener{

    private ImageView userAvatar;
    private Button logoutButton;
    private Button resendButton;
    private Button resetButton;
    private TextView verificationStatusTextView;

    private static final int USER_RESET_PASSWORD_REQUEST_CODE               = 0x101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        setTitle(ParseUser.getCurrentUser().getEmail());

        initView();

        initEvent();
    }

    private void initView() {
        userAvatar = (ImageView) findViewById(R.id.user_avatar);
        logoutButton = (Button) findViewById(R.id.btn_logout);
        resendButton = (Button) findViewById(R.id.btn_resend_email);
        resetButton = (Button) findViewById(R.id.btn_reset_passwd);
        verificationStatusTextView = (TextView) findViewById(R.id.verification_status);

        if(ParseUser.getCurrentUser().isAuthenticated()){
            verificationStatusTextView.setText(getResources().getString(R.string.email_verified));
            resendButton.setVisibility(View.GONE);
        }
        else{
            verificationStatusTextView.setText(getResources().getString(R.string.email_not_verified));
            resendButton.setVisibility(View.VISIBLE);
        }
    }

    private void initEvent() {
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Intent toLoginScreen = new Intent(UserScreen.this, LoginScreen.class);
                startActivity(toLoginScreen);
                finish();
            }
        });

        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ParseUser.getCurrentUser().isAuthenticated()){
                    Toast.makeText(UserScreen.this, getString(R.string.already_verified), Toast.LENGTH_SHORT).show();
                }
                else {
                    ParseUser.getCurrentUser().saveInBackground();
                    Toast.makeText(UserScreen.this, getString(R.string.resent_email), Toast.LENGTH_SHORT).show();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("UserScreen", "Reset Password");
                Synchronizer.getInstance(UserScreen.this).resetPassword(ParseUser.getCurrentUser().getEmail(),
                        USER_RESET_PASSWORD_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onGetObjectComplete(ParseObject object, int requestCode) {
        // do nothing
    }

    @Override
    public void onGetListObjectsComplete(List<ParseObject> objects, int requestCode) {
        // do nothing
    }

    @Override
    public void onResetPasswordComplete(boolean success, int requestCode) {
        switch (requestCode) {
            case USER_RESET_PASSWORD_REQUEST_CODE:
                Toast.makeText(UserScreen.this,
                        success ? getString(R.string.reset_email_sent) : getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }
}
