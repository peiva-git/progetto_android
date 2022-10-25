package it.units.simandroid.progetto.fragments.directions;

import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import it.units.simandroid.progetto.R;

public class NewTripFragmentDirections {
    public static NavDirections actionNewTripFragmentToTripsFragment() {
        return new ActionOnlyNavDirections(R.id.action_newTripFragment_to_tripsFragment);
    }
}
