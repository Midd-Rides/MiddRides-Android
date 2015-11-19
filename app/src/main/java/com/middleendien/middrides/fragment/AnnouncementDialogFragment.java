package com.middleendien.middrides.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.middleendien.middrides.R;

/**
 * Created by Peter on 10/5/15.
 *
 * The app checks for unread announcements at launch
 * and will display them in this dialog
 *
 */
public class AnnouncementDialogFragment extends DialogFragment {



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());

        dialogBuilder.setMessage("We fired all the drivers!!!")
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: or not TODO:
                    }
                });


        return dialogBuilder.create();
    }
}
