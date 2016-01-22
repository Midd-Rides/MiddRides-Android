package com.middleendien.middrides;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.middleendien.middrides.fragment.SettingsFragment;
import com.middleendien.middrides.utils.Synchronizer.OnSynchronizeListener;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Peter on 10/1/15.
 * TODO: pass the parent activity in with intent
 * TODO: so that when user logs out, the parent activity finishes as well
 */
public class SettingsScreen extends AppCompatActivity {

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
}
