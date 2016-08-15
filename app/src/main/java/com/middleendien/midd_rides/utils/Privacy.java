package com.middleendien.midd_rides.utils;

import android.util.Base64;

/**
 * Created by Peter on 8/15/16.
 * Things that ordinary people are not supposed to see
 */

public class Privacy {

    public static String encodePassword(String unencoded) {
        return Base64.encodeToString(unencoded.getBytes(), Base64.URL_SAFE);
    }

}
