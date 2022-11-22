package it.units.simandroid.progetto;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

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

public class TripsViewModel extends ViewModel {
    private final MutableLiveData<List<Trip>> databaseTrips;
    private final FirebaseDatabase database;

    public TripsViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        databaseTrips = new MutableLiveData<>();
        database.getReference("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {};
                Map<String, Object> tripsById = snapshot.getValue(type);
                if (tripsById != null) {
                    List<Trip> trips = new ArrayList<>(tripsById.keySet().size());
                    for (String tripKey : tripsById.keySet()) {
                        DataSnapshot tripSnapshot = snapshot.child(tripKey);
                        Trip databaseTrip = tripSnapshot.getValue(Trip.class);
                        trips.add(databaseTrip);
                    }
                    databaseTrips.setValue(trips);
                } else {
                    Log.d("GET_TRIPS", "No trips found in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GET_TRIPS", "Unable to retrieve trips from database: " + error.getMessage());
            }
        });
    }

    public LiveData<List<Trip>> getTrips() {
        return databaseTrips;
    }

}
