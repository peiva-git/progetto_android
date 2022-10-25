package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.RealtimeDatabase.NEW_TRIP_DB_TAG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;

public class NewTripFragment extends Fragment {

    public static final String NEW_TRIP_TAG = "TRIP_PUT";
    public static final String START_DATE_PICKER_TAG = "START_DATE_PICKER";
    public static final String END_DATE_PICKER_TAG = "END_DATE_PICKER";
    private ActivityResultLauncher<String[]> pickTripImages;
    public static final String IMAGE_PICKER_DEBUG_TAG = "IMG_PICK";
    private FirebaseStorage storage;
    private int numberOfTrips;
    private FirebaseAuth authentication;
    private List<Uri> pickedImages = Collections.emptyList();
    private ImageButton newImageButton;
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
        // Using OpenMultipleDocuments instead of GetMultipleContents, to obtain persistable uris
        pickTripImages = registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
            if (!uris.isEmpty()) {
                Log.d(IMAGE_PICKER_DEBUG_TAG, "Picked " + uris.size() + " images");
                pickedImages = uris;
                for (Uri uri : uris) {
                    requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
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
        tripName = fragmentView.findViewById(R.id.trip_name);
        tripDestination = fragmentView.findViewById(R.id.trip_destination);
        tripStartDate = fragmentView.findViewById(R.id.trip_start_date);
        tripEndDate = fragmentView.findViewById(R.id.trip_end_date);
        tripDescription = fragmentView.findViewById(R.id.trip_description);
        progressIndicator = fragmentView.findViewById(R.id.new_trip_progress_indicator);

        newImageButton.setOnClickListener(view -> {
            pickTripImages.launch(new String[]{"image/*"});
        });

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem item = menu.findItem(R.id.user_account);
                item.setVisible(false);
                menuInflater.inflate(R.menu.top_app_bar_new_trip, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.save_trip) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    uploadNewTripData();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        tripStartDate.setOnClickListener(view -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.date_picker_start)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                tripStartDate.setText(DateFormat.getDateInstance().format(date));
            });
            datePicker.show(requireActivity().getSupportFragmentManager(), START_DATE_PICKER_TAG);
        });

        tripEndDate.setOnClickListener(view -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.date_picker_end)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                tripEndDate.setText(DateFormat.getDateInstance().format(date));
            });
            datePicker.show(requireActivity().getSupportFragmentManager(), END_DATE_PICKER_TAG);
        });

        database.getReference("trips").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Object>> type = new GenericTypeIndicator<List<Object>>() {
                };
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
                Snackbar.make(requireView(), R.string.trip_images_uploaded, Snackbar.LENGTH_SHORT).show();
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