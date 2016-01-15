package com.middleendien.middrides.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.middleendien.middrides.R;
import com.middleendien.middrides.utils.Synchronizer;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import static android.preference.Preference.*;

public class SettingsFragment extends PreferenceFragment {

    private Preference cancelRequestPref;
    private Preference logOutPref;
    private Preference resetPasswdPref;
    private Preference veriStatusPref;

    private PreferenceCategory userPrefCat;

    private static final int USER_RESET_PASSWORD_REQUEST_CODE               = 0x101;
    private static final int USER_LOGOUT_RESULT_CODE                        = 0x102;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        getPrefs();

        initEvent();
    }

    private void getPrefs() {
        cancelRequestPref       = findPreference(getString(R.string.pref_cancel_request));
        logOutPref              = findPreference(getString(R.string.pref_log_out));
        resetPasswdPref         = findPreference(getString(R.string.pref_reset_passwd));
        veriStatusPref          = findPreference(getString(R.string.pref_verification_status_unavailable));

        if (ParseUser.getCurrentUser() != null) {
            veriStatusPref.setTitle(ParseUser.getCurrentUser().isAuthenticated() ?
                    getString(R.string.pref_verfied) : getString(R.string.pref_not_verifed));
        } else {
            veriStatusPref.setTitle(getString(R.string.pref_verification_status_unavailable));
        }

        userPrefCat             = (PreferenceCategory) findPreference(getString(R.string.cat_user));
        userPrefCat.setTitle("User - " + ParseUser.getCurrentUser().getEmail());
    }

    private void initEvent() {
        // availability of the preference
        if (ParseUser.getCurrentUser() == null) {                   // not logged in
            cancelRequestPref.setEnabled(false);
        } else {
            Log.d("CurrentUser Pending", ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request)) + "");
            cancelRequestPref.setEnabled(ParseUser.getCurrentUser()
                    .getBoolean(getString(R.string.parse_user_pending_request)));
        }

        // initialise click events
        // cancel
        cancelRequestPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
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

                                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                    editor.putBoolean(getString(R.string.parse_user_pending_request), false).apply();

                                    cancelRequestPref.setEnabled(false);

                                    Toast.makeText(getActivity(), getString(R.string.request_cancelled), Toast.LENGTH_SHORT).show();
                                } else {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                return true;
            }
        });

        // logout
        logOutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.dialog_msg_are_you_sure))
                        .setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ParseUser.logOut();
                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                installation.put("user","0");
                                installation.saveInBackground();

//                                Intent toLoginScreen = new Intent(getActivity(), LoginScreen.class);
//                                toLoginScreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(toLoginScreen);
//                                getActivity().finish();
                                getActivity().setResult(USER_LOGOUT_RESULT_CODE);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .create()
                        .show();

                return false;
            }
        });

        resetPasswdPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.dialog_msg_are_you_sure))
                        .setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Synchronizer.getInstance(getActivity()).resetPassword(ParseUser.getCurrentUser().getEmail(),
                                        USER_RESET_PASSWORD_REQUEST_CODE);
                                Log.i("SettingsFragment", "Reset Password");
                            }
                        })
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .create()
                        .show();
                return false;
            }
        });

        veriStatusPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!ParseUser.getCurrentUser().isAuthenticated()) {            // email not verified
                    ParseUser.getCurrentUser().saveInBackground();
                    Toast.makeText(getActivity(), getString(R.string.resent_email), Toast.LENGTH_SHORT).show();
                }
                return false;
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
