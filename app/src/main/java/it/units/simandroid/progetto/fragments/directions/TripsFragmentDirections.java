package it.units.simandroid.progetto.fragments.directions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavAction;
import androidx.navigation.NavDirections;

import com.google.gson.Gson;

import org.jetbrains.annotations.Contract;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;

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

    public static ViewTripDetailsAction actionViewTripDetails() {
        return new ViewTripDetailsAction();
    }

    public static class ViewTripDetailsAction implements NavDirections {
        private Trip trip;
        public static final String TRIP_KEY = "TRIP";

        public ViewTripDetailsAction(Trip trip) {
            this.trip = trip;
        }

        public ViewTripDetailsAction() {}

        public void setTrip(Trip trip) {
            this.trip = trip;
        }

        public Trip getTrip() {
            return trip;
        }

        @Override
        public int getActionId() {
            return R.id.view_trip_details_action;
        }

        @NonNull
        @Override
        public Bundle getArguments() {
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            bundle.putString(TRIP_KEY, gson.toJson(trip, Trip.class));
            return bundle;
        }
    }
}
