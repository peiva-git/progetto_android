package it.units.simandroid.progetto.adapters;

import android.widget.CompoundButton;

public interface OnUserClickListener {
    void onUserClick(int position);
    void onUserCheckedStateChanged(int position, CompoundButton compoundButton, boolean isChecked);
}
