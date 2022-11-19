package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.RealtimeDatabase.GET_DB_TRIPS;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.adapters.OnTripClickListener;
import it.units.simandroid.progetto.adapters.TripAdapter;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;


public class TripsFragment extends Fragment {

    public static final String PERMISSION_ASKED = "PERMISSION_ASKED";
    public static final String PERMISSION_DIALOG_SHOWN = "PERMISSION_DIALOG_SHOWN";
    private FirebaseAuth authentication;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private List<Trip> displayedTrips;
    private RecyclerView tripsRecyclerView;
    private FloatingActionButton newTripButton;
    private TripAdapter tripAdapter;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private List<Trip> retrievedTrips;
    private LinearProgressIndicator progressIndicator;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance(DB_URL);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                getAndDisplayTrips();
            } else {
                getAndDisplayTripsWithoutPermission();
            }
        });
    }

    private void getAndDisplayTripsWithoutPermission() {
        displayedTrips = new ArrayList<>();
        AtomicInteger progress = new AtomicInteger(0);
        for (Trip retrievedTrip : retrievedTrips) {
            List<FileDownloadTask> downloadTasks = new ArrayList<>();
            if (retrievedTrip.getImagesUris() != null) {
                for (String imageUri : retrievedTrip.getImagesUris()) {
                    updateTripImageFromLocalStorageOrRemotely(retrievedTrip.getId(), retrievedTrip.getOwnerId(), retrievedTrip.getImagesUris(), imageUri, progress, downloadTasks);
                }
            } else {
                retrievedTrip.setImagesUris(Collections.emptyList());
            }
            setupUpdateTripDataOnTasksFinishedListener(downloadTasks, retrievedTrip);
        }
    }


    private void updateTripImageFromLocalStorageOrRemotely(@NonNull String tripId, @NonNull String tripOwnerId, @NonNull List<String> tripImageUris, @NonNull String imageUri, @NonNull AtomicInteger downloadProgress, @NonNull List<FileDownloadTask> downloadTasks) {
        File tripDirectory = requireContext().getDir(tripId, Context.MODE_PRIVATE);
        File localImage = new File(tripDirectory, imageUri.replace("/", "$"));
        if (localImage.exists()) {
            int index = tripImageUris.indexOf(imageUri);
            tripImageUris.set(index, localImage.toURI().toString());
        } else {
            FileDownloadTask downloadTask = storage.getReference("users").child(tripOwnerId)
                    .child(tripId)
                    .child(imageUri.replace("/", "$"))
                    .getFile(localImage);
            downloadTask.addOnSuccessListener(taskSnapshot -> {
                        downloadProgress.set(downloadProgress.get() + (100 / getFinalProgressValue()));
                        progressIndicator.setProgressCompat(downloadProgress.get(), true);

                        int index = tripImageUris.indexOf(imageUri);
                        tripImageUris.set(index, localImage.toURI().toString());
                    })
                    .addOnFailureListener(exception -> {
                        Log.d("GET_TRIP_IMAGE", "Failed to retrieve trip image " + imageUri + "; " + exception.getMessage());
                        tripImageUris.remove(imageUri);
                    });
            downloadTasks.add(downloadTask);
        }
    }

    private int getFinalProgressValue() {
        int finalProgressValue = 0;
        for (Trip retrievedTrip : retrievedTrips) {
            finalProgressValue += retrievedTrip.getImagesUris().size();
        }
        return finalProgressValue;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View fragmentView = inflater.inflate(R.layout.fragment_trips, container, false);
        tripsRecyclerView = fragmentView.findViewById(R.id.trips_recycler_view);
        newTripButton = fragmentView.findViewById(R.id.new_trip_button);
        progressIndicator = requireActivity().findViewById(R.id.progress_indicator);

        tripAdapter = new TripAdapter(getContext(), Collections.emptyList(), new OnTripClickListener() {
            @Override
            public void onTripClick(Trip trip) {
                TripsFragmentDirections.ViewTripDetailsAction action = TripsFragmentDirections.actionViewTripDetails();
                action.setTrip(trip);
                action.setSharedTripsModeActive(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());
                Navigation.findNavController(requireView()).navigate(action);
            }

            @Override
            public void onTripFavoriteStateChanged(Trip trip, CompoundButton compoundButton, boolean isChecked) {
                trip.setFavorite(isChecked);
                FirebaseDatabase.getInstance(DB_URL)
                        .getReference("trips")
                        .child(trip.getId())
                        .child("favorite")
                        .setValue(isChecked);
            }
        });
        tripAdapter.setSharedModeOn(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            tripsRecyclerView.setLayoutManager(linearLayoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            tripsRecyclerView.setLayoutManager(gridLayoutManager);
        }
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
                Map<String, Object> tripsById = snapshot.getValue(type);
                retrievedTrips = new ArrayList<>();
                if (tripsById != null) {
                    for (String key : tripsById.keySet()) {
                        DataSnapshot tripSnapshot = snapshot.child(key);
                        retrievedTrips.add(tripSnapshot.getValue(Trip.class));
                    }
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        getAndDisplayTrips();
                    } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
                        boolean hasPermissionDialogBeenShown = preferences.getBoolean(PERMISSION_DIALOG_SHOWN, false);
                        if (!hasPermissionDialogBeenShown) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.educational_permission_request_title)
                                    .setMessage(R.string.educational_permission_request_content)
                                    .setPositiveButton(R.string.got_it, (dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                        getAndDisplayTripsWithoutPermission();
                                    })
                                    .show();
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean(PERMISSION_DIALOG_SHOWN, true);
                            editor.apply();
                        }
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                } else {
                    Snackbar.make(requireActivity().findViewById(R.id.activity_layout), R.string.no_trips, Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("GET_TRIPS", "Error downloading trip data: " + error.getMessage());
            }
        });
        return fragmentView;
    }

    private void getAndDisplayTrips() {
        displayedTrips = new ArrayList<>();
        for (Trip retrievedTrip : retrievedTrips) {
            AtomicInteger progress = new AtomicInteger(0);
            List<FileDownloadTask> downloadTasks = new ArrayList<>();
            if (retrievedTrip.getImagesUris() != null) {
                for (String imageUri : retrievedTrip.getImagesUris()) {
                    DocumentFile image = DocumentFile.fromSingleUri(requireContext(), Uri.parse(imageUri));
                    // doesn't return null after Android KitKat, which is above minSdk version
                    // need READ_EXTERNAL_STORAGE permissions for the exists() call
                    if (!Objects.requireNonNull(image).exists()) {
                        updateTripImageFromLocalStorageOrRemotely(retrievedTrip.getId(), retrievedTrip.getOwnerId(), retrievedTrip.getImagesUris(), imageUri, progress, downloadTasks);
                    }
                }
            } else {
                retrievedTrip.setImagesUris(Collections.emptyList());
            }

            setupUpdateTripDataOnTasksFinishedListener(downloadTasks, retrievedTrip);
        }
    }

    private void setupUpdateTripDataOnTasksFinishedListener(@NonNull List<FileDownloadTask> downloadTasks,@NonNull Trip retrievedTrip) {
        if (downloadTasks.isEmpty()) {
            boolean isSharedTripsModeOn = TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive();
            boolean isFavoritesFilteringEnabled = TripsFragmentArgs.fromBundle(requireArguments()).isFilteringActive();
            boolean isTripFavorite = retrievedTrip.isFavorite();
            if (isSharedTripsModeOn) {
                addTripIfUserAuthorized(retrievedTrip);
            } else {
                if (isFavoritesFilteringEnabled) {
                    if (isTripFavorite) {
                        addTripIfCurrentUserOwner(retrievedTrip);
                    }
                } else {
                    addTripIfCurrentUserOwner(retrievedTrip);
                }
            }
            tripAdapter.updateTrips(displayedTrips);
        } else {
            Tasks.whenAllComplete(downloadTasks).addOnCompleteListener(task -> {
                boolean isSharedTripsModeOn = TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive();
                boolean isFavoritesFilteringEnabled = TripsFragmentArgs.fromBundle(requireArguments()).isFilteringActive();
                boolean isTripFavorite = retrievedTrip.isFavorite();
                if (isSharedTripsModeOn) {
                    addTripIfUserAuthorized(retrievedTrip);
                } else {
                    if (isFavoritesFilteringEnabled) {
                        if (isTripFavorite) {
                            addTripIfCurrentUserOwner(retrievedTrip);
                        }
                    } else {
                        addTripIfCurrentUserOwner(retrievedTrip);
                    }
                }
                tripAdapter.updateTrips(displayedTrips);
            });
        }
    }


    private void addTripIfUserAuthorized(@NonNull Trip trip) {
        if (trip.getAuthorizedUsers() == null) {
            return;
        }
        if (trip.getAuthorizedUsers().contains(authentication.getUid())) {
            displayedTrips.add(trip);
            Log.d(GET_DB_TRIPS, "Trip with id " + trip.getId() + " added to list");
        }
    }

    private void addTripIfCurrentUserOwner(@NonNull Trip trip) {
        if (trip.getOwnerId().equals(authentication.getUid())) {
            displayedTrips.add(trip);
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