package it.units.simandroid.progetto.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.SlideshowPagerAdapter;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentArgs;

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
        Log.d(TRIP_TAG, "Received trip as an argument to fragment " + getClass().getName() + ": " + trip.getName());

        viewPager = fragmentView.findViewById(R.id.trip_image_pager);
        tripName = fragmentView.findViewById(R.id.content_trip_name);
        tripDestination = fragmentView.findViewById(R.id.content_trip_destination);
        tripDates = fragmentView.findViewById(R.id.content_trip_dates);
        tripDescription = fragmentView.findViewById(R.id.content_trip_description);

        pagerAdapter = new SlideshowPagerAdapter(this, trip.getImagesUris());
        viewPager.setAdapter(pagerAdapter);

        tripName.setText(trip.getName());
        tripDestination.setText(trip.getDestination());
        tripDates.setText(String.format("%s - %s", trip.getStartDate(), trip.getEndDate()));
        tripDescription.setText(trip.getDescription());

        return fragmentView;
    }
}