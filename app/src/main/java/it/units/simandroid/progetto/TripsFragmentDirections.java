package it.units.simandroid.progetto;

import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

public class TripsFragmentDirections {
    public static NavDirections actionTripsFragmentToNewTripFragment() {
        return new ActionOnlyNavDirections(R.id.action_tripsFragment_to_newTripFragment);
    }
}
