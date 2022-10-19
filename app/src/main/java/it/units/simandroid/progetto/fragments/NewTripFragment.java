package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.RealtimeDatabase.NEW_TRIP_DB_TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;

public class NewTripFragment extends Fragment {

    public static final String NEW_TRIP_TAG = "TRIP_PUT";
    private ActivityResultLauncher<String> pickTripImages;
    public static final String IMAGE_PICKER_DEBUG_TAG = "IMG_PICK";
    private FirebaseStorage storage;
    private int numberOfTrips;
    private FirebaseAuth authentication;
    private List<Uri> pickedImages = Collections.emptyList();
    private ImageButton newImageButton;
    private FloatingActionButton saveNewTripButton;
    private EditText tripName;
    private EditText tripDestination;
    private EditText tripStartDate;
    private EditText tripEndDate;
    private EditText tripDescription;
    private FirebaseDatabase database;
    private LinearProgressIndicator progressIndicator;

    public NewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        authentication = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(DB_URL);

        // PickMultipleVisualMedia contract seems to be buggy, even when implemented according to docs
        pickTripImages = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (!uris.isEmpty()) {
                Log.d(IMAGE_PICKER_DEBUG_TAG, "Picked " + uris.size() + " images");
                pickedImages = uris;
            } else {
                Log.d(IMAGE_PICKER_DEBUG_TAG, "No media selected");
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_new_trip, container, false);
        newImageButton = fragmentView.findViewById(R.id.trip_images_button);
        saveNewTripButton = fragmentView.findViewById(R.id.save_new_trip_button);
        tripName = fragmentView.findViewById(R.id.trip_name);
        tripDestination = fragmentView.findViewById(R.id.trip_destination);
        tripStartDate = fragmentView.findViewById(R.id.trip_start_date);
        tripEndDate = fragmentView.findViewById(R.id.trip_end_date);
        tripDescription = fragmentView.findViewById(R.id.trip_description);
        progressIndicator = fragmentView.findViewById(R.id.new_trip_progress_indicator);

        newImageButton.setOnClickListener(view -> {
            pickTripImages.launch("image/*");
        });

        saveNewTripButton.setOnClickListener(view -> {
            progressIndicator.setVisibility(View.VISIBLE);
            uploadNewTripImages();
            uploadNewTripData();
        });

        database.getReference("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Object>> type = new GenericTypeIndicator<List<Object>>() {};
                if (snapshot.getValue(type) == null) {
                    numberOfTrips = 0;
                } else {
                    numberOfTrips = snapshot.getValue(type).size();
                }
                Log.d(NEW_TRIP_DB_TAG, "Number of trips currently in database: " + numberOfTrips);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(NEW_TRIP_DB_TAG, "Failed to update number of trips");
            }
        });

        return fragmentView;
    }

    private void uploadNewTripImages() {
        for (Uri tripImage : pickedImages) {
            StorageReference newTripImagesReference = storage.getReference().child("users/" + authentication.getUid() + "/" + numberOfTrips + "/" + tripImage.getLastPathSegment());
            newTripImagesReference.putFile(tripImage).addOnSuccessListener(taskSnapshot -> {
                Log.d(NEW_TRIP_TAG, "Image added to firecloud storage");
                progressIndicator.setVisibility(View.INVISIBLE);
            }).addOnFailureListener(exception -> {
                Log.e(NEW_TRIP_TAG, "Failed to load image: " + exception.getMessage());
                progressIndicator.setVisibility(View.INVISIBLE);
            });
        }
    }

    private void uploadNewTripData() {
        DatabaseReference tripsDbReference = database.getReference("trips").child(String.valueOf(numberOfTrips));
        String newTripName = tripName.getText().toString();
        String newTripDestination = tripDestination.getText().toString();
        String newTripStartDate = tripStartDate.getText().toString();
        String newTripEndDate = tripEndDate.getText().toString();
        String newTripDescription = tripDescription.getText().toString();
        List<String> newTripImageUris = new ArrayList<>();
        for (Uri pickedImageUri : pickedImages) {
            newTripImageUris.add(pickedImageUri.toString());
        }
        Trip newTrip = new Trip(newTripImageUris, newTripName, newTripStartDate, newTripEndDate, newTripDescription, newTripDestination);
        tripsDbReference.setValue(newTrip).addOnSuccessListener(task -> {
            Log.d(NEW_TRIP_DB_TAG, "Added new trip data to realtime DB");
        }).addOnFailureListener(exception -> {
            Log.e(NEW_TRIP_DB_TAG, "Failed to add new trip data: " + exception.getMessage());
        });
    }
}