package com.middleendien.midd_rides.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.middleendien.midd_rides.R;

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
        cancelRequestPref       = findPreference(getString(R.string.pref_cancel_request));
        logOutPref              = findPreference(getString(R.string.pref_log_out));
        resetPasswdPref         = findPreference(getString(R.string.pref_reset_passwd));
        veriStatusPref          = findPreference(getString(R.string.pref_verification_status_unavailable));

        // email verification
        // TODO:
//        if (ParseUser.getCurrentUser() != null) {
//            // default true
//            veriStatusPref.setTitle(ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified)) ?
//                    getString(R.string.pref_verified) : getString(R.string.pref_not_verifed));
//        } else {
//            veriStatusPref.setTitle(getString(R.string.pref_verification_status_unavailable));
//        }

        PreferenceCategory userPrefCat = (PreferenceCategory) findPreference(getString(R.string.cat_user));
        // TODO:
//        userPrefCat.setTitle(ParseUser.getCurrentUser().getEmail());
    }

    private void initEvent() {
        // availability of the preference
        // TODO:
//        if (ParseUser.getCurrentUser() == null) {                   // not logged in
//            cancelRequestPref.setEnabled(false);
//        } else {
//            Log.d("SettingsFragment", "Current user pending request: " + ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request)) + "");
//            cancelRequestPref.setEnabled(ParseUser.getCurrentUser()
//                    .getBoolean(getString(R.string.parse_user_pending_request)));
//        }

        // initialise click events
        // cancel
        cancelRequestPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                cancelCurrentRequest(false);

                return true;
            }
        });

        // logout
        logOutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                // TODO:
//                final Boolean hasPendingRequest = ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request));
//
//                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
//                        .setTitleText(getString(R.string.dialog_msg_are_you_sure))
//                        .setContentText(hasPendingRequest ? getString(R.string.dialog_msg_will_cancel_request) : null)
//                        .showContentText(hasPendingRequest)
//                        .setConfirmText(getString(R.string.dialog_btn_yes))
//                        .setCancelText(getString(R.string.dialog_btn_cancel))
//                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                if (hasPendingRequest) {
//                                    // cancel the request first
//                                    cancelCurrentRequest(true);
//                                } else {
//                                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                                    editor.putBoolean(getString(R.string.waiting_to_log_out), false).apply();
//                                    logUserOut();
//                                }
//                            }
//                        }).show();

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
                                // TODO:
//                                Synchronizer.getInstance(getActivity()).resetPassword(ParseUser.getCurrentUser().getEmail(),
//                                        USER_RESET_PASSWORD_REQUEST_CODE);
                                sweetAlertDialog.dismissWithAnimation();
                                Log.i("SettingsFragment", "Reset Password");
                            }
                        }).show();

                return false;
            }
        });

        veriStatusPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO:
//                if (!ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_email_verified))) {    // email not verified
//                    ParseUser.getCurrentUser().saveInBackground();
//                    new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
//                            .setTitleText(getString(R.string.resent_email))
//                            .setConfirmText(getString(R.string.dialog_btn_got_it))
//                            .show();
//                    Log.i("SettingsFragment", "Re-sent Email Verification");
//                }
                return false;
            }
        });
    }

    /***
     * Okay so what happens here is:
     * incrementing needs the user alive, for some reason
     * we cancel the request, knowing that it's bound to take longer than returning to MainActivity
     * and return to MainActivity, what a surprise
     * the increment will check upon finish whether we are waiting to log out
     * and will perform log out accordingly
     * and if it logs out, MainActivity will hear it, and return to LoginActivity
     * and they live happily ever since
     * @param andLogOut whether to log out after cancelling current request
     */
    private void cancelCurrentRequest(final Boolean andLogOut) {
        // TODO:
    }

    private void logUserOut() {
        // TODO:
//        LoginAgent.getInstance(getActivity()).logOutInBackground();
        getActivity().setResult(USER_LOGOUT_RESULT_CODE);
        getActivity().finish();
    }

    @Override
    public void onResume(){
        super.onResume();

        // TODO:
//        if (ParseUser.getCurrentUser() != null &&           // please test before you commit
//                ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request))) {    // has pending request
//            cancelRequestPref.setEnabled(true);
//        } else {
//            cancelRequestPref.setEnabled(false);
//        }
    }
}
