package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.SettingsViewModel;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String USER_NAME_KEY = "user_name";
    public static final String USER_SURNAME_KEY = "user_surname";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

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
                viewModel.setUserName(sharedPreferences.getString(key, ""));
                Log.i("SETTINGS", "Preference value was updated to: " + sharedPreferences.getString(key, ""));
            } else if (key.equals(USER_SURNAME_KEY)) {
                viewModel.setUserSurname(sharedPreferences.getString(key, ""));
                Log.i("SETTINGS", "Preference value was updated to: " + sharedPreferences.getString(key, ""));
            } else {
                Log.w("SETTINGS", "No preference key matching");
            }
        });

    }
}