package com.middleendien.middrides;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.middleendien.middrides.fragment.SettingsFragment;
import com.middleendien.middrides.utils.Synchronizer.OnSynchronizeListener;
import com.parse.ParseObject;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Peter on 10/1/15.
 * TODO: pass the parent activity in with intent
 * TODO: so that when user logs out, the parent activity finishes as well
 */
public class SettingsScreen extends AppCompatActivity implements OnSynchronizeListener {

    private static final int USER_RESET_PASSWORD_REQUEST_CODE               = 0x101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);

        android.support.v7.app.ActionBar actionBar= getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onGetObjectComplete(ParseObject object, int requestCode) {

    }

    @Override
    public void onGetListObjectsComplete(List<ParseObject> objects, int requestCode) {

    }

    @Override
    public void onResetPasswordComplete(boolean resetSuccess, int requestCode) {
        switch (requestCode) {
            case USER_RESET_PASSWORD_REQUEST_CODE:
                //TODO: this toast is not shown, figure out why         - Peter
                Toast.makeText(SettingsScreen.this,
                        resetSuccess ? getString(R.string.reset_email_sent) : getString(R.string.something_went_wrong),
                        Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
