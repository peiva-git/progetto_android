package it.units.simandroid.progetto.adapters;

import android.widget.CompoundButton;

import it.units.simandroid.progetto.User;

public interface OnUserClickListener {
    void onUserClick(int position);
    void onUserCheckedStateChanged(int position, CompoundButton compoundButton, boolean isChecked);
}
