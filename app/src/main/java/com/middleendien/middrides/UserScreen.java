package com.middleendien.middrides;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;

/**
 * Created by Peter on 10/16/15.
 */
public class UserScreen extends AppCompatActivity {

    private ImageView userAvatar;
    private Button logoutButton;
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
        verificationStatusTextView = (TextView) findViewById(R.id.verification_status);
        if(ParseUser.getCurrentUser().isAuthenticated())
            verificationStatusTextView.setText(getResources().getString(R.string.email_verified));
        else
            verificationStatusTextView.setText(getResources().getString(R.string.email_not_verified));
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
    }

}
