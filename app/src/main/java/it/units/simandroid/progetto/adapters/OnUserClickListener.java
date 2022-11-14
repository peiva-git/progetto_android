package it.units.simandroid.progetto.adapters;

import android.widget.CompoundButton;

import it.units.simandroid.progetto.User;

public interface OnUserClickListener {
    void onUserClick(User user);
    void onUserCheckedStateChanged(User user, CompoundButton compoundButton, boolean isChecked);
}
