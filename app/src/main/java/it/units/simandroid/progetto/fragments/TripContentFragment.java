package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.fragments.TripsFragment.GET_IMAGE_TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.TripsViewModel;
import it.units.simandroid.progetto.adapters.SlideshowPagerAdapter;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentDirections;

public class TripContentFragment extends Fragment {

    public static final String TRIP_CONTENT_TAG = "TRIP_CONTENT";
    private ViewPager2 viewPager;
    private SlideshowPagerAdapter pagerAdapter;
    private TextView tripName;
    private TextView tripDates;
    private TextView tripDestination;
    private TextView tripDescription;
    private TripsViewModel viewModel;

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
        tripDestination = fragmentView.findViewById(R.id.content_trip_destination);
        tripDates = fragmentView.findViewById(R.id.content_trip_dates);
        tripDescription = fragmentView.findViewById(R.id.content_trip_description);

        pagerAdapter = new SlideshowPagerAdapter(this, Collections.emptyList());
        viewPager.setAdapter(pagerAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(TripsViewModel.class);
        viewModel.getTripById(tripId).observe(getViewLifecycleOwner(), trip -> {
            tripName.setText(trip.getName());
            tripDestination.setText(trip.getDestination());
            tripDates.setText(String.format("%s - %s", trip.getStartDate(), trip.getEndDate()));
            tripDescription.setText(trip.getDescription());
            updateUI(trip);
        });

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem userAccount = menu.findItem(R.id.user_account);
                userAccount.setVisible(false);
                menuInflater.inflate(R.menu.top_app_bar_trip_content, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.share_trip) {
                    TripContentFragmentDirections.ShareTripAction action = TripContentFragmentDirections.actionShareTrip();
                    action.setTripId(tripId);
                    Navigation.findNavController(requireView())
                            .navigate(action);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        return fragmentView;
    }

    private void updateUI(Trip trip) {
        if (trip.getImagesUris() != null) {
            List<FileDownloadTask> imagesDownloadTasks = new ArrayList<>();
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
                            Log.d(GET_IMAGE_TAG, "Error downloading image " + imageUriById.getKey() + " for trip " + trip.getId() + ": " + getImage.getException().getMessage());
                        }
                    });
                    imagesDownloadTasks.add(task);
                }
            }
            Tasks.whenAllComplete(imagesDownloadTasks).addOnCompleteListener(task -> {
                List<String> tripImages = new ArrayList<>(trip.getImagesUris().size());
                tripImages.addAll(trip.getImagesUris().values());
                pagerAdapter.setTripImageUris(tripImages);
            });
        }
    }
}