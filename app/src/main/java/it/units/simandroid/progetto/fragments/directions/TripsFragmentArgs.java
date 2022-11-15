package it.units.simandroid.progetto.fragments.directions;

import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.FilterTripsAction.FAVORITE_TRIP_KEY;
import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.FilterTripsAction.SHARED_TRIPS_KEY;

import android.os.Bundle;

public class TripsFragmentArgs {
    public static TripsFragmentDirections.FilterTripsAction fromBundle(Bundle bundle) {
        return new TripsFragmentDirections.FilterTripsAction(bundle.getBoolean(FAVORITE_TRIP_KEY), bundle.getBoolean(SHARED_TRIPS_KEY));
    }
}
