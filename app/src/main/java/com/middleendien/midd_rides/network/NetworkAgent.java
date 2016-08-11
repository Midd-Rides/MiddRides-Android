package com.middleendien.midd_rides.network;

import android.content.Context;

import com.middleendien.midd_rides.Constants;
import com.middleendien.midd_rides.R;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by Peter on 8/11/16.
 *
 * Agent for handling connections between app and server
 */

public class NetworkAgent {

    interface MiddRidesService {

        @GET(Constants.INDEX_URL)
        Call<ResponseBody> status();

        @POST(Constants.LOGIN_URL)
        @FormUrlEncoded
        Call<ResponseBody> login(@FieldMap Map<String, String> bodyParams);

        @POST(Constants.REGISTER_URL)
        @FormUrlEncoded
        Call<ResponseBody> register(@FieldMap Map<String, String> bodyParams);

        @GET(Constants.UPDATE_LOCATION_URL)
        Call<ResponseBody> updateLocations(@QueryMap Map<String, String> queryParams);

        @POST(Constants.MAKE_REQUEST_URL)
        @FormUrlEncoded
        Call<ResponseBody> makeRequest(@FieldMap Map<String, String> bodyParams);

        @DELETE(Constants.CANCEL_REQUEST_URL)
        Call<ResponseBody> cancelRequest(@QueryMap Map<String, String> queryParams);

    }

    private static NetworkAgent agent;
    private MiddRidesService service;

    /***
     * Singleton pattern for getting NetworkAgent
     * @return      current agent
     */
    public NetworkAgent getInstance() {
        if (agent == null)
            agent = new NetworkAgent();
        return agent;
    }

    private NetworkAgent() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SERVER_BASE_URL)
                .build();
        service = retrofit.create(MiddRidesService.class);
    }

    /***
     * Check if server if running
     * @param callback      callback
     */
    public void isServerRunning(Callback<ResponseBody> callback) {
        service.status().enqueue(callback);
    }

    /***
     * Log user in
     * @param email         email
     * @param password      password (encoded)
     * @param context       context
     * @param callback      callback
     */
    public void login(String email, String password, Context context, Callback<ResponseBody> callback) {
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(context.getString(R.string.map_param_email), email);
        bodyParams.put(context.getString(R.string.map_param_password), password);
        service.login(bodyParams).enqueue(callback);
    }

    /***
     * Register user
     * @param email         email
     * @param password      password (encoded)
     * @param context       context
     * @param callback      callback
     */
    public void register(String email, String password, Context context, Callback<ResponseBody> callback) {
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(context.getString(R.string.map_param_email), email);
        bodyParams.put(context.getString(R.string.map_param_password), password);
        service.register(bodyParams).enqueue(callback);
    }

    /***
     * Sync latest locations with
     * @param lastUpdatedTimeInMillis       last update time in milliseconds
     * @param context       context
     * @param callback      callback
     */
    public void updateStops(long lastUpdatedTimeInMillis, Context context, Callback<ResponseBody> callback) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(context.getString(R.string.map_param_last_updated_time), String.valueOf(lastUpdatedTimeInMillis));
        service.updateLocations(queryParams).enqueue(callback);
    }

    /***
     * Make MiddRides request to server
     * @param email         email
     * @param password      password (encoded)
     * @param stopId        unique ID of the stop
     * @param context       context
     * @param callback      callback
     */
    public void makeRequest(String email, String password, String stopId, Context context, Callback<ResponseBody> callback) {
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put(context.getString(R.string.map_param_email), email);
        bodyParams.put(context.getString(R.string.map_param_password), password);
        bodyParams.put(context.getString(R.string.map_param_stop_id), stopId);
        service.makeRequest(bodyParams).enqueue(callback);
    }

    /***
     * Cancel MiddRides request
     * @param email         email
     * @param password      password
     * @param stopId        unique ID of the stop
     * @param context       context
     * @param callback      callback
     */
    public void cancelRequest(String email, String password, String stopId, Context context, Callback<ResponseBody> callback) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(context.getString(R.string.map_param_email), email);
        queryParams.put(context.getString(R.string.map_param_password), password);
        queryParams.put(context.getString(R.string.map_param_stop_id), stopId);
        service.cancelRequest(queryParams).enqueue(callback);
    }

}
