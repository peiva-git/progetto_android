package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_ERROR;
import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.RealtimeDatabase.GET_DB_TRIPS;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            database.getReference("trips").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {};
                    Map<String, Object> tripsById = snapshot.getValue(type);
                    if (tripsById != null) {
                        trips = new ArrayList<>();
                        for (String tripId : tripsById.keySet()) {
                            DataSnapshot tripSnapshot = snapshot.child(tripId);
                            Trip retrievedTrip = tripSnapshot.getValue(Trip.class);

                            AtomicInteger progress = new AtomicInteger(0);
                            List<FileDownloadTask> downloadTasks = new ArrayList<>();

                            if (retrievedTrip != null) {
                                for (String imageUri : retrievedTrip.getImagesUris()) {
                                    DocumentFile image = DocumentFile.fromSingleUri(requireContext(), Uri.parse(imageUri));
                                    // doesn't return null after Android KitKat, which is above minSdk version
                                    // need READ_EXTERNAL_STORAGE permissions for the exists() call
                                    if (!Objects.requireNonNull(image).exists()) {
                                        File tripDirectory = requireContext().getDir(retrievedTrip.getId(), Context.MODE_PRIVATE);
                                        File localImage = new File(tripDirectory, imageUri.replace("/", "$"));
                                        if (localImage.exists()) {
                                            int index = retrievedTrip.getImagesUris().indexOf(imageUri);
                                            retrievedTrip.getImagesUris().set(index, localImage.toURI().toString());
                                        } else {
                                            FileDownloadTask downloadTask = storage.getReference("users").child(retrievedTrip.getOwnerId())
                                                    .child(retrievedTrip.getId())
                                                    .child(imageUri.replace("/", "$"))
                                                    .getFile(localImage);
                                            downloadTask.addOnSuccessListener(taskSnapshot -> {
                                                        progress.set(progress.get() + (100 / retrievedTrip.getImagesUris().size()));
                                                        LinearProgressIndicator progressIndicator = requireActivity().findViewById(R.id.progress_indicator);
                                                        progressIndicator.setProgressCompat(progress.get(), true);

                                                        int index = retrievedTrip.getImagesUris().indexOf(imageUri);
                                                        retrievedTrip.getImagesUris().set(index, localImage.toURI().toString());
                                                    })
                                                    .addOnFailureListener(exception -> {
                                                        Log.d("GET_TRIP_IMAGE", "Failed to retrieve trip image");
                                                        retrievedTrip.getImagesUris().remove(imageUri);
                                                        Snackbar.make(requireView(), R.string.get_images_error, Snackbar.LENGTH_SHORT).show();
                                                    });
                                            downloadTasks.add(downloadTask);
                                        }
                                    }
                                }
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
                                    tripAdapter.updateTrips(trips);
                                });
                            } else {
                                Log.d("GET_TRIPS", "No trip for id: " + tripId);
                            }
                        }
                    } else {
                        Snackbar.make(requireView(), R.string.no_trips, Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("GET_TRIPS", "Error downloading trip data: " + error.getMessage());
                }
            });
        }

        if (isGranted) {
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

                            List<FileDownloadTask> downloadTasks = new ArrayList<>();
                            AtomicInteger progress = new AtomicInteger(0);
                            for (String uriString : trip.getImagesUris()) {
                                DocumentFile image = DocumentFile.fromSingleUri(requireContext(), Uri.parse(uriString));
                                // check if file is available locally
                                if (!image.exists()) {
                                    // check if file was already added locally
                                    File imageFile = new File(requireContext().getFilesDir(), trip.getId() + "/" + uriString.replace("/", "$"));
                                    if (imageFile.exists()) {
                                        // replace URI in trip's list
                                        int index = trip.getImagesUris().indexOf(uriString);
                                        trip.getImagesUris().set(index, imageFile.toURI().toString());
                                    } else {
                                        FileDownloadTask downloadTask = storage.getReference("users").child(trip.getOwnerId())
                                                .child(trip.getId())
                                                .child(uriString.replace("/", "$"))
                                                .getFile(imageFile);
                                        downloadTask
                                                .addOnSuccessListener(taskSnapshot -> {
                                                    progress.set(progress.get() + (100 / trip.getImagesUris().size()));
                                                    LinearProgressIndicator progressIndicator = requireActivity().findViewById(R.id.progress_indicator);
                                                    progressIndicator.setProgressCompat(progress.get(), true);

                                                    // replace URI in trip's list
                                                    int index = trip.getImagesUris().indexOf(uriString);
                                                    trip.getImagesUris().set(index, imageFile.toURI().toString());
                                                })
                                                .addOnFailureListener(exception -> {
                                                    Log.d("GET_TRIP_IMAGE", "Failed to retrieve trip image");
                                                    trip.getImagesUris().remove(uriString);
                                                    Snackbar.make(requireActivity().findViewById(R.id.activity_layout), R.string.get_images_error, Snackbar.LENGTH_SHORT).show();
                                                });

                                        downloadTasks.add(downloadTask);
                                    }
                                }
                            }
                            Tasks.whenAllComplete(downloadTasks).addOnCompleteListener(task -> {
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
                                tripAdapter.updateTrips(trips);
                            });
                        }
                        tripAdapter.updateTrips(trips);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(DB_ERROR, "Trips' value update canceled: " + error.getMessage());
                }
            });

        } else {
            Log.d("PERMISSION", "Permission not granted");
            // can't check if images are already available locally
            // gonna rely only on remote or previously downloaded data
            // and discard everything else
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

                            List<FileDownloadTask> downloadTasks = new ArrayList<>();
                            List<String> newImageUris = new ArrayList<>();
                            AtomicInteger progress = new AtomicInteger(0);
                            for (String oldUriString : trip.getImagesUris()) {
                                File imageFile = new File(requireContext().getFilesDir(), trip.getId() + "/" + oldUriString.replace("/", "$"));
                                if (imageFile.exists()) {
                                    newImageUris.add(imageFile.toURI().toString());
                                } else {
                                    FileDownloadTask downloadTask = storage.getReference("users").child(trip.getOwnerId())
                                            .child(trip.getId())
                                            .child(oldUriString.replace("/", "$"))
                                            .getFile(imageFile);
                                    downloadTask
                                            .addOnSuccessListener(taskSnapshot -> {
                                                progress.set(progress.get() + (100 / trip.getImagesUris().size()));
                                                LinearProgressIndicator progressIndicator = requireActivity().findViewById(R.id.progress_indicator);
                                                progressIndicator.setProgressCompat(progress.get(), true);

                                                newImageUris.add(imageFile.toURI().toString());
                                            })
                                            .addOnFailureListener(exception -> {
                                                Log.d("GET_TRIP_IMAGE", "Failed to retrieve trip image");
                                                Snackbar.make(requireActivity().findViewById(R.id.activity_layout), R.string.get_images_error, Snackbar.LENGTH_SHORT).show();
                                            });
                                    downloadTasks.add(downloadTask);
                                }
                            }
                            Tasks.whenAllComplete(downloadTasks).addOnCompleteListener(task -> {
                                trip.setImagesUris(newImageUris);
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
                                tripAdapter.updateTrips(trips);
                            });
                        }
                        tripAdapter.updateTrips(trips);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
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