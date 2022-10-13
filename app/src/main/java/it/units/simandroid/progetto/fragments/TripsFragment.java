package it.units.simandroid.progetto.fragments;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
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

    public static final String TRIP_GET_TAG = "TRIP_GET";
    private FirebaseAuth authentication;
    private FirebaseStorage storage;
    private List<Trip> trips;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        trips = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_trips, container, false);
        RecyclerView tripsRecyclerView = fragmentView.findViewById(R.id.trips_recycler_view);
        FloatingActionButton newTripButton = fragmentView.findViewById(R.id.new_trip_button);

        newTripButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment());
        });

        downloadStoredTrips();
        TripAdapter tripAdapter = new TripAdapter(getContext(), trips);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        tripsRecyclerView.setLayoutManager(linearLayoutManager);
        tripsRecyclerView.setAdapter(tripAdapter);

        return fragmentView;
    }

    @NonNull
    private void downloadStoredTrips() {
        StorageReference tripsReference = storage.getReference().child("users/" + authentication.getUid() + "/trips");
        tripsReference.listAll().addOnSuccessListener(taskSnapshot -> {
            for (StorageReference tripReference : taskSnapshot.getPrefixes()) {
                Trip retrievedTrip = new Trip();
                tripReference.child(NewTripFragment.NAME_STORAGE_REFERENCE).getStream()
                        .addOnSuccessListener(nameTask -> {
                            try {
                                String value = getRemoteUTF_8StoredValueFromStream(nameTask.getStream());
                                retrievedTrip.setName(value);
                            } catch (IOException e) {
                                Log.e(TRIP_GET_TAG, "Failed to read from trip name stream: " + e.getMessage());
                            }
                        }).addOnFailureListener(exception -> {
                            Log.e(TRIP_GET_TAG, "Failed to retrieve " + tripReference + " name: " + exception.getMessage());
                        });
                tripReference.child(NewTripFragment.DESTINATION_STORAGE_REFERENCE).getStream()
                        .addOnSuccessListener(destinationTask -> {
                            try {
                                String value = getRemoteUTF_8StoredValueFromStream(destinationTask.getStream());
                                retrievedTrip.setDestination(value);
                            } catch (IOException e) {
                                Log.e(TRIP_GET_TAG, "Failed to read from trip destination stream: " + e.getMessage());
                            }
                        }).addOnFailureListener(exception -> {
                            Log.e(TRIP_GET_TAG, "Failed to retrieve " + tripReference + " destination: " + exception.getMessage());
                        });
                tripReference.child(NewTripFragment.START_DATE_STORAGE_REFERENCE).getStream()
                        .addOnSuccessListener(startDateTask -> {
                            try {
                                String value = getRemoteUTF_8StoredValueFromStream(startDateTask.getStream());
                                retrievedTrip.setStartDate(value);
                            } catch (IOException e) {
                                Log.e(TRIP_GET_TAG, "Failed to read from trip start date stream: " + e.getMessage());
                            }
                        }).addOnFailureListener(exception -> {
                            Log.e(TRIP_GET_TAG, "Failed to retrieve " + tripReference + " start date: " + exception.getMessage());
                        });
                tripReference.child(NewTripFragment.END_DATE_STORAGE_REFERENCE).getStream()
                        .addOnSuccessListener(endDateTask -> {
                            try {
                                String value = getRemoteUTF_8StoredValueFromStream(endDateTask.getStream());
                                retrievedTrip.setEndDate(value);
                            } catch (IOException e) {
                                Log.e(TRIP_GET_TAG, "Failed to read from trip end date stream: " + e.getMessage());
                            }
                        }).addOnFailureListener(exception -> {
                            Log.e(TRIP_GET_TAG, "Failed to retrieve " + tripReference + " end date: " + exception.getMessage());
                        });
                tripReference.child(NewTripFragment.DESCRIPTION_STORAGE_REFERENCE).getStream()
                        .addOnSuccessListener(descriptionTask -> {
                            try {
                                String value = getRemoteUTF_8StoredValueFromStream(descriptionTask.getStream());
                                retrievedTrip.setDescription(value);
                            } catch (IOException e) {
                                Log.e(TRIP_GET_TAG, "Failed to read from trip description stream: " + e.getMessage());
                            }
                        }).addOnFailureListener(exception -> {
                            Log.e(TRIP_GET_TAG, "Failed to retrieve " + tripReference + " description: " + exception.getMessage());
                        });
                trips.add(retrievedTrip);
            }
        }).addOnFailureListener(exception -> {
            Log.e(TRIP_GET_TAG, "Failed to list trip references: " + exception.getMessage());
        });
    }

    private String getRemoteUTF_8StoredValueFromStream(InputStream stream) throws IOException {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder output = new StringBuilder();
        Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            output.append(buffer, 0, numRead);
        }
        return output.toString();
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