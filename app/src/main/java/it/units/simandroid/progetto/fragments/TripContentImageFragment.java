package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.SlideshowPagerAdapter.IMAGE_URI_ARG_KEY;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import it.units.simandroid.progetto.R;

public class TripContentImageFragment extends Fragment {

    private ImageView tripImage;

    public TripContentImageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_trip_content_image, container, false);
        String imageUri = requireArguments().getString(IMAGE_URI_ARG_KEY);
        tripImage = fragmentView.findViewById(R.id.content_trip_image);
        tripImage.setImageURI(Uri.parse(imageUri));
        return fragmentView;
    }
}