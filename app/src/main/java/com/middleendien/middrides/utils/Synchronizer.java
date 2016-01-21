package com.middleendien.middrides.utils;

import android.content.Context;
import android.util.Log;

import com.middleendien.middrides.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import java.util.List;

/**
 * Created by Peter on 11/17/15.
 * Used to sync all sorts of stuff between local storage and Parse
 * since parse uses async itself, we don't need to implement that
 * callback is OnSynchronizeListener
 * All classes that wish to sync data will implement this
 *
 * Singleton pattern
 */
public class Synchronizer {

    private static Synchronizer synchronizer;
    private Context context;                    // for editing SharedPreferences
    private OnSynchronizeListener callback;

    public static Synchronizer getInstance(Context context) {
        if (synchronizer == null) {
            synchronizer = new Synchronizer(context);
        }
        return synchronizer;
    }

    public Synchronizer (Context context) {
        this.context = context;
        this.callback = (OnSynchronizeListener) context;
    }

    @SuppressWarnings("all")
    public void getObject (String varName, String objectId, String className, final int requestCode) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(className);
        if (varName != null) {      // search by name
            // haven't thought of a scenario where this would be useful
            // normally we could just use find() || findInBackground, so maybe this is not necessary
            // for now, just keep the first parameter as null
        } else {                    // search by Id
            parseQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        callback.onGetObjectComplete(object, requestCode);
                    } else {        // error syncing
                        // do nothing
                    }
                }
            });
        }
    }

    public void getListObjects (String className, final int requestCode) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(className);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    callback.onGetListObjectsComplete(objects, requestCode);
                    Log.d("getListObjects", "Success");
                } else {            // error syncing
                    e.printStackTrace();
                }
            }
        });
    }

    public void getListObjectsLocal (String className, final int requestCode) {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(className);
        parseQuery.fromLocalDatastore();
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // success, verified        - Peter
                    callback.onGetListObjectsComplete(objects, requestCode);
                } else {            // error syncing
                    // do nothing
                }
            }
        });
    }

    public void resetPassword (String email, final int requestCode) {
        ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                callback.onResetPasswordComplete(e == null, requestCode);
            }
        });
    }

    public void refreshObject (ParseObject object) {
        object.fetchInBackground();
    }



    public interface OnSynchronizeListener {

        void onGetObjectComplete(ParseObject object, int requestCode);

        void onGetListObjectsComplete(List<ParseObject> objects, int requestCode);

        void onResetPasswordComplete(boolean resetSuccess, int requestCode);

    }






//    // Async Task to get Double type value
//    class GetDoubleTaskAsync extends AsyncTask<String, Void, Double> {
//        private OnSynchronizeListener callback;
//
//        @Override
//        protected Double doInBackground(String... params) {         // please make sure that you query one at a time
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Double aDouble) {
//            super.onPostExecute(aDouble);
//        }
//    }

}
