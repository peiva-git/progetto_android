package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import it.units.simandroid.progetto.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);
        EditTextPreference namePreference = findPreference("user_name");
        EditTextPreference surnamePreference = findPreference("user_surname");

        FirebaseDatabase database = FirebaseDatabase.getInstance(DB_URL);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Task<DataSnapshot> task = database.getReference("users")
                .child(Objects.requireNonNull(auth.getUid()))
                .get();
        task.addOnCompleteListener(taskCompleted -> {
            DataSnapshot snapshot = taskCompleted.getResult();
            String userName = snapshot.child("name").getValue(String.class);
            String userSurname = snapshot.child("surname").getValue(String.class);

            Objects.requireNonNull(namePreference).setText(userName);
            Objects.requireNonNull(surnamePreference).setText(userSurname);
        });
    }
}