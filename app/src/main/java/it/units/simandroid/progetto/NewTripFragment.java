package it.units.simandroid.progetto;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NewTripFragment extends Fragment {

    public static final String NEW_TRIP_TAG = "TRIP_PUT";
    private ActivityResultLauncher<String> pickTripImages;
    public static final int MAX_NUMBER_OF_IMAGES = 10;
    public static final String IMAGE_PICKER_DEBUG_TAG = "IMG_PICK";
    private FirebaseStorage storage;
    private int numberOfTrips;
    private FirebaseAuth authentication;
    private List<Uri> pickedImages = Collections.emptyList();

    public NewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        authentication = FirebaseAuth.getInstance();

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

    private int getNumberOfTripsForCurrentUser() {
        AtomicInteger numberOfTrips = new AtomicInteger();
        StorageReference tripsReference = storage.getReference()
                .child("users/" + authentication.getUid() + "/trips");
        tripsReference.listAll().addOnSuccessListener(taskSnapshot -> {
            numberOfTrips.set(taskSnapshot.getItems().size());
        }).addOnFailureListener(exception -> {
            Log.e(IMAGE_PICKER_DEBUG_TAG, "Failed to retrieve number of trips for current user: " + exception.getMessage());
        });
        return numberOfTrips.get();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_new_trip, container, false);
        ImageButton newImageButton = fragmentView.findViewById(R.id.trip_images_button);
        FloatingActionButton saveNewTripButton = fragmentView.findViewById(R.id.save_new_trip_button);
        EditText tripName = fragmentView.findViewById(R.id.trip_name);
        EditText tripDestination = fragmentView.findViewById(R.id.trip_destination);
        EditText tripStartDate = fragmentView.findViewById(R.id.trip_start_date);
        EditText tripEndDate = fragmentView.findViewById(R.id.trip_end_date);
        EditText tripDescription = fragmentView.findViewById(R.id.trip_description);

        newImageButton.setOnClickListener(view -> {
            pickTripImages.launch("image/*");
        });

        saveNewTripButton.setOnClickListener(view -> {
            String userId = authentication.getUid();
            StorageReference userStorageReference = storage.getReference().child("users/" + userId);
            StorageReference tripsReference = userStorageReference.child("trips");

            int numberOfTrips = getNumberOfTripsForCurrentUser();
            StorageReference newTripReference = tripsReference.child("trip_" + numberOfTrips);

            for (Uri tripImage : pickedImages) {
                StorageReference newTripImageReference = newTripReference.child(tripImage.getLastPathSegment());
                newTripImageReference.putFile(tripImage).addOnSuccessListener(taskSnapshot -> {
                    Log.d(NEW_TRIP_TAG, "Image added to firecloud storage");
                }).addOnFailureListener(exception -> {
                    Log.e(NEW_TRIP_TAG, "Failed to load image: " + exception.getMessage());
                });
            }
            newTripReference.child("name").putBytes(tripName.getText().toString().getBytes(StandardCharsets.UTF_8))
                    .addOnSuccessListener(taskSnapshot -> {
                        Log.d(NEW_TRIP_TAG, "Trip name added to firecould storage");
                    }).addOnFailureListener(exception -> {
                        Log.e(NEW_TRIP_TAG, "Failed to load trip name: " + exception.getMessage());
                    });
        });

        return fragmentView;
    }
}