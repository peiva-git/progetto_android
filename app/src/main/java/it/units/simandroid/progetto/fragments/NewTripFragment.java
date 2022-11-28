package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.TripsViewModel;

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
    private FloatingActionButton saveTripButton;

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
                    NewTripFragment.this.requireContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        saveTripButton = fragmentView.findViewById(R.id.save_new_trip_button);

        newImageButton.setOnClickListener(view -> pickTripImages.launch(new String[]{"image/*"}));

        tripStartDate.setOnClickListener(view -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.date_picker_start)
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                tripStartDate.setText(DateFormat.getDateInstance().format(date));
            });
            datePicker.show(NewTripFragment.this.requireActivity().getSupportFragmentManager(), START_DATE_PICKER_TAG);
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
            datePicker.show(NewTripFragment.this.requireActivity().getSupportFragmentManager(), END_DATE_PICKER_TAG);
        });

        saveTripButton.setOnClickListener(view -> {
            uploadNewTripData();
            NavHostFragment.findNavController(this).navigateUp();
            Snackbar.make(NewTripFragment.this.requireActivity().findViewById(R.id.activity_layout), R.string.trip_saved, Snackbar.LENGTH_LONG).show();
        });

        return fragmentView;
    }

    private void uploadNewTripData() {
        LinearProgressIndicator progressIndicator = requireActivity().findViewById(R.id.progress_indicator);
        progressIndicator.show();
        String newTripName = tripName.getText().toString();
        String newTripDestination = tripDestination.getText().toString();
        String newTripStartDate = tripStartDate.getText().toString();
        String newTripEndDate = tripEndDate.getText().toString();
        String newTripDescription = tripDescription.getText().toString();

        TripsViewModel viewModel = new ViewModelProvider(this).get(TripsViewModel.class);
        Trip uploadedTrip = viewModel.uploadTripData(pickedImages,
                newTripName,
                newTripStartDate,
                newTripEndDate,
                newTripDescription,
                newTripDestination,
                authentication.getUid());
        List<UploadTask> tasks = viewModel.uploadTripImages(uploadedTrip);
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                progressIndicator.hide();
            } else {
                Log.e("NEW_TRIP", "Failed to upload trip images to remote storage: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }
}