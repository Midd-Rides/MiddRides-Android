package com.middleendien.middrides.backup;


import android.app.DialogFragment;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.middleendien.middrides.R;

/**
 * This is backup plan, in case a Dialog isn't good enough, we can use this fragment
 */

public class AnnouncementFragment extends Fragment {

    public AnnouncementFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View retView = inflater.inflate(R.layout.content_announcement_frag, null);

        Log.i("Fragment Created", "Announcement");
        return retView;
    }

    @Override
    public void onDestroy() {
        Log.i("Fragment Destroyed", "Announcement");
        super.onDestroy();
    }
}
