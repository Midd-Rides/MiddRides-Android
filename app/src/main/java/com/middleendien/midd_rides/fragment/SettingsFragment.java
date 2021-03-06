package com.middleendien.midd_rides.fragment;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.Stop;
import com.middleendien.midd_rides.models.User;
import com.middleendien.midd_rides.utils.NetworkUtil;
import com.middleendien.midd_rides.utils.RequestUtil;
import com.middleendien.midd_rides.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.DialogInterface.*;
import static android.preference.Preference.*;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = "SettingsFragment";

    private Preference cancelRequestPref;
    private Preference logOutPref;
    private Preference resetPasswordPref;
    private Preference verifyStatusPref;

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
        resetPasswordPref       = findPreference(getString(R.string.pref_change_password));
        verifyStatusPref        = findPreference(getString(R.string.pref_verification_status_unavailable));

        PreferenceCategory userPrefCat = (PreferenceCategory) findPreference(getString(R.string.cat_user));

        // email verification
        User currentUser = UserUtil.getCurrentUser(getActivity());
        if (currentUser != null) {
            // default true
            verifyStatusPref.setTitle(currentUser.isVerified() ?
                    getString(R.string.pref_verified) : getString(R.string.pref_not_verified));
            userPrefCat.setTitle(currentUser.getEmail());
        } else {
            verifyStatusPref.setTitle(getString(R.string.pref_verification_status_unavailable));
        }
    }

    @SuppressWarnings("all")
    private void initEvent() {
        // availability of the preference
        if (UserUtil.getCurrentUser(getActivity()) == null) {                   // not logged in
            cancelRequestPref.setEnabled(false);
        } else {
            Log.d(TAG, "Current user pending request: " + RequestUtil.getPendingRequest(getActivity()));
            cancelRequestPref.setEnabled(RequestUtil.getPendingRequest(getActivity()) != null);
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
                final Boolean hasPendingRequest = RequestUtil.getPendingRequest(getActivity()) != null;

                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.dialog_msg_are_you_sure))
                        .setMessage(hasPendingRequest ? getString(R.string.dialog_msg_will_cancel_request) : null)
                        .setCancelable(true)
                        .setNegativeButton(getString(R.string.dialog_btn_cancel), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do nothing
                            }
                        })
                        .setPositiveButton(getString(R.string.dialog_btn_yes), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (hasPendingRequest) {
                                    // cancel the request first
                                    cancelCurrentRequest(true);
                                } else {
                                    getActivity().setResult(USER_LOGOUT_RESULT_CODE);
                                    getActivity().finish();
                                }
                            }
                        })
                        .create()
                        .show();

                return false;
            }
        });

        resetPasswordPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.dialog_msg_are_you_sure))
                        .setPositiveButton(getString(R.string.dialog_btn_yes), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // show logout screen
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction()
                                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                        .replace(android.R.id.content, new ResetPasswordFragment())
                                        .addToBackStack("settings")
                                        .commit();
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_btn_cancel), new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();

                return false;
            }
        });

        verifyStatusPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final User currentUser = UserUtil.getCurrentUser(getActivity());
                if (!currentUser.isVerified()) {    // email not verified
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.dialog_msg_are_you_sure))
                            .setPositiveButton(getString(R.string.dialog_btn_yes), new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    NetworkUtil.getInstance().sendVerificationEmail(
                                            currentUser.getEmail(),
                                            currentUser.getPassword(),
                                            getActivity(),
                                            new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if (response.isSuccessful())
                                                        Log.i(TAG, "Re-sent Email Verification");
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                    t.printStackTrace();
                                                    Toast.makeText(getActivity(), getString(R.string.failed_to_talk_to_server), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                    );

                                    dialogInterface.dismiss();
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.resent_email))
                                            .setPositiveButton(getString(R.string.dialog_btn_got_it), null)
                                            .create()
                                            .show();
                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_btn_cancel), new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();

//                    new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
//                            .setTitleText(getString(R.string.dialog_msg_are_you_sure))
//                            .setConfirmText(getString(R.string.dialog_btn_yes))
//                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    NetworkUtil.getInstance().sendVerificationEmail(
//                                            currentUser.getEmail(),
//                                            currentUser.getPassword(),
//                                            getActivity(),
//                                            new Callback<ResponseBody>() {
//                                                @Override
//                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                                                    if (response.isSuccessful())
//                                                        Log.i(TAG, "Re-sent Email Verification");
//                                                }
//
//                                                @Override
//                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
//                                                    t.printStackTrace();
//                                                    Toast.makeText(getActivity(), getString(R.string.failed_to_talk_to_server), Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                    );
//
//                                    sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
//                                    sweetAlertDialog.setTitleText(getString(R.string.resent_email))
//                                            .setConfirmText(getString(R.string.dialog_btn_got_it))
//                                            .setConfirmClickListener(null)
//                                            .showCancelButton(false)
//                                            .show();
//                                }
//                            })
//                            .setCancelText(getString(R.string.dialog_btn_cancel))
//                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                    sweetAlertDialog.dismissWithAnimation();
//                                }
//                            })
//                            .show();
                }

                return false;
            }
        });
    }

    /***
     * Cancel request, might also log user out
     * @param andLogOut whether to log out after cancelling current request
     */
    private void cancelCurrentRequest(final Boolean andLogOut) {
        final User currentUser = UserUtil.getCurrentUser(getActivity());
        // TODO: if currentUser is null, log out or show something weird
        Stop pendingRequestStop = RequestUtil.getPendingRequest(getActivity());
        NetworkUtil.getInstance().cancelRequest(
                currentUser.getEmail(),
                currentUser.getPassword(),
                pendingRequestStop.getStopId(),
                getActivity(),
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            JSONObject body;
                            if (!response.isSuccessful()) {         // cancel failed
                                body = new JSONObject(response.errorBody().string());
                                Toast.makeText(getActivity(), body.getString(getString(R.string.res_param_error)), Toast.LENGTH_SHORT).show();
                                Log.d(TAG, body.toString());
                            } else {                // cancel success
                                cancelRequestPref.setEnabled(false);
                                getActivity().setResult(USER_CANCEL_REQUEST_RESULT_CODE);
                                RequestUtil.putPendingRequest(null, getActivity());     // empty pending request
                                // go ahead and log out
                                if (andLogOut) {
                                    getActivity().setResult(USER_LOGOUT_RESULT_CODE);
                                    getActivity().finish();
                                } else
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle(getString(R.string.dialog_title_request_cancelled))
                                            .setPositiveButton(getString(R.string.dialog_btn_dismiss), null)
                                            .create()
                                            .show();
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getActivity(), getString(R.string.failed_to_talk_to_server), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        if (UserUtil.getCurrentUser(getActivity()) != null &&           // please test before you commit
                RequestUtil.getPendingRequest(getActivity()) != null) {    // has pending request
            cancelRequestPref.setEnabled(true);
        } else {
            cancelRequestPref.setEnabled(false);
        }
    }
}
