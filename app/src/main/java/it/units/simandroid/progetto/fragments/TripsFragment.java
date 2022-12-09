package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.fragments.TripContentFragment.DELETE_TRIP_TAG;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;

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
import it.units.simandroid.progetto.adapters.OnFavoriteStateChangedListener;
import it.units.simandroid.progetto.adapters.OnTripClickListener;
import it.units.simandroid.progetto.adapters.TripAdapter;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;
import it.units.simandroid.progetto.viewmodels.TripsViewModel;


public class TripsFragment extends Fragment implements OnTripClickListener, OnFavoriteStateChangedListener {

    public static final String PERMISSION_DIALOG_SHOWN = "PERMISSION_DIALOG_SHOWN";
    public static final String DATA_UPDATE_TAG = "TRIP_DATA_UPDATE";
    public static final String GET_IMAGE_TAG = "GET_IMAGE";
    public static final String TRIPS_TAG = "TRIPS";
    public static final String SELECTION_TAG = "SELECTION";
    private FirebaseAuth authentication;
    private RecyclerView tripsRecyclerView;
    private FloatingActionButton newTripButton;
    private TripAdapter tripAdapter;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LinearProgressIndicator progressIndicator;
    private TripsViewModel viewModel;
    private List<Trip> trips;
    private MaterialToolbar toolbar;
    private final ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this).get(TripsViewModel.class);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                getTripsImagesWithPermissionAndUpdateAdapter(trips);
            } else {
                getTripsImagesWithoutPermissionAndUpdateAdapter(trips);
            }
        });
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_trips, container, false);
        tripsRecyclerView = fragmentView.findViewById(R.id.trips_recycler_view);
        newTripButton = fragmentView.findViewById(R.id.new_trip_button);
        progressIndicator = requireActivity().findViewById(R.id.progress_indicator);
        toolbar = requireActivity().findViewById(R.id.toolbar);

        FirebaseUser currentUser = authentication.getCurrentUser();
        if (currentUser == null) {
            NavHostFragment.findNavController(this)
                    .navigate(TripsFragmentDirections.actionTripsFragmentToLoginFragment());
            return null;
        }

        boolean isSizeAtLeastLarge = getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
        if (isSizeAtLeastLarge) {
            newTripButton.setVisibility(View.GONE);
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                    getResources().getInteger(R.integer.grid_layout_spans),
                    StaggeredGridLayoutManager.VERTICAL);
            tripsRecyclerView.setLayoutManager(layoutManager);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    getContext(), LinearLayoutManager.VERTICAL, false);
            tripsRecyclerView.setLayoutManager(layoutManager);
        }
        if (TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()
                || TripsFragmentArgs.fromBundle(requireArguments()).isFilteringActive()) {
            newTripButton.setVisibility(View.GONE);
        }

        tripAdapter = new TripAdapter(getContext(), Collections.emptyList(), this, this);
        tripAdapter.setSharedModeOn(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());
        tripsRecyclerView.setAdapter(tripAdapter);
        tripsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (newTripButton != null) {
            newTripButton.setOnClickListener(view ->
                    NavHostFragment.findNavController(TripsFragment.this)
                            .navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment()));
        }

        if (TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()) {
            viewModel.getTripsSharedWithUser(authentication.getUid()).observe(getViewLifecycleOwner(), trips -> {
                Log.d(DATA_UPDATE_TAG, "Shared trips mode, received " + trips.size() + " trips");
                this.trips = trips;
                updateUI(trips);
            });
        } else if (TripsFragmentArgs.fromBundle(requireArguments()).isFilteringActive()) {
            viewModel.getFavoriteTrips(authentication.getUid()).observe(getViewLifecycleOwner(), trips -> {
                Log.d(DATA_UPDATE_TAG, "Favorite trips mode, received " + trips.size() + " trips");
                this.trips = trips;
                updateUI(trips);
            });
        } else {
            viewModel.getTripsByOwner(authentication.getUid()).observe(getViewLifecycleOwner(), trips -> {
                Log.d(DATA_UPDATE_TAG, "My trips mode, received " + trips.size() + " trips");
                this.trips = trips;
                updateUI(trips);
            });
        }
        return fragmentView;
    }

    private void updateUI(@NonNull List<Trip> trips) {
        if (trips.isEmpty()) {
            Snackbar.make(requireView(), R.string.no_trips, Snackbar.LENGTH_SHORT).show();
        }
        if (ContextCompat.checkSelfPermission(TripsFragment.this.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            getTripsImagesWithPermissionAndUpdateAdapter(trips);

        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TripsFragment.this.requireContext());
            boolean hasPermissionDialogBeenShown = preferences.getBoolean(PERMISSION_DIALOG_SHOWN, false);
            if (!hasPermissionDialogBeenShown) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.educational_permission_request_title)
                        .setMessage(R.string.educational_permission_request_content)
                        .setPositiveButton(R.string.got_it, (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
                getTripsImagesWithoutPermissionAndUpdateAdapter(trips);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(PERMISSION_DIALOG_SHOWN, true);
                editor.apply();
            } else {
                getTripsImagesWithoutPermissionAndUpdateAdapter(trips);
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void getTripsImagesWithoutPermissionAndUpdateAdapter(@NonNull List<Trip> trips) {
        progressIndicator.show();
        final int numberOfImages = getTotalNumberOfImages(trips);
        List<FileDownloadTask> imagesDownloadTasks = new ArrayList<>();
        AtomicInteger progress = new AtomicInteger(0);
        for (Trip trip : trips) {
            if (trip.getImagesUris() != null) {
                for (Map.Entry<String, String> imageUriById : trip.getImagesUris().entrySet()) {
                    File tripDirectory = TripsFragment.this.requireContext().getDir(trip.getId(), Context.MODE_PRIVATE);
                    File storedImage = new File(tripDirectory, imageUriById.getKey());
                    if (storedImage.exists()) {
                        trip.getImagesUris().put(imageUriById.getKey(), storedImage.toURI().toString());
                    } else {
                        FileDownloadTask task = viewModel.getTripImage(trip, imageUriById.getKey(), storedImage);
                        task.addOnCompleteListener(getImage -> {
                            if (getImage.isSuccessful()) {
                                trip.getImagesUris().put(imageUriById.getKey(), storedImage.toURI().toString());
                                Log.d(GET_IMAGE_TAG, "Downloaded image " + imageUriById.getKey() + " for trip " + trip.getId());
                                progress.addAndGet(100 / numberOfImages);
                                progressIndicator.setProgressCompat(progress.get(), true);
                            } else {
                                trip.getImagesUris().remove(imageUriById.getKey());
                                Log.d(GET_IMAGE_TAG, "Error downloading image " + imageUriById.getKey() + " for trip " + trip.getId() + ": " + getImage.getException().getMessage());
                            }
                        });
                        imagesDownloadTasks.add(task);
                    }
                }
            } else {
                Log.d(GET_IMAGE_TAG, "No images for trip " + trip.getId());
            }
        }
        Tasks.whenAllComplete(imagesDownloadTasks).addOnCompleteListener(task -> {
            tripAdapter.setAdapterTrips(trips);
            progressIndicator.hide();
        });
    }

    private int getTotalNumberOfImages(@NonNull List<Trip> trips) {
        int counter = 0;
        for (Trip trip : trips) {
            if (trip.getImagesUris() != null) {
                counter += trip.getImagesUris().size();
            }
        }
        return counter;
    }

    private void getTripsImagesWithPermissionAndUpdateAdapter(@NonNull List<Trip> trips) {
        progressIndicator.show();
        final int numberOfImages = getTotalNumberOfImages(trips);
        AtomicInteger progress = new AtomicInteger(0);
        List<FileDownloadTask> imagesDownloadTasks = new ArrayList<>();
        for (Trip trip : trips) {
            if (trip.getImagesUris() != null) {
                for (Map.Entry<String, String> imageUriById : trip.getImagesUris().entrySet()) {
                    DocumentFile localImage = DocumentFile.fromSingleUri(TripsFragment.this.requireContext(), Uri.parse(imageUriById.getValue()));
                    boolean hasImageBeenPickedOnThisPhone = Objects.requireNonNull(localImage).exists();
                    Log.d(GET_IMAGE_TAG, hasImageBeenPickedOnThisPhone ?
                            "Image " + imageUriById.getKey() + " picked on this phone" :
                            "Image " + imageUriById.getKey() + " hasn't been picked on this phone, need to download it");
                    if (!hasImageBeenPickedOnThisPhone) {
                        File tripDirectory = TripsFragment.this.requireContext().getDir(trip.getId(), Context.MODE_PRIVATE);
                        File storedImage = new File(tripDirectory, imageUriById.getKey());
                        Log.d(GET_IMAGE_TAG, storedImage.exists() ?
                                "Image " + imageUriById.getKey() + " already downloaded, available in internal storage" :
                                "Image " + imageUriById.getKey() + " not available in internal storage, downloading");
                        if (storedImage.exists()) {
                            trip.getImagesUris().put(imageUriById.getKey(), storedImage.toURI().toString());
                        } else {
                            FileDownloadTask task = viewModel.getTripImage(trip, imageUriById.getKey(), storedImage);
                            task.addOnCompleteListener(getImage -> {
                                if (getImage.isSuccessful()) {
                                    trip.getImagesUris().put(imageUriById.getKey(), storedImage.toURI().toString());
                                    Log.d(GET_IMAGE_TAG, "Downloaded image " + imageUriById.getKey() + " for trip " + trip.getId());
                                    progress.addAndGet(100 / numberOfImages);
                                    progressIndicator.setProgressCompat(progress.get(), true);
                                } else {
                                    trip.getImagesUris().remove(imageUriById.getKey());
                                    Log.d(GET_IMAGE_TAG, "Error downloading image " + imageUriById.getKey() + " for trip " + trip.getId() + ": " + getImage.getException().getMessage());
                                }
                            });
                            imagesDownloadTasks.add(task);
                        }
                    }
                }
            } else {
                Log.d(GET_IMAGE_TAG, "No images for trip " + trip.getId());
            }
        }
        Tasks.whenAllComplete(imagesDownloadTasks).addOnCompleteListener(task -> {
            tripAdapter.setAdapterTrips(trips);
            progressIndicator.hide();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // show dialog again next time the fragment is shown
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PERMISSION_DIALOG_SHOWN, false);
        editor.apply();
    }

    @Override
    public void onTripClick(int position) {
        if (actionMode != null) {
            toggleTripSelection(position);
            Log.d(SELECTION_TAG, "Toggled trip at position " + position + " out of " + trips.size());
        } else {
            String tripId = tripAdapter.getAdapterTrip(position).getId();
            TripsFragmentDirections.ViewTripDetailsAction action = TripsFragmentDirections.actionViewTripDetails();
            action.setTripId(tripId);
            action.setSharedTripsModeActive(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());
            NavHostFragment.findNavController(TripsFragment.this).navigate(action);
        }
    }

    @Override
    public boolean onTripLongClick(int position) {
        if (!TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()) {
            if (actionMode == null) {
                actionMode = toolbar.startActionMode(actionModeCallback);
            }
            toggleTripSelection(position);
            Log.d(SELECTION_TAG, "Trip at position " + position + " out of " + trips.size() + " added to selection");
        }
        return true;
    }

    private void toggleTripSelection(int position) {
        tripAdapter.toggleTripSelection(position);
        if (tripAdapter.getSelectedTripsCount() == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(tripAdapter.getSelectedTripsCount() + " " + getString(R.string.select_trips_mode_title));
            actionMode.invalidate();
        }
    }

    @Override
    public void onFavoriteStateChanged(int position, CompoundButton compoundButton, boolean isChecked) {
        String tripId = tripAdapter.getAdapterTrip(position).getId();
        viewModel.setTripFavorite(tripId, isChecked)
                .addOnSuccessListener(task -> Log.d(TRIPS_TAG, "Trip " + tripId + " set as favorite"))
                .addOnFailureListener(exception -> Log.w(TRIPS_TAG, "Unable to set trip " + tripId + " as favorite", exception));
    }

    @Override
    public void onStop() {
        super.onStop();
        // remove deleted trips from database as well when the fragment is no longer visible
        if (trips != null) {
            for (Trip trip : trips) {
                if (!tripAdapter.getAdapterTrips().contains(trip)) {
                    viewModel.deleteTrip(trip.getId())
                            .addOnSuccessListener(task -> Log.d(DELETE_TRIP_TAG, "Trip " + trip.getId() + " removed from database"))
                            .addOnFailureListener(exception -> Log.w(DELETE_TRIP_TAG, exception));
                    Tasks.whenAllComplete(viewModel.deleteTripImages(trip))
                            .addOnCompleteListener(task -> Log.d(DELETE_TRIP_TAG, "Images removed for trip " + trip.getId()));
                    deleteLocallyStoredImages(trip);
                }
            }
        } else {
            Log.d(DELETE_TRIP_TAG, "No trips currently loaded");
        }
    }

    private void deleteLocallyStoredImages(Trip trip) {
        if (trip.getImagesUris() != null) {
            File tripDirectory = requireContext().getDir(trip.getId(), Context.MODE_PRIVATE);
            for (String imageId : trip.getImagesUris().keySet()) {
                File storedImage = new File(tripDirectory, imageId);
                if (storedImage.exists()) {
                    Log.d(DELETE_TRIP_TAG, storedImage.delete() ?
                            "Successfully deleted image " + imageId + " from trip " + trip.getId() :
                            "Can't delete image " + imageId + " from trip " + trip.getId());
                } else {
                    Log.d(DELETE_TRIP_TAG, "No image found at " + storedImage.getPath());
                }
            }
        } else {
            Log.d(DELETE_TRIP_TAG, "No images for trip " + trip.getId());
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.top_app_bar_contextual_pick_trips, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.delete_trip) {
                // remove trips from view only
                tripAdapter.removeTripsByPositions(tripAdapter.getSelectedTripsPositions());
                mode.finish();
                Snackbar.make(TripsFragment.this.requireView(), R.string.trips_deleted, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            tripAdapter.clearTripSelection();
            actionMode = null;
        }
    }
}