package it.units.simandroid.progetto.fragments.directions;

import androidx.navigation.ActionOnlyNavDirections;
import androidx.navigation.NavDirections;

import it.units.simandroid.progetto.R;

public class TripContentFragmentDirections {
    public static NavDirections actionTripContentFragmentToSelectUsersFragment() {
        return new ActionOnlyNavDirections(R.id.action_tripContentFragment_to_selectUsersDialogFragment);
    }
}
