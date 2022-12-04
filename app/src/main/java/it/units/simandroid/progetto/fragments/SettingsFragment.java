package it.units.simandroid.progetto.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.viewmodels.SettingsViewModel;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String USER_NAME_KEY = "user_name";
    public static final String USER_SURNAME_KEY = "user_surname";
    public static final String SETTINGS_TAG = "SETTINGS";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = super.onCreateView(inflater, container, savedInstanceState);

        SettingsViewModel viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        viewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            EditTextPreference namePreference = findPreference(USER_NAME_KEY);
            namePreference.setText(name);
        });
        viewModel.getUserSurname().observe(getViewLifecycleOwner(), surname -> {
            EditTextPreference surnamePreference = findPreference(USER_SURNAME_KEY);
            surnamePreference.setText(surname);
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key.equals(USER_NAME_KEY)) {
                viewModel.setUserName(sharedPreferences.getString(key, ""))
                        .addOnSuccessListener(task -> Log.i(SETTINGS_TAG, "Preference value was updated to: " + sharedPreferences.getString(key, "")))
                        .addOnFailureListener(exception -> Log.w(SETTINGS_TAG, "Couldn't update preference value " + USER_NAME_KEY, exception));
            } else if (key.equals(USER_SURNAME_KEY)) {
                viewModel.setUserSurname(sharedPreferences.getString(key, ""))
                        .addOnSuccessListener(task -> Log.i(SETTINGS_TAG, "Preference value was updated to: " + sharedPreferences.getString(key, "")))
                        .addOnFailureListener(exception -> Log.w(SETTINGS_TAG, "Couldn't update preference value " + USER_SURNAME_KEY, exception));
            } else {
                Log.w(SETTINGS_TAG, "No preference key matching");
            }
        });

        return fragmentView;
    }
}