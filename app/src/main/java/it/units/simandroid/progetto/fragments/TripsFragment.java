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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.tasks.Tasks;
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

    public TripsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authentication = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(TripsViewModel.class);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            List<Trip> trips;
            if (TripsFragmentArgs.fromBundle(TripsFragment.this.requireArguments()).isSharedTripsModeActive()) {
                trips = viewModel.getTripsSharedWithUser(authentication.getUid()).getValue();
            } else if (TripsFragmentArgs.fromBundle(TripsFragment.this.requireArguments()).isFilteringActive()) {
                trips = viewModel.getFavoriteTrips(authentication.getUid()).getValue();
            } else {
                trips = viewModel.getTripsByOwner(authentication.getUid()).getValue();
            }
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
                viewModel.setTripFavorite(trip.getId(), isChecked);
            }
        });
        tripAdapter.setSharedModeOn(TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive());
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        tripsRecyclerView.setLayoutManager(layoutManager);
        tripsRecyclerView.setAdapter(tripAdapter);

        if (newTripButton != null) {
            newTripButton.setOnClickListener(view -> {
                NavController navController = Navigation.findNavController(view);
                navController.navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment());
            });
        }

        if (TripsFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()) {
            viewModel.getTripsSharedWithUser(authentication.getUid()).observe(getViewLifecycleOwner(), trips -> {
                Log.d(DATA_UPDATE_TAG, "Shared trips mode, received trip data update");
                updateUI(trips);
            });
        } else if (TripsFragmentArgs.fromBundle(requireArguments()).isFilteringActive()) {
            viewModel.getFavoriteTrips(authentication.getUid()).observe(getViewLifecycleOwner(), trips -> {
                Log.d(DATA_UPDATE_TAG, "Favorite trips mode, received trip data update");
                updateUI(trips);
            });
        } else {
            viewModel.getTripsByOwner(authentication.getUid()).observe(getViewLifecycleOwner(), trips -> {
                Log.d(DATA_UPDATE_TAG, "My trips mode, received trip data update");
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
            SharedPreferences  preferences = PreferenceManager.getDefaultSharedPreferences(TripsFragment.this.requireContext());
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
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void getTripsImagesWithoutPermissionAndUpdateAdapter(List<Trip> trips) {
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
            }
        }
        Tasks.whenAllComplete(imagesDownloadTasks).addOnCompleteListener(task -> tripAdapter.updateTrips(trips));
    }

    private void getTripsImagesWithPermissionAndUpdateAdapter(List<Trip> trips) {
        List<FileDownloadTask> imagesDownloadTasks = new ArrayList<>();
        for (Trip trip : trips) {
            if (trip.getImagesUris() != null) {
                for (Map.Entry<String, String> imageUriById : trip.getImagesUris().entrySet()) {
                    DocumentFile localImage = DocumentFile.fromSingleUri(TripsFragment.this.requireContext(), Uri.parse(imageUriById.getValue()));
                    if (!Objects.requireNonNull(localImage).exists()) {
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
                }
            }
        }
        Tasks.whenAllComplete(imagesDownloadTasks).addOnCompleteListener(task -> tripAdapter.updateTrips(trips));
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