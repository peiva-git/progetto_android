package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.RealtimeDatabase.NEW_TRIP_DB_TAG;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.fragments.directions.NewTripFragmentDirections;

public class NewTripFragment extends Fragment {

    public static final String NEW_TRIP_TAG = "TRIP_PUT";
    public static final String START_DATE_PICKER_TAG = "START_DATE_PICKER";
    public static final String END_DATE_PICKER_TAG = "END_DATE_PICKER";
    private ActivityResultLauncher<String[]> pickTripImages;
    public static final String IMAGE_PICKER_TAG = "IMG_PICK";
    private FirebaseStorage storage;
    private FirebaseAuth authentication;
    private List<Uri> pickedImages = Collections.emptyList();
    private ImageButton newImageButton;
    private EditText tripName;
    private EditText tripDestination;
    private MaterialButton tripStartDate;
    private MaterialButton tripEndDate;
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
                Log.d(IMAGE_PICKER_TAG, "Picked " + uris.size() + " images");
                pickedImages = uris;
                for (Uri uri : uris) {
                    requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                newImageButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
                newImageButton.setImageURI(uris.get(0));
            } else {
                Log.d(IMAGE_PICKER_TAG, "No media selected");
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

        newImageButton.setOnClickListener(view -> pickTripImages.launch(new String[]{"image/*"}));

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
                    NavDirections action = NewTripFragmentDirections.actionNewTripFragmentToTripsFragment();
                    Navigation.findNavController(fragmentView).navigate(action);
                    Snackbar.make(fragmentView, R.string.trip_saved, Snackbar.LENGTH_LONG).show();
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

        tripDescription.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                newImageButton.setVisibility(View.GONE);
            } else {
                newImageButton.setVisibility(View.VISIBLE);
            }
        });

        return fragmentView;
    }

    private void uploadNewTripData() {
        DatabaseReference tripsDbReference = database.getReference("trips").push();
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
        newTrip.setId(tripsDbReference.getKey());
        tripsDbReference.setValue(newTrip)
                .addOnSuccessListener(task -> Log.d(NEW_TRIP_DB_TAG, "Added new trip data to realtime DB"))
                .addOnFailureListener(exception -> Log.e(NEW_TRIP_DB_TAG, "Failed to add new trip data: " + exception.getMessage()));
    }
}