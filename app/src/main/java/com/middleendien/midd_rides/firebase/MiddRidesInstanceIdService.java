package com.middleendien.midd_rides.firebase;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Peter on 8/15/16.
 *
 */

public class MiddRidesInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // TODO: send token to server?
        super.onTokenRefresh();
    }
}
