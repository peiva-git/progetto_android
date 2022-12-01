package it.units.simandroid.progetto.adapters;

import android.widget.CompoundButton;

import it.units.simandroid.progetto.Trip;

public interface OnFavoriteStateChangedListener {
    void onFavoriteStateChanged(int position, CompoundButton compoundButton, boolean isChecked);
}
