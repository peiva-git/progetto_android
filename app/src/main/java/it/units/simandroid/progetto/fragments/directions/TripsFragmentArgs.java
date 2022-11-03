package it.units.simandroid.progetto.fragments.directions;

import static it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections.FilterByFavoriteTripsAction.FAVORITE_TRIP_KEY;

import android.os.Bundle;

public class TripsFragmentArgs {
    public static TripsFragmentDirections.FilterByFavoriteTripsAction fromBundle(Bundle bundle) {
        return new TripsFragmentDirections.FilterByFavoriteTripsAction(bundle.getBoolean(FAVORITE_TRIP_KEY));
    }
}
