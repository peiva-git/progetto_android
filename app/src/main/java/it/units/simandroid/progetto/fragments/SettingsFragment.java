package it.units.simandroid.progetto.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import it.units.simandroid.progetto.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }
}