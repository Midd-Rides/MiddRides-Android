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
 *
 * This dialog presents a list of stops for the user to choose from
 */
public class LocationSelectDialogFragment extends DialogFragment {


    //List of MiddRide stops
    private ArrayList<Location> vanStopLocationsList = new ArrayList<Location>();

    private ListView locationsListView;
    private Button selectNearestStop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_select_location, container, false);
        getDialog().setTitle("Select Location");


        //Initialize buttons and list view
        locationsListView = (ListView)view.findViewById(R.id.locationListView);
        selectNearestStop = (Button)view.findViewById(R.id.chooseNearestStopButton);
        selectNearestStop.setEnabled(false); //TODO: Implement finding nearest location if team agrees


        //Initialize list of locations
        initializeLocationsList();


        //Populate List View using adapter
        configureAdapter();



        return view;
    }

    private void initializeLocationsList(){
        vanStopLocationsList.add(new Location("Adirondack Circle", 44.010250, -73.179967));
        vanStopLocationsList.add(new Location("R Lot", 44.012077, -73.176455));
        vanStopLocationsList.add(new Location("Robert A Jones' 59 House", 44.008026, -73.180564));
        vanStopLocationsList.add(new Location("Track Lot/KDR", 44.015337, -73.167340));
        vanStopLocationsList.add(new Location("T Lot", 44.012077, -73.176455));
        vanStopLocationsList.add(new Location("McCullough Student Center", 44.008295, -73.177213));
        vanStopLocationsList.add(new Location("E Lot", 44.012077, -73.176455));
        vanStopLocationsList.add(new Location("Q Lot", 44.012077, -73.176455));
        vanStopLocationsList.add(new Location("Frog Hollow", 44.013340, -73.169148));
    }

    //Configures adapter and sets Item click listener for List
    private void configureAdapter(){
        ArrayAdapter<Location> locationsArrayListAdapter = new ArrayAdapter<Location>(getActivity(),android.R.layout.simple_list_item_activated_1, vanStopLocationsList);
        locationsListView.setAdapter(locationsArrayListAdapter);

        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location locationSelected = (Location) locationsListView.getItemAtPosition(position);

                //Return selected Location to caller Activity and dismiss dialog
                SelectLocationDialogListener mainScreenActivity = (SelectLocationDialogListener) getActivity();
                mainScreenActivity.onLocationSelected(locationSelected);

                dismiss();
            }
        });
    }

    //Interface for caller activity
    public interface SelectLocationDialogListener{
        void onLocationSelected(Location locationSelected);
    }


}
