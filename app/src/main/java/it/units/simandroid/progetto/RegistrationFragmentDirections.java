package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

public class RegistrationFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionRegistrationFragmentToTripsFragment() {
        return new ActionOnlyNavDirections(R.id.action_registrationFragment_to_tripsFragment);
    }
}
