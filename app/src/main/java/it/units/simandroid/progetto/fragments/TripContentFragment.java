package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.fragments.TripsFragment.GET_IMAGE_TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.viewmodels.TripsViewModel;
import it.units.simandroid.progetto.adapters.SlideshowPagerAdapter;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentDirections;

public class TripContentFragment extends Fragment {

    public static final String TRIP_CONTENT_TAG = "TRIP_CONTENT";
    public static final String DELETE_TRIP_TAG = "DELETE_TRIP";
    private ViewPager2 viewPager;
    private SlideshowPagerAdapter pagerAdapter;
    private MaterialTextView tripName;
    private MaterialTextView tripStartDate;
    private MaterialTextView tripDestination;
    private MaterialTextView tripDescription;
    private TripsViewModel viewModel;
    private MaterialCheckBox isTripFavorite;
    private Trip trip;
    private LinearProgressIndicator progressIndicator;
    private MaterialTextView tripEndDate;

    public TripContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_trip_content, container, false);
        String tripId = TripContentFragmentArgs.fromBundle(requireArguments()).getTripId();
        Log.d(TRIP_CONTENT_TAG, "Received trip " + tripId + " as an argument to fragment " + getClass().getName());

        viewPager = fragmentView.findViewById(R.id.trip_image_pager);
        tripName = fragmentView.findViewById(R.id.content_trip_name);
        isTripFavorite = fragmentView.findViewById(R.id.content_favorite_trip);
        tripDestination = fragmentView.findViewById(R.id.content_trip_destination);
        tripStartDate = fragmentView.findViewById(R.id.content_trip_start_date);
        tripEndDate = fragmentView.findViewById(R.id.content_trip_end_date);
        tripDescription = fragmentView.findViewById(R.id.content_trip_description);
        progressIndicator = requireActivity().findViewById(R.id.progress_indicator);

        pagerAdapter = new SlideshowPagerAdapter(this, getContext(), Collections.emptyList());
        viewPager.setAdapter(pagerAdapter);

        viewModel = new ViewModelProvider(this).get(TripsViewModel.class);
        viewModel.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> {
            if (trip == null) {
                Log.d(TRIP_CONTENT_TAG, "No trip found for id " + tripId);
                return;
            }
            Log.d(TRIP_CONTENT_TAG, "Trip " + trip.getId() + " received, updating UI");
            tripName.setText(trip.getName());
            isTripFavorite.setChecked(trip.isFavorite());
            tripDestination.setText(trip.getDestination());
            tripStartDate.setText(String.format("%s: %s", getString(R.string.from), trip.getStartDate()));
            tripEndDate.setText(String.format("%s: %s", getString(R.string.until), trip.getEndDate()));
            tripDescription.setText(trip.getDescription());
            this.trip = trip;
            updateUI(trip);
        });

        if (TripContentFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()) {
            isTripFavorite.setVisibility(View.INVISIBLE);
        }
        isTripFavorite.setOnCheckedChangeListener((compoundButton, isChecked) ->
                viewModel.setTripFavorite(tripId, isChecked)
                        .addOnSuccessListener(task -> Log.d(TRIP_CONTENT_TAG, "Trip " + tripId + " set as favorite"))
                        .addOnFailureListener(exception -> Log.w(TRIP_CONTENT_TAG, "Unable to set trip " + tripId + " as favorite", exception)));
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem userAccount = menu.findItem(R.id.user_account);
                userAccount.setVisible(false);
                menuInflater.inflate(R.menu.top_app_bar_trip_content, menu);
                MenuItem deleteTrip = menu.findItem(R.id.delete_trip);
                MenuItem shareTrip = menu.findItem(R.id.share_trip);
                if (TripContentFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()) {
                    deleteTrip.setVisible(false);
                    shareTrip.setVisible(false);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.share_trip) {
                    TripContentFragmentDirections.ShareTripAction action = TripContentFragmentDirections.actionShareTrip();
                    action.setTripId(tripId);
                    NavHostFragment.findNavController(TripContentFragment.this)
                            .navigate(action);
                    return true;
                } else if (menuItem.getItemId() == R.id.delete_trip) {
                    viewModel.deleteTrip(tripId).addOnSuccessListener(task -> Log.d(TRIP_CONTENT_TAG, "Trip " + tripId + " removed from database"));
                    viewModel.deleteTripImages(trip);
                    deleteLocallyStoredImages(tripId);
                    NavHostFragment.findNavController(TripContentFragment.this).navigateUp();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return fragmentView;
    }

    private void deleteLocallyStoredImages(String tripId) {
        if (trip.getImagesUris() != null) {
            File tripDirectory = TripContentFragment.this.requireContext().getDir(tripId, Context.MODE_PRIVATE);
            for (String imageId : trip.getImagesUris().keySet()) {
                File storedImage = new File(tripDirectory, imageId);
                if (storedImage.exists()) {
                    Log.d(DELETE_TRIP_TAG, storedImage.delete() ?
                            "Successfully deleted image " + imageId + " from trip " + tripId :
                            "Can't delete image " + imageId + " from trip " + tripId);
                } else {
                    Log.d(DELETE_TRIP_TAG, "No image found at " + storedImage.getPath());
                }
            }
        }
    }

    private void updateUI(@NonNull Trip trip) {
        progressIndicator.show();
        List<FileDownloadTask> imagesDownloadTasks = new ArrayList<>();
        if (trip.getImagesUris() != null) {
            for (Map.Entry<String, String> imageUriById : trip.getImagesUris().entrySet()) {
                File tripDirectory = requireContext().getDir(trip.getId(), Context.MODE_PRIVATE);
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
                            // not-null, checked if task was successful
                            Log.d(GET_IMAGE_TAG, "Error downloading image " + imageUriById.getKey() + " for trip " + trip.getId() + ": " + Objects.requireNonNull(getImage.getException()).getMessage());
                        }
                    });
                    imagesDownloadTasks.add(task);
                }
            }
            Tasks.whenAllComplete(imagesDownloadTasks).addOnCompleteListener(task -> {
                List<String> tripImages = new ArrayList<>(trip.getImagesUris().size());
                tripImages.addAll(trip.getImagesUris().values());
                pagerAdapter.setTripImageUris(tripImages);
                progressIndicator.hide();
            });
        } else {
            progressIndicator.hide();
            Log.d(GET_IMAGE_TAG, "No images for trip " + trip.getId());
        }
    }
}