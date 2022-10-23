package it.units.simandroid.progetto;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import it.units.simandroid.progetto.fragments.TripContentImageFragment;

public class SlideshowPagerAdapter extends FragmentStateAdapter {

    public static final String IMAGE_URI_ARG_KEY = "IMAGE_URI";
    private List<String> tripImageUris;

    public SlideshowPagerAdapter(Fragment fragment, List<String> tripImageUris) {
        super(fragment);
        this.tripImageUris = tripImageUris;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String imageUri = tripImageUris.get(position);
        Bundle bundle = new Bundle();
        bundle.putString(IMAGE_URI_ARG_KEY, imageUri);
        Fragment tripImageFragment = new TripContentImageFragment();
        tripImageFragment.setArguments(bundle);
        return tripImageFragment;
    }

    @Override
    public int getItemCount() {
        return tripImageUris.size();
    }
}
