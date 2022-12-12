package it.units.simandroid.progetto.adapters;

import android.widget.CompoundButton;

public interface OnFavoriteStateChangedListener {
    void onFavoriteStateChanged(int position, CompoundButton compoundButton, boolean isChecked);
}
