package com.middleendien.midd_rides;

/**
 * Created by Peter on 8/11/16.
 * Routes and other secrets, preferably in .gitignore
 */

public class Constants {

    public static final String SERVER_BASE_URL = "http://10.0.2.2:3000";

    /* GET */
    public static final String INDEX_URL = "/";
    public static final String UPDATE_LOCATION_URL = "/location";

    /* POST */
    public static final String LOGIN_URL = "/login";
    public static final String REGISTER_URL = "/register";
    public static final String CHANGE_PASSWORD_URL = "changepwd";
    public static final String MAKE_REQUEST_URL = "/request";

    /* DELETE */
    public static final String CANCEL_REQUEST_URL = "/cancel";

}
