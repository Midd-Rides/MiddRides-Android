package com.middleendien.middrides;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.VISIBLE;

/**
 * Created by Peter on 1/15/16.
 *
 *
 *
 */
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ImageView splashLogo = (ImageView) findViewById(R.id.splash_img);

        splashLogo.getLayoutParams().width = (int)(metrics.widthPixels * 0.5);
        splashLogo.getLayoutParams().height = (int)(metrics.heightPixels * 0.4);

        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {
            // Showing splash screen with a timer.
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainScreen.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
