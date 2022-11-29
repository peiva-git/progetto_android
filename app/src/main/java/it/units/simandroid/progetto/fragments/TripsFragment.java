package it.units.simandroid.progetto.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
import it.units.simandroid.progetto.TripsViewModel;
import it.units.simandroid.progetto.adapters.OnTripClickListener;
import it.units.simandroid.progetto.adapters.TripAdapter;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;


public class TripsFragment extends Fragment {

    public static final String PERMISSION_ASKED = "PERMISSION_ASKED";
    public static final String PERMISSION_DIALOG_SHOWN = "PERMISSION_DIALOG_SHOWN";
    public static final String DATA_UPDATE_TAG = "TRIP_DATA_UPDATE";
    public static final String GET_IMAGE_TAG = "GET_IMAGE";
    private FirebaseAuth authentication;
    private RecyclerView tripsRecyclerView;
    private FloatingActionButton newTripButton;
    private TripAdapter tripAdapter;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private LinearProgressIndicator progressIndicator;
    private TripsViewModel viewModel;
    private List<Trip> trips;

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

        boolean isSizeAtLeastLarge = getResources().getConfiguration().isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE);
        if (isSizeAtLeastLarge) {
            newTripButton.setVisibility(View.GONE);
        }

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        tripsRecyclerView.setLayoutManager(layoutManager);

        if (newTripButton != null) {
            newTripButton.setOnClickListener(view -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment());
            });
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

    private void updateUI(List<Trip> trips) {
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
                        .setPositiveButton(R.string.got_it, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            getTripsImagesWithoutPermissionAndUpdateAdapter(trips);
                        })
                        .show();
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

    private void getTripsImagesWithoutPermissionAndUpdateAdapter(List<Trip> trips) {
        progressIndicator.show();
        List<FileDownloadTask> imagesDownloadTasks = new ArrayList<>();
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
            tripAdapter = new TripAdapter(getContext(), trips);
            tripAdapter.setSharedModeOn(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());
            tripsRecyclerView.setAdapter(tripAdapter);
            progressIndicator.hide();
        });
    }

    private void getTripsImagesWithPermissionAndUpdateAdapter(List<Trip> trips) {
        progressIndicator.show();
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
            tripAdapter = new TripAdapter(getContext(), trips);
            tripAdapter.setSharedModeOn(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());
            tripsRecyclerView.setAdapter(tripAdapter);
            progressIndicator.hide();
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PERMISSION_DIALOG_SHOWN, false);
        editor.apply();
    }
}