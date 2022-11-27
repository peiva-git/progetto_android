package it.units.simandroid.progetto.fragments.directions;

import android.os.Bundle;

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
        return new ActionOnlyNavDirections(R.id.action_global_loginFragment);
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    public static ViewTripDetailsAction actionViewTripDetails() {
        return new ViewTripDetailsAction();
    }

    @NonNull
    @Contract(value = " -> new", pure = true)
    public static FilterTripsAction actionFilterTrips() {
        return new FilterTripsAction();
    }

    public static class ViewTripDetailsAction implements NavDirections {
        public static final String SHARED_MODE_KEY = "TRIP_SHARE";
        @NonNull
        private String tripId;
        private boolean isSharedTripsModeActive = false;
        public static final String TRIP_ID_KEY = "TRIP_ID";

        public ViewTripDetailsAction(@NonNull String tripId, boolean isSharedTripsModeActive) {
            this.tripId = tripId;
            this.isSharedTripsModeActive = isSharedTripsModeActive;
        }

        private ViewTripDetailsAction() {}

        public boolean isSharedTripsModeActive() {
            return isSharedTripsModeActive;
        }

        public void setSharedTripsModeActive(boolean sharedTripsModeActive) {
            isSharedTripsModeActive = sharedTripsModeActive;
        }

        public void setTripId(@NonNull String tripId) {
            this.tripId = tripId;
        }

        @NonNull
        public String getTripId() {
            return tripId;
        }

        @Override
        public int getActionId() {
            return R.id.view_trip_details_action;
        }

        @NonNull
        @Override
        public Bundle getArguments() {
            Bundle bundle = new Bundle();
            bundle.putString(TRIP_ID_KEY, tripId);
            bundle.putBoolean(SHARED_MODE_KEY, isSharedTripsModeActive);
            return bundle;
        }
    }

    public static class FilterTripsAction implements NavDirections {
        private boolean isFilteringActive = false;
        private boolean isSharedTripsModeActive = false;
        public static final String FAVORITE_TRIP_KEY = "TRIP_FAV";
        public static final String SHARED_TRIPS_KEY = "TRIP_SHARE";

        public FilterTripsAction(boolean isFilteringActive, boolean isSharedTripsModeActive) {
            this.isFilteringActive = isFilteringActive;
            this.isSharedTripsModeActive = isSharedTripsModeActive;
        }

        public FilterTripsAction() {}

        public boolean isFilteringActive() {
            return isFilteringActive;
        }

        public void setFilteringActive(boolean filteringActive) {
            isFilteringActive = filteringActive;
        }

        public boolean isSharedTripsModeActive() {
            return isSharedTripsModeActive;
        }

        public void setSharedTripsModeActive(boolean sharedTripsModeActive) {
            isSharedTripsModeActive = sharedTripsModeActive;
        }

        @Override
        public int getActionId() {
            return R.id.action_tripsFragment_self;
        }

        @NonNull
        @Override
        public Bundle getArguments() {
            Bundle bundle = new Bundle();
            bundle.putBoolean(FAVORITE_TRIP_KEY, isFilteringActive);
            bundle.putBoolean(SHARED_TRIPS_KEY, isSharedTripsModeActive);
            return bundle;
        }
    }
}
