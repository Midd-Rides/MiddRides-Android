package com.middleendien.midd_rides.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.network.NetworkAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Peter on 1/15/16.
 *
 */

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;
    private AlertDialog dialog;

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
        dialog = builder.setIcon(R.drawable.logo_with_background)
                .setTitle(getString(R.string.dialog_title_service_down))
                .setMessage(getString(R.string.dialog_msg_middrides_not_available))
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_BACK:
                        finish();
                    default:
                        return false;
                }
            }
        });

        // check if server is running
        NetworkAgent.getInstance().isServerRunning(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject body = new JSONObject(response.body().string());
                            if (body.getBoolean(getString(R.string.res_param_status))) {
                                // TODO: check if logged in
                                Toast.makeText(SplashActivity.this, "Server Running", Toast.LENGTH_SHORT).show();
                            } else {
                                dialog.show();
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            dialog.show();
                        }
                    }
                }, SPLASH_TIME_OUT);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                dialog.show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (dialog.isShowing())
            finish();
        // disable back press when I'm TALKING!!! (to the server)
    }
}
