package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_ERROR;
import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.RealtimeDatabase.GET_DB_TRIPS;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.TripAdapter;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;


public class TripsFragment extends Fragment {

    private FirebaseAuth authentication;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance(DB_URL);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_trips, container, false);
        RecyclerView tripsRecyclerView = fragmentView.findViewById(R.id.trips_recycler_view);
        FloatingActionButton newTripButton = fragmentView.findViewById(R.id.new_trip_button);

        newTripButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment());
        });

        database.getReference("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Object>> type = new GenericTypeIndicator<List<Object>>() {};
                snapshot.getValue(type);
                if (snapshot.getValue(type) != null) {
                    List<Trip> trips = new ArrayList<>();
                    for (int tripId = 0; tripId < snapshot.getValue(type).size(); tripId++) {
                        DataSnapshot tripSnapshot = snapshot.child(String.valueOf(tripId));
                        Trip trip = tripSnapshot.getValue(Trip.class);
                        trips.add(trip);
                        Log.d(GET_DB_TRIPS, "Trip with id " + tripId + " added to list");
                    }
                    TripAdapter tripAdapter = new TripAdapter(getContext(), trips);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                    tripsRecyclerView.setLayoutManager(linearLayoutManager);
                    tripsRecyclerView.setAdapter(tripAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(DB_ERROR, "Trips' value update canceled: " + error.getMessage());
            }
        });
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = authentication.getCurrentUser();
        if (currentUser == null) {
            NavHostFragment.findNavController(this)
                    .navigate(TripsFragmentDirections.actionTripsFragmentToLoginFragment());
        }
    }
}