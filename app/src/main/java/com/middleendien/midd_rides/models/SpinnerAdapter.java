package com.middleendien.midd_rides.models;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.middleendien.midd_rides.models.Stop;

import java.util.List;

/**
 * Created by Peter on 8/16/16.
 *
 */

public class SpinnerAdapter extends ArrayAdapter<Stop> {

    public SpinnerAdapter(Context context, int resource, List<Stop> objects) {
        super(context, resource, objects);
    }

    public @Nullable Stop getStopById(String stopId) {
        for (int i = 0; i < getCount(); i++)
            if (getItem(i).getStopId().equals(stopId))
                return getItem(i);
        return null;
    }

}
