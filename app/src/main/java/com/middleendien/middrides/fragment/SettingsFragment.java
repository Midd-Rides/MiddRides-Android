package com.middleendien.middrides.fragment;


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

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.preference.Preference.*;

public class SettingsFragment extends PreferenceFragment {

    private Preference cancelRequestPref;
    private Preference logOutPref;
    private Preference resetPasswdPref;
    private Preference veriStatusPref;

    private static final int INCREMENT_FIELD_REQUEST_CODE                   = 0x100;
    private static final int USER_RESET_PASSWORD_REQUEST_CODE               = 0x101;

    private static final int USER_LOGOUT_RESULT_CODE                        = 0x102;
    private static final int USER_CANCEL_REQUEST_RESULT_CODE                = 0x103;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        getPrefs();

        initEvent();
    }

    private void getPrefs() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        cancelRequestPref       = findPreference(getString(R.string.pref_cancel_request));
        logOutPref              = findPreference(getString(R.string.pref_log_out));
        resetPasswdPref         = findPreference(getString(R.string.pref_reset_passwd));
        veriStatusPref          = findPreference(getString(R.string.pref_verification_status_unavailable));

        // email verification
        if (ParseUser.getCurrentUser() != null) {
            // default true
            veriStatusPref.setTitle(ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified)) ?
                    getString(R.string.pref_verfied) : getString(R.string.pref_not_verifed));
        } else {
            veriStatusPref.setTitle(getString(R.string.pref_verification_status_unavailable));
        }

        PreferenceCategory userPrefCat = (PreferenceCategory) findPreference(getString(R.string.cat_user));
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

                                    Synchronizer.getInstance(getActivity()).getObject(
                                            null,
                                            requestToBeDeleted.getString(getString(R.string.parse_request_locationID)),
                                            getString(R.string.parse_class_location),
                                            INCREMENT_FIELD_REQUEST_CODE);

                                    getActivity().setResult(USER_CANCEL_REQUEST_RESULT_CODE);

                                    new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                                            .setTitleText(getString(R.string.request_cancelled))
                                            .setConfirmText(getString(R.string.dialog_btn_dismiss))
                                            .show();

                                    Log.i("SettingsFragment", "Request Cancelled");
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
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.dialog_msg_are_you_sure))
                        .setConfirmText(getString(R.string.dialog_btn_yes))
                        .setCancelText(getString(R.string.dialog_btn_cancel))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                // TODO: check for cancel request
                                ParseUser.logOut();
                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                installation.put("user", "0");
                                installation.saveInBackground();
                                getActivity().setResult(USER_LOGOUT_RESULT_CODE);
                                getActivity().finish();
                            }
                        }).show();

                return false;
            }
        });

        resetPasswdPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.dialog_msg_are_you_sure))
                        .setConfirmText(getString(R.string.dialog_btn_yes))
                        .setCancelText(getString(R.string.dialog_btn_cancel))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Synchronizer.getInstance(getActivity()).resetPassword(ParseUser.getCurrentUser().getEmail(),
                                        USER_RESET_PASSWORD_REQUEST_CODE);
                                Log.i("SettingsFragment", "Reset Password");
                            }
                        }).show();

                return false;
            }
        });

        veriStatusPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {    // email not verified
                    ParseUser.getCurrentUser().saveInBackground();
                    new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText(getString(R.string.resent_email))
                            .setConfirmText(getString(R.string.dialog_btn_got_it))
                            .show();
                    Log.i("SettingsFragment", "Re-sent Email Verification");
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
