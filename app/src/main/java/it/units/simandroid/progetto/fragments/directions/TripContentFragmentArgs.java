package it.units.simandroid.progetto.fragments.directions;

import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.ViewTripDetailsAction.SHARED_TRIPS_KEY;
import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.ViewTripDetailsAction.TRIP_KEY;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import it.units.simandroid.progetto.Trip;

public class TripContentFragmentArgs {
    @NonNull
    public static TripsFragmentDirections.ViewTripDetailsAction fromBundle(@NonNull Bundle bundle) {
        String tripJsonString = bundle.getString(TRIP_KEY);
        boolean isSharedTripsModeActive = bundle.getBoolean(SHARED_TRIPS_KEY);
        Gson gson = new Gson();
        Trip trip = gson.fromJson(tripJsonString, Trip.class);
        return new TripsFragmentDirections.ViewTripDetailsAction(trip, isSharedTripsModeActive);
    }
}
