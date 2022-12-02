package it.units.simandroid.progetto.viewmodels;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.viewmodels.TripsViewModel.USERS;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.units.simandroid.progetto.User;

public class UsersViewModel extends ViewModel {
    public static final String GET_USERS_TAG = "GET_USERS";
    private final FirebaseDatabase database;
    private final MutableLiveData<List<User>> databaseUsers;
    private final ValueEventListener listener;

    public UsersViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseUsers = new MutableLiveData<>();
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {
                };
                Map<String, Object> usersById = snapshot.getValue(type);
                if (usersById != null) {
                    List<User> users = new ArrayList<>(usersById.size());
                    for (String userKey : usersById.keySet()) {
                        DataSnapshot userSnapshot = snapshot.child(userKey);
                        User databaseUser = userSnapshot.getValue(User.class);
                        users.add(databaseUser);
                    }
                    databaseUsers.setValue(users);
                } else {
                    Log.i(GET_USERS_TAG, "No users found in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(GET_USERS_TAG, "Unable to retrieve users from database: " + error.getMessage());
            }
        };
        database.getReference(USERS).addValueEventListener(listener);
    }

    public LiveData<List<User>> getUsers() {
        return databaseUsers;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        database.getReference(USERS).removeEventListener(listener);
    }
}
