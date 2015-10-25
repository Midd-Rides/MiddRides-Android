package com.middleendien.middrides.utils;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.middleendien.middrides.MainScreen;
import com.middleendien.middrides.R;
import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.ParseException;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference cancelRequestButton =  findPreference(getString(R.string.cancelRequest_button));
        if((boolean)ParseUser.getCurrentUser().get(MainScreen.PENDING_USER_REQUEST_PARSE_KEY) == true){
            cancelRequestButton.setEnabled(true);
        }else{
            cancelRequestButton.setEnabled(false);
        }

        cancelRequestButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                ParseQuery<ParseObject> query = ParseQuery.getQuery(MainScreen.USER_REQUESTS_PARSE_OBJECT);
                query.getInBackground((String)ParseUser.getCurrentUser().get(MainScreen.PENDING_USER_REQUESTID_PARSE_KEY), new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject requestToDelete, com.parse.ParseException e) {
                        if(e == null){
                            //Delete pending requests and set peding requests to false
                            requestToDelete.deleteInBackground();
                            ParseUser.getCurrentUser().put(MainScreen.PENDING_USER_REQUEST_PARSE_KEY, false);
                            ParseUser.getCurrentUser().saveInBackground();
                        }
                        else {
                            Toast.makeText(getActivity(),"Something went wrong",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
        });

    }
}
