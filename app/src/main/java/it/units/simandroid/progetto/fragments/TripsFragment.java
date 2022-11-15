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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.adapters.TripAdapter;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;


public class TripsFragment extends Fragment {

    private FirebaseAuth authentication;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private List<Trip> trips;
    private RecyclerView tripsRecyclerView;
    private FloatingActionButton newTripButton;
    private TripAdapter tripAdapter;

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
        tripsRecyclerView = fragmentView.findViewById(R.id.trips_recycler_view);
        newTripButton = fragmentView.findViewById(R.id.new_trip_button);

        tripAdapter = new TripAdapter(getContext(), Collections.emptyList());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        tripsRecyclerView.setLayoutManager(linearLayoutManager);
        tripsRecyclerView.setAdapter(tripAdapter);

        newTripButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment());
        });

        database.getReference("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {
                };
                Map<String, Object> tripsByKey = snapshot.getValue(type);
                if (tripsByKey != null) {
                    trips = new ArrayList<>();
                    for (String key : tripsByKey.keySet()) {
                        DataSnapshot tripSnapshot = snapshot.child(key);
                        Trip trip = tripSnapshot.getValue(Trip.class);
                        boolean isFavoritesFilteringEnabled = TripsFragmentArgs.fromBundle(requireArguments()).isFilteringActive();
                        boolean isSharedTripsModeOn = TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive();
                        boolean isTripFavorite = trip.isFavorite();
                        if (isSharedTripsModeOn) {
                            addTripIfUserAuthorized(trip);
                        } else {
                            if (isFavoritesFilteringEnabled) {
                                if (isTripFavorite) {
                                    addTripIfCurrentUserOwner(trip);
                                }
                            } else {
                                addTripIfCurrentUserOwner(trip);
                            }
                        }
                    }
                    tripAdapter.updateTrips(trips);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(DB_ERROR, "Trips' value update canceled: " + error.getMessage());
            }
        });
        return fragmentView;
    }

    private void addTripIfUserAuthorized(@NonNull Trip trip) {
        if (trip.getAuthorizedUsers() == null) {
            return;
        }
        if (trip.getAuthorizedUsers().contains(authentication.getUid())) {
            trips.add(trip);
            Log.d(GET_DB_TRIPS, "Trip with id " + trip.getId() + " added to list");
        }
    }

    private void addTripIfCurrentUserOwner(@NonNull Trip trip) {
        if (trip.getOwnerId().equals(authentication.getUid())) {
            trips.add(trip);
        }
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