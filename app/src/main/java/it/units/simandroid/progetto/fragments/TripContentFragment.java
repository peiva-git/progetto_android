package it.units.simandroid.progetto.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentArgs;

public class TripContentFragment extends Fragment {

    public TripContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Trip trip = TripContentFragmentArgs.fromBundle(requireArguments()).getTrip();
        return inflater.inflate(R.layout.fragment_trip_content, container, false);
    }
}