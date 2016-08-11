package com.middleendien.midd_rides.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.middleendien.midd_rides.R;

/**
 * Created by Peter on 1/15/16.
 *
 */

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ImageView splashLogo = (ImageView) findViewById(R.id.splash_img);

        splashLogo.getLayoutParams().width = (int) (metrics.widthPixels * 0.5);
        splashLogo.getLayoutParams().height = (int) (metrics.heightPixels * 0.4);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.setIcon(R.drawable.logo_with_background)
                .setTitle(getString(R.string.dialog_title_service_down))
                .setMessage(getString(R.string.dialog_msg_middrides_not_available))
                .create();
        dialog.setCanceledOnTouchOutside(false);

        // TODO: check server running
//        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(getString(R.string.parse_class_status));
//        parseQuery.getInBackground("oWxwbDQuhL", new GetCallback<ParseObject>() {       // no harm hardcoding
//            @Override
//            public void done(ParseObject object, ParseException e) {
//                if (e == null) {
//                    if (!object.getBoolean(getString(R.string.parse_status_is_running))) {
//                        dialog.show();
//                    } else {
//                        Log.d("Splash Screen", "Service is running ");
//                        new Handler().postDelayed(new Runnable() {
//                            // Showing splash screen with a timer.
//                            @Override
//                            public void run() {
//                                // This method will be executed once the timer is over
//                                // Start your app main activity
//                                Intent i = new Intent(SplashActivity.this, MainActivity.class);
//                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(i);
//
//                                // close this activity
//                                finish();
//                            }
//                        }, SPLASH_TIME_OUT);
//                    }
//                } else {
//                    dialog.show();
//                }
//            }
//        });
    }
}
