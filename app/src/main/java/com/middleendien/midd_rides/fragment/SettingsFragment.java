package com.middleendien.midd_rides.fragment;


import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.User;
import com.middleendien.midd_rides.utils.UserUtil;

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

        PreferenceCategory userPrefCat = (PreferenceCategory) findPreference(getString(R.string.cat_user));

        // email verification
        User currentUser = UserUtil.getCurrentUser(getActivity());
        if (currentUser != null) {
            // default true
            veriStatusPref.setTitle(currentUser.isVerified() ?
                    getString(R.string.pref_verified) : getString(R.string.pref_not_verifed));
            userPrefCat.setTitle(currentUser.getEmail());
        } else {
            veriStatusPref.setTitle(getString(R.string.pref_verification_status_unavailable));
        }
    }

    @SuppressWarnings("all")
    private void initEvent() {
        // availability of the preference
        if (UserUtil.getCurrentUser(getActivity()) == null) {                   // not logged in
            cancelRequestPref.setEnabled(false);
        } else {
            // TODO: save pending request in SharedPreference
//            Log.d("SettingsFragment", "Current user pending request: " + ParseUser.getCurrentUser().getBoolean(getString(R.string.parse_user_pending_request)) + "");
//            cancelRequestPref.setEnabled(UserUtil.getCurrentUser(getActivity())
//                    .getBoolean(getString(R.string.parse_user_pending_request)));
        }

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
                // TODO: pending request thing
//                final Boolean hasPendingRequest = UserUtil.getCurrentUser(getActivity()).getBoolean(getString(R.string.parse_user_pending_request));
                final Boolean hasPendingRequest = false;

                new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.dialog_msg_are_you_sure))
                        .setContentText(hasPendingRequest ? getString(R.string.dialog_msg_will_cancel_request) : null)
                        .showContentText(hasPendingRequest)
                        .setConfirmText(getString(R.string.dialog_btn_yes))
                        .setCancelText(getString(R.string.dialog_btn_cancel))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (hasPendingRequest) {
                                    // cancel the request first
                                    cancelCurrentRequest(true);
                                } else {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(getString(R.string.waiting_to_log_out), false).apply();
                                    // TODO: what's this for

                                    UserUtil.reset(getActivity());
                                    getActivity().setResult(USER_LOGOUT_RESULT_CODE);
                                    getActivity().finish();
                                }
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
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                // TODO: to reset screen
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                        .replace(android.R.id.content, new ResetPasswordFragment())
                                        .addToBackStack("settings")
                                        .commit();
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .setCancelText(getString(R.string.dialog_btn_cancel))
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                            }
                        })
                        .show();

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
