package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
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

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.adapters.SlideshowPagerAdapter;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentDirections;

public class TripContentFragment extends Fragment {

    public static final String TRIP_TAG = "TRIP";
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TextView tripName;
    private TextView tripDates;
    private TextView tripDestination;
    private TextView tripDescription;

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
        Trip trip = TripContentFragmentArgs.fromBundle(requireArguments()).getTrip();
        List<String> tripImages = new ArrayList<>(trip.getImagesUris().size());
        tripImages.addAll(trip.getImagesUris().values());
        Log.d(TRIP_TAG, "Received trip as an argument to fragment " + getClass().getName() + ": " + trip.getName());

        viewPager = fragmentView.findViewById(R.id.trip_image_pager);
        tripName = fragmentView.findViewById(R.id.content_trip_name);
        tripDestination = fragmentView.findViewById(R.id.content_trip_destination);
        tripDates = fragmentView.findViewById(R.id.content_trip_dates);
        tripDescription = fragmentView.findViewById(R.id.content_trip_description);

        pagerAdapter = new SlideshowPagerAdapter(this, tripImages);
        viewPager.setAdapter(pagerAdapter);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem userAccount = menu.findItem(R.id.user_account);
                userAccount.setVisible(false);
                if (!TripContentFragmentArgs.fromBundle(requireArguments()).isSharedTripsModeActive()) {
                    menuInflater.inflate(R.menu.top_app_bar_trip_content, menu);
                    MaterialCheckBox isTripFavorite = (MaterialCheckBox) menu.findItem(R.id.favorite_trip).getActionView();
                    isTripFavorite.setButtonIconDrawableResource(R.drawable.sl_baseline_favorite_24);
                    isTripFavorite.setButtonDrawable(R.drawable.ic_baseline_favorite_border_24);
                    isTripFavorite.setButtonIconTintList(ColorStateList.valueOf(com.google.android.material.R.attr.colorOnPrimary));
                    isTripFavorite.setChecked(trip.isFavorite());

                    isTripFavorite.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                        trip.setFavorite(isChecked);
                        String tripId = TripContentFragmentArgs.fromBundle(requireArguments()).getTrip().getId();
                        FirebaseDatabase.getInstance(DB_URL)
                                .getReference("trips")
                                .child(tripId)
                                .child("favorite")
                                .setValue(isChecked);
                    });
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.favorite_trip) {
                    return true;
                } else if (menuItem.getItemId() == R.id.share_trip) {
                    TripContentFragmentDirections.ShareTripAction action = TripContentFragmentDirections.actionShareTrip();
                    action.setTripId(trip.getId());
                    Navigation.findNavController(requireView())
                            .navigate(action);
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        tripName.setText(trip.getName());
        tripDestination.setText(trip.getDestination());
        tripDates.setText(String.format("%s - %s", trip.getStartDate(), trip.getEndDate()));
        tripDescription.setText(trip.getDescription());

        return fragmentView;
    }
}