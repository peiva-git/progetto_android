package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

public class LoginFragmentDirections {
    @NonNull
    @Contract(" -> new")
    public static NavDirections actionLoginFragmentToRegistrationFragment() {
        return new ActionOnlyNavDirections(R.id.action_loginFragment_to_registrationFragment);
    }
}
