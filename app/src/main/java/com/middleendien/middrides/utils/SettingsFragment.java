package com.middleendien.middrides.utils;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.middleendien.middrides.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SettingsFragment extends PreferenceFragment {

    private Preference cancelRequestPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        cancelRequestPref =  findPreference(getString(R.string.cancelRequest_button));

        if (ParseUser.getCurrentUser() == null) {                   // not logged in
            cancelRequestPref.setEnabled(false);
        } else {
            Log.d("CurrentUser Pending", ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request)) + "");
            cancelRequestPref.setEnabled(ParseUser.getCurrentUser()
                    .getBoolean(getString(R.string.parse_user_pending_request)));
        }

        cancelRequestPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(getString(R.string.parse_class_request));          // class name
                parseQuery.getInBackground(ParseUser.getCurrentUser().getString(getString(R.string.parse_request_request_id)),
                        new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject requestToBeDeleted, ParseException e) {
                                if (e == null) {
                                    //Delete pending requests and set pending requests to false
                                    requestToBeDeleted.deleteInBackground();
                                    ParseUser.getCurrentUser().put(getString(R.string.parse_user_pending_request), false);
                                    ParseUser.getCurrentUser().saveInBackground();
                                    Toast.makeText(getActivity(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
                                    cancelRequestPref.setEnabled(false);

                                } else {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return true;
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();

        if (ParseUser.getCurrentUser() != null &&           // please test before you commit
                ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {    // has pending request
            cancelRequestPref.setEnabled(true);
        } else {
            cancelRequestPref.setEnabled(false);
        }
    }

}
