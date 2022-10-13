package it.units.simandroid.progetto.fragments.directions;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import it.units.simandroid.progetto.R;

public class RegistrationFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionRegistrationFragmentToTripsFragment() {
        return new ActionOnlyNavDirections(R.id.action_registrationFragment_to_tripsFragment);
    }
}
