package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

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
