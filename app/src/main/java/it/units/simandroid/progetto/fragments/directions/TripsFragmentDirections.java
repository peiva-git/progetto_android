package it.units.simandroid.progetto.fragments.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.simandroid.progetto.R;

public class TripsFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionTripsFragmentToNewTripFragment() {
        return new ActionOnlyNavDirections(R.id.action_tripsFragment_to_newTripFragment);
    }

    @NonNull
    @Contract(" -> new")
    public static NavDirections actionTripsFragmentToLoginFragment() {
        return new ActionOnlyNavDirections(R.id.action_tripsFragment_to_loginFragment);
    }
}
