package com.middleendien.middrides;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;

/**
 * Created by Peter on 10/16/15.
 */
public class UserScreen extends AppCompatActivity {

    private ImageView userAvatar;
    private Button logoutButton;
    private Button resendButton;
    private TextView verificationStatusTextView;

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
                Intent toLoginScreen = new Intent(UserScreen.this, LoginPage.class);
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
    }

}
