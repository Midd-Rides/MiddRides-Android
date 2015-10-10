package com.middleendien.middrides;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.middleendien.middrides.backup.AnnouncementFragment;
import com.middleendien.middrides.utils.AnnouncementDialogFragment;

public class MainScreen extends AppCompatActivity {

    private MenuItem loginButton;

    private AnnouncementDialogFragment announcementDialogFragment;

    // for settings such as announcement, user name and login status and so on
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    private void initEvent() {
        // check for announcements
        if(hasAnnouncement()){
            announcementDialogFragment = new AnnouncementDialogFragment();
            announcementDialogFragment
                    .show(getFragmentManager(), "Announcement");
        }
    }

    private void initView() {
        // temporary announcement

        // define the floating action button
        FloatingActionButton callService = (FloatingActionButton) findViewById(R.id.fab);
        callService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.not_logged_in, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Toast toast = Toast.makeText(MainScreen.this, "We Send a request", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        // define the action bar button
        // to change showAsAction property later
        loginButton = (MenuItem) findViewById(R.id.action_login);
    }

    private boolean hasAnnouncement() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Action Bar items' click events
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent intentSettings = new Intent(MainScreen.this, Settings.class);
                startActivity(intentSettings);
                return true;

            case R.id.action_login:
                Intent intentLogin = new Intent(MainScreen.this, LoginPage.class);
                startActivity(intentLogin);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
