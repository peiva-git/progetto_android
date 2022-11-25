package it.units.simandroid.progetto.fragments.directions;

import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.ViewTripDetailsAction.SHARED_TRIPS_KEY;
import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.ViewTripDetailsAction.TRIP_ID_KEY;

import android.os.Bundle;

import androidx.annotation.NonNull;

public class TripContentFragmentArgs {
    @NonNull
    public static TripsFragmentDirections.ViewTripDetailsAction fromBundle(@NonNull Bundle bundle) {
        String tripId = bundle.getString(TRIP_ID_KEY);
        boolean isSharedTripsModeActive = bundle.getBoolean(SHARED_TRIPS_KEY);
        return new TripsFragmentDirections.ViewTripDetailsAction(tripId, isSharedTripsModeActive);
    }
}
