package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.viewmodels.TripsViewModel;

public class NewTripFragment extends Fragment {

    public static final String DATES_PICKER_TAG = "DATES_PICKER";
    private ActivityResultLauncher<String[]> pickTripImages;
    public static final String IMAGE_PICKER_TAG = "IMG_PICK";
    private FirebaseAuth authentication;
    private List<Uri> pickedImages = Collections.emptyList();
    private ImageButton newImageButton;
    private TextInputEditText tripName;
    private TextInputEditText tripDestination;
    private MaterialButton tripDates;
    private TextInputEditText tripDescription;
    private FloatingActionButton saveTripButton;
    private TextInputLayout tripNameLayout;
    private TextInputLayout tripDestinationLayout;
    private TextInputLayout tripDescriptionLayout;

    public NewTripFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();

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
        tripNameLayout = fragmentView.findViewById(R.id.trip_name_layout);
        tripDestination = fragmentView.findViewById(R.id.trip_destination);
        tripDestinationLayout = fragmentView.findViewById(R.id.trip_destination_layout);
        tripDates = fragmentView.findViewById(R.id.trip_dates);
        tripDescription = fragmentView.findViewById(R.id.trip_description);
        tripDescriptionLayout = fragmentView.findViewById(R.id.trip_description_layout);
        saveTripButton = fragmentView.findViewById(R.id.save_new_trip_button);

        newImageButton.setOnClickListener(view -> pickTripImages.launch(new String[]{"image/*"}));

        tripDates.setOnClickListener(view -> {
            MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setSelection(new Pair<>(
                            MaterialDatePicker.todayInUtcMilliseconds(),
                            MaterialDatePicker.todayInUtcMilliseconds()))
                    .setTitleText(R.string.date_picker_title)
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date startDate = new Date(selection.first);
                Date endDate = new Date(selection.second);
                String formattedStartDate = DateFormat.getDateInstance().format(startDate);
                String formattedEndDate = DateFormat.getDateInstance().format(endDate);
                String result = String.format("%s: %s - %s: %s",
                        getString(R.string.from),
                        formattedStartDate,
                        getString(R.string.until),
                        formattedEndDate);
                Log.d("NEW_TRIP", "Trip dates picked, setting button text to " + result);
                tripDates.setText(result);
            });
            datePicker.show(NewTripFragment.this.requireActivity().getSupportFragmentManager(), DATES_PICKER_TAG);
        });

        saveTripButton.setOnClickListener(view -> {
            if (!formValidation()) {
                return;
            }
            if (pickedImages.isEmpty()) {
                new MaterialAlertDialogBuilder(NewTripFragment.this.requireContext())
                        .setTitle(R.string.no_picked_images_dialog_title)
                        .setMessage(R.string.no_picked_images_dialog_message)
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                            uploadNewTripData();
                            NavHostFragment.findNavController(this).navigateUp();
                            Snackbar.make(NewTripFragment.this.requireActivity().findViewById(R.id.activity_layout), R.string.trip_saved, Snackbar.LENGTH_LONG).show();
                        })
                        .setNegativeButton(R.string.picked_images_dialog_no, (dialogInterface, i) -> {
                        })
                        .show();
            } else {
                uploadNewTripData();
                NavHostFragment.findNavController(this).navigateUp();
                Snackbar.make(NewTripFragment.this.requireActivity().findViewById(R.id.activity_layout), R.string.trip_saved, Snackbar.LENGTH_LONG).show();
            }
        });

        return fragmentView;
    }

    private void uploadNewTripData() {
        LinearProgressIndicator progressIndicator = requireActivity().findViewById(R.id.progress_indicator);
        progressIndicator.show();
        CharSequence datesText = tripDates.getText();
        int indexOfDash = datesText.toString().indexOf("-");
        int fromResourceLength = getString(R.string.from).length();
        int untilResourceLength = getString(R.string.until).length();
        String startDate = datesText.subSequence(fromResourceLength + 2, indexOfDash - 1).toString();
        String endDate = datesText.subSequence(indexOfDash + 1 + untilResourceLength + 3, datesText.length()).toString();
        String newTripName = tripName.getText().toString();
        String newTripDestination = tripDestination.getText().toString();
        String newTripDescription = tripDescription.getText().toString();

        TripsViewModel viewModel = new ViewModelProvider(this).get(TripsViewModel.class);
        List<UploadTask> tasks = viewModel.uploadTripData(pickedImages,
                newTripName,
                startDate,
                endDate,
                newTripDescription,
                newTripDestination,
                authentication.getUid());
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> progressIndicator.hide());
    }

    private boolean formValidation() {
        boolean isFormValid = true;
        Editable name = tripName.getText();
        Editable destination = tripDestination.getText();
        Editable description = tripDescription.getText();

        if (TextUtils.isEmpty(name)) {
            tripNameLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            tripNameLayout.setError(null);
        }
        if (TextUtils.isEmpty(destination)) {
            tripDestinationLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            tripDestinationLayout.setError(null);
        }
        if (TextUtils.isEmpty(description)) {
            tripDescriptionLayout.setError(getString(R.string.field_required));
            isFormValid = false;
        } else {
            tripDescriptionLayout.setError(null);
        }
        return isFormValid;
    }
}