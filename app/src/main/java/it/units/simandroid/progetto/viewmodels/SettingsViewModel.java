package it.units.simandroid.progetto.viewmodels;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.viewmodels.TripsViewModel.USERS;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SettingsViewModel extends ViewModel {
    public static final String USERNAME_FIELD_NAME = "name";
    public static final String SURNAME_FIELD_NAME = "surname";
    private final FirebaseDatabase database;
    private final FirebaseAuth authentication;
    private final MutableLiveData<String> userName;
    private final MutableLiveData<String> userSurname;
    private final ValueEventListener listener;

    public SettingsViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        authentication = FirebaseAuth.getInstance();

        userName = new MutableLiveData<>();
        userSurname = new MutableLiveData<>();

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userName.setValue(snapshot
                        .child(USERNAME_FIELD_NAME)
                        .getValue(String.class));
                userSurname.setValue(snapshot
                        .child(SURNAME_FIELD_NAME)
                        .getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GET_SETTINGS", "Unable to retrieve current user from database: " + error.getMessage());
            }
        };
        database.getReference(USERS).child(Objects.requireNonNull(authentication.getUid())).addValueEventListener(listener);
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<String> getUserSurname() {
        return userSurname;
    }

    public Task<Void> setUserName(String userName) {
        return database.getReference(USERS)
                .child(Objects.requireNonNull(authentication.getUid()))
                .child(USERNAME_FIELD_NAME)
                .setValue(userName);
    }

    public Task<Void> setUserSurname(String userSurname) {
        return database.getReference(USERS)
                .child(Objects.requireNonNull(authentication.getUid()))
                .child(SURNAME_FIELD_NAME)
                .setValue(userSurname);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(USERS).child(Objects.requireNonNull(authentication.getUid())).removeEventListener(listener);
    }
}
