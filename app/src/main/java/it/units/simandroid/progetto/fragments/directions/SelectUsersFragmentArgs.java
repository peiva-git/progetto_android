package it.units.simandroid.progetto.fragments.directions;

import static it.units.simandroid.progetto.fragments.directions.TripContentFragmentDirections.ShareTripAction.TRIP_ID_KEY;

import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public class SelectUsersFragmentArgs {
    @NonNull
    @Contract("_ -> new")
    public static TripContentFragmentDirections.ShareTripAction fromBundle(Bundle bundle) {
        return new TripContentFragmentDirections.ShareTripAction(bundle.getString(TRIP_ID_KEY));
    }
}
