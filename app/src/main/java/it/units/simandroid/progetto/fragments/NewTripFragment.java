package it.units.simandroid.progetto.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import it.units.simandroid.progetto.R;

public class NewTripFragment extends Fragment {

    public static final String NEW_TRIP_TAG = "TRIP_PUT";
    public static final String LOCAL_STORAGE_TAG = "TRIPS_NUM";
    public static final String NUMBER_OF_TRIPS_FILE_KEY = "it.units.simandroid.progetto.NUMBER_OF_TRIPS";
    public static final String NAME_STORAGE_REFERENCE = "name";
    public static final String DESTINATION_STORAGE_REFERENCE = "destination";
    public static final String START_DATE_STORAGE_REFERENCE = "startDate";
    public static final String END_DATE_STORAGE_REFERENCE = "endDate";
    public static final String DESCRIPTION_STORAGE_REFERENCE = "description";
    private ActivityResultLauncher<String> pickTripImages;
    public static final int MAX_NUMBER_OF_IMAGES = 10;
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

        if (getNumberOfTripsForCurrentUser() == -1) {
            // unable to find locally stored number of trips, app was previously uninstalled
            // get number of trips from remote storage
            StorageReference tripsReference = storage.getReference().child("users/" + authentication.getUid() + "/trips");
            tripsReference.listAll().addOnSuccessListener(taskSnapshot -> {
                setNumberOfTripsForCurrentUser(taskSnapshot.getPrefixes().size());
                Log.d(LOCAL_STORAGE_TAG, "Number of user trips fetched remotely: " + taskSnapshot.getPrefixes().size());
            }).addOnFailureListener(exception -> {
                setNumberOfTripsForCurrentUser(0);
                Log.e(LOCAL_STORAGE_TAG, "Failed to fetch number of trips remotely: " + exception.getMessage() + "\nSet the number back to 0");
            });
        }
    }

    private int getNumberOfTripsForCurrentUser() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(NUMBER_OF_TRIPS_FILE_KEY, Context.MODE_PRIVATE);
        int numberOfTrips = sharedPreferences.getInt(String.valueOf(authentication.getUid()), -1);
        Log.d(LOCAL_STORAGE_TAG, "Current number of user trips is: " + numberOfTrips);
        return numberOfTrips;
    }

    private void setNumberOfTripsForCurrentUser(int numberOfTrips) {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences(NUMBER_OF_TRIPS_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(authentication.getUid(), numberOfTrips);
        editor.apply();
        Log.d(LOCAL_STORAGE_TAG, "Set new number of user trips");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_new_trip, container, false);
        newImageButton = fragmentView.findViewById(R.id.trip_images_button);
        saveNewTripButton = fragmentView.findViewById(R.id.save_new_trip_button);
        tripName = fragmentView.findViewById(R.id.trip_name);
        tripDestination = fragmentView.findViewById(R.id.trip_destination);
        tripStartDate = fragmentView.findViewById(R.id.trip_start_date);
        tripEndDate = fragmentView.findViewById(R.id.trip_end_date);
        tripDescription = fragmentView.findViewById(R.id.trip_description);

        newImageButton.setOnClickListener(view -> {
            pickTripImages.launch("image/*");
        });

        saveNewTripButton.setOnClickListener(view -> {
            String userId = authentication.getUid();
            StorageReference userStorageReference = storage.getReference().child("users/" + userId);
            StorageReference tripsReference = userStorageReference.child("trips");

            int numberOfTrips = getNumberOfTripsForCurrentUser();
            StorageReference newTripReference = tripsReference.child("trip_" + numberOfTrips);
            uploadNewTripData(newTripReference);
            setNumberOfTripsForCurrentUser(++numberOfTrips);
        });

        return fragmentView;
    }

    private void uploadNewTripData(StorageReference newTripReference) {
        for (Uri tripImage : pickedImages) {
            StorageReference newTripImageReference = newTripReference.child(tripImage.getLastPathSegment());
            newTripImageReference.putFile(tripImage).addOnSuccessListener(taskSnapshot -> {
                Log.d(NEW_TRIP_TAG, "Image added to firecloud storage");
            }).addOnFailureListener(exception -> {
                Log.e(NEW_TRIP_TAG, "Failed to load image: " + exception.getMessage());
            });
        }
        newTripReference.child(NAME_STORAGE_REFERENCE).putBytes(tripName.getText().toString().getBytes(StandardCharsets.UTF_8))
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(NEW_TRIP_TAG, "Trip name added to firecould storage");
                }).addOnFailureListener(exception -> {
                    Log.e(NEW_TRIP_TAG, "Failed to load trip name: " + exception.getMessage());
                });
        newTripReference.child(DESTINATION_STORAGE_REFERENCE).putBytes(tripDestination.getText().toString().getBytes(StandardCharsets.UTF_8))
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(NEW_TRIP_TAG, "Trip destination added to firecloud storage");
                }).addOnFailureListener(exception -> {
                    Log.e(NEW_TRIP_TAG, "Failed to add trip destination: " + exception.getMessage());
                });
        newTripReference.child(START_DATE_STORAGE_REFERENCE).putBytes(tripStartDate.getText().toString().getBytes(StandardCharsets.UTF_8))
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(NEW_TRIP_TAG, "Trip start date added to firecloud storage");
                }).addOnFailureListener(exception -> {
                    Log.e(NEW_TRIP_TAG, "Failed to add trip start date: " + exception.getMessage());
                });
        newTripReference.child(END_DATE_STORAGE_REFERENCE).putBytes(tripEndDate.getText().toString().getBytes(StandardCharsets.UTF_8))
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(NEW_TRIP_TAG, "Trip end date added to firecloud storage");
                }).addOnFailureListener(exception -> {
                    Log.e(NEW_TRIP_TAG, "Failed to add trip end date: " + exception.getMessage());
                });
        newTripReference.child(DESCRIPTION_STORAGE_REFERENCE).putBytes(tripDescription.getText().toString().getBytes(StandardCharsets.UTF_8))
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(NEW_TRIP_TAG, "Trip description added to firecloud storage");
                }).addOnFailureListener(exception -> {
                    Log.e(NEW_TRIP_TAG, "Failed to add trip description: " + exception.getMessage());
                });
    }
}