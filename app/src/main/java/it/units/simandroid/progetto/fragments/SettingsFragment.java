package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
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

public class SettingsFragment extends PreferenceFragmentCompat {

    public static final String USER_NAME_KEY = "user_name";
    public static final String USER_SURNAME_KEY = "user_surname";
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        EditTextPreference namePreference = findPreference(USER_NAME_KEY);
        EditTextPreference surnamePreference = findPreference(USER_SURNAME_KEY);

        database = FirebaseDatabase.getInstance(DB_URL);
        auth = FirebaseAuth.getInstance();
        database.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userId = Objects.requireNonNull(auth.getUid());
                String userName = snapshot
                        .child(userId)
                        .child("name")
                        .getValue(String.class);
                String userSurname = snapshot
                        .child(userId)
                        .child("surname")
                        .getValue(String.class);
                Objects.requireNonNull(namePreference).setText(userName);
                Objects.requireNonNull(surnamePreference).setText(userSurname);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SETTINGS", "Error retrieving settings: " + error.getMessage());
            }
        });
    }
}