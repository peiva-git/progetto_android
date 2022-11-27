package it.units.simandroid.progetto.fragments.directions;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

import it.units.simandroid.progetto.R;

public class TripContentFragmentDirections {

    @NonNull
    @Contract(value = " -> new", pure = true)
    public static ShareTripAction actionShareTrip() {
        return new ShareTripAction();
    }

    public static class ShareTripAction implements NavDirections {

        private String tripId;
        public final static String TRIP_ID_KEY = "TRIP_ID";

        public ShareTripAction(String tripId) {
            this.tripId = tripId;
        }

        public ShareTripAction() {}

        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        @Override
        public int getActionId() {
            return R.id.action_tripContentFragment_to_selectUsersFragment;
        }

        @NonNull
        @Override
        public Bundle getArguments() {
            Bundle bundle = new Bundle();
            bundle.putString(TRIP_ID_KEY, tripId);
            return bundle;
        }
    }
}
