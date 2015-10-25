package com.middleendien.middrides.utils;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.middleendien.middrides.R;
import com.middleendien.middrides.models.Location;


import java.util.ArrayList;

/**
 * Created by Nosagie on 10/23/15.
 */
public class LocationSelectDialogFragment extends DialogFragment {


    private ArrayList<Location> locationArrayList = new ArrayList<>();
    private android.location.Location Ulocation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_location, container, false);
        final ListView locationsList = (ListView)v.findViewById(R.id.locationListView);
        Button selectNearestStop = (Button)v.findViewById(R.id.chooseNearestStopButton);
        selectNearestStop.setEnabled(false); //TODO: Implement finding nearest location if team agrees

        getDialog().setTitle("Select Location");


        //INITIALIZE LIST OF LOCATIONS NOTE THAT LOT LATS AND LONGS MAY NOT BE ACCURATE
        locationArrayList.add(new Location("Adirondack Circle", 44.010250, -73.179967));
        locationArrayList.add(new Location("R Lot", 44.012077, -73.176455));
        locationArrayList.add(new Location("Robert A Jones' 59 House",44.008026,-73.180564));
        locationArrayList.add(new Location("Track Lot/KDR",44.015337,-73.167340));
        locationArrayList.add(new Location("T Lot",44.012077,-73.176455));
        locationArrayList.add(new Location("McCullough Student Center",44.008295,-73.177213));
        locationArrayList.add(new Location("E Lot",44.012077,-73.176455));
        locationArrayList.add(new Location("Q Lot", 44.012077, -73.176455));
        locationArrayList.add(new Location("Frog Hollow", 44.013340, -73.169148));


        //Set adapter;
        ArrayAdapter<Location> locationsArrayListAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_activated_1,locationArrayList);
        locationsList.setAdapter(locationsArrayListAdapter);

        locationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location locationSelected = (Location) locationsList.getItemAtPosition(position);

                SelectLocationDialogListener mainScreenActivity = (SelectLocationDialogListener) getActivity();
                mainScreenActivity.onLocationSelected(locationSelected);

                dismiss();
            }
        });



        return v;
    }


    //Interface for caller activity
    public interface SelectLocationDialogListener{
        void onLocationSelected(Location locationSelected);
    }


}
