package it.units.simandroid.progetto.adapters;

import android.view.View;
import android.widget.CompoundButton;

import it.units.simandroid.progetto.Trip;

public interface OnTripClickListener {
    void onTripClick(int position);
    boolean onTripLongClick(int position);
}
