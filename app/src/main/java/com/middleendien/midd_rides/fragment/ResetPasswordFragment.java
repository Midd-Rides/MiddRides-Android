package com.middleendien.midd_rides.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.middleendien.midd_rides.R;
import com.middleendien.midd_rides.models.User;
import com.middleendien.midd_rides.utils.NetworkUtil;
import com.middleendien.midd_rides.utils.Privacy;
import com.middleendien.midd_rides.utils.UserUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Peter on 8/15/16.
 *
 */

public class ResetPasswordFragment extends Fragment {

    private static final String TAG = "ResetPasswordFragment";

    private View rootView;

    private Button changePassword;
    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;

    private SweetAlertDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reset_password, container, false);

        initView();

        initEvent();

        return rootView;
    }

    private void initView() {
        oldPassword = (EditText) rootView.findViewById(R.id.btn_old_password);
        newPassword = (EditText) rootView.findViewById(R.id.btn_new_password);
        confirmPassword = (EditText) rootView.findViewById(R.id.btn_new_password_confirm);

        changePassword = (Button) rootView.findViewById(R.id.btn_change_password);
    }

    private void initEvent() {
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPass = oldPassword.getText().toString();
                final String newPass = newPassword.getText().toString();
                String confirmPass = confirmPassword.getText().toString();

                User currentUser = UserUtil.getCurrentUser(getActivity());
                if (!currentUser.getPassword().equals(Privacy.encodePassword(oldPass))) {
                    // wrong old password
                    Toast.makeText(getActivity(), getString(R.string.password_incorrect), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    // password don't match
                    Toast.makeText(getActivity(), getString(R.string.password_dont_match), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPass.equals(oldPass)) {
                    // same password
                    Toast.makeText(getActivity(), getString(R.string.password_didnt_change), Toast.LENGTH_SHORT).show();
                    return;
                }

                setDialogShowing(true);
                NetworkUtil.getInstance().changePassword(
                        currentUser.getEmail(),
                        currentUser.getPassword(),
                        Privacy.encodePassword(newPass),
                        getActivity(),
                        new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                setDialogShowing(false);
                                try {
                                    JSONObject body;
                                    if (!response.isSuccessful()) {             // reset unsuccessful
                                        body = new JSONObject(response.errorBody().string());
                                        Toast.makeText(getActivity(), getString(R.string.res_param_error), Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, body.toString());
                                    } else {                                    // reset success
                                        UserUtil.getCurrentUser(getActivity()).setPassword(Privacy.encodePassword(newPass));
                                        getFragmentManager().beginTransaction().remove(ResetPasswordFragment.this).commit();
                                        getFragmentManager().popBackStack();
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
        });
    }

    private void setDialogShowing(boolean showing) {
        if (showing) {
            progressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE)
                    .setTitleText(getString(R.string.dialog_changing_password));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.showCancelButton(false);
            progressDialog.getProgressHelper().setBarColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
            progressDialog.show();
        } else {
            if (progressDialog.isShowing())
                progressDialog.dismissWithAnimation();
        }
    }

}
