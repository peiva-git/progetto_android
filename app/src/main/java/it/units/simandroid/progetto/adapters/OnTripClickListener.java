package it.units.simandroid.progetto.adapters;

import android.widget.CompoundButton;

import it.units.simandroid.progetto.Trip;

public interface OnTripClickListener {
    void onTripClick(Trip trip);
    void onTripFavoriteStateChanged(Trip trip, CompoundButton compoundButton, boolean isChecked);
}
