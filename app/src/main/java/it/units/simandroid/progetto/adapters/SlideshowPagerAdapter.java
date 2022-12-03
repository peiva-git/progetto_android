package it.units.simandroid.progetto.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.fragments.TripContentImageFragment;

public class SlideshowPagerAdapter extends FragmentStateAdapter {

    public static final String IMAGE_URI_ARG_KEY = "IMAGE_URI";
    public static final String TRIP_CONTENT_PAGER_TAG = "TRIP_CONTENT_PAGER";
    private final Context context;
    @NonNull
    private List<String> tripImageUris;

    public SlideshowPagerAdapter(Fragment fragment, Context context, @NonNull List<String> tripImageUris) {
        super(fragment);
        this.context = context;
        this.tripImageUris = tripImageUris;
    }

    public void setTripImageUris(List<String> tripImageUris) {
        this.tripImageUris = tripImageUris;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String imageUri = tripImageUris.get(position);
        Log.d(TRIP_CONTENT_PAGER_TAG, "Creating fragment for image with URI " + imageUri);
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
