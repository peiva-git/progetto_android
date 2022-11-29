package it.units.simandroid.progetto.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Trip> trips;
    private boolean isSharedModeOn = false;

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    public boolean isSharedModeOn() {
        return isSharedModeOn;
    }

    public void setSharedModeOn(boolean sharedModeOn) {
        isSharedModeOn = sharedModeOn;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_trip, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Trip trip = trips.get(position);
        // need to set everything, otherwise old data is going to stay there when the holder is recycled
        holder.tripName.setText(trip.getName());
        holder.tripDescription.setText(trip.getDescription());
        holder.tripDestination.setText(trip.getDestination());
        StringBuilder sb = new StringBuilder(trip.getStartDate());
        holder.tripStartEndDate.setText(sb.append(" - ").append(trip.getEndDate()).toString());
        if (isSharedModeOn) {
            holder.isTripFavorite.setVisibility(View.INVISIBLE);
        } else {
            holder.isTripFavorite.setVisibility(View.VISIBLE);
            holder.isTripFavorite.setChecked(trip.isFavorite());
        }
        if (trip.getImagesUris() != null && !trip.getImagesUris().isEmpty()) {
            Iterator<String> iterator = trip.getImagesUris().values().iterator();
            Uri mainImageUri = Uri.parse(iterator.next());
            holder.tripMainPicture.setImageURI(mainImageUri);
        } else {
            holder.tripMainPicture.setImageResource(R.drawable.ic_baseline_image_24);
        }
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView tripMainPicture;
        private final TextView tripName;
        private final TextView tripDestination;
        private final TextView tripDescription;
        private final TextView tripStartEndDate;
        private final CheckBox isTripFavorite;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tripMainPicture = itemView.findViewById(R.id.main_trip_picture);
            tripDescription = itemView.findViewById(R.id.trip_description);
            tripName = itemView.findViewById(R.id.trip_name);
            tripDestination = itemView.findViewById(R.id.trip_location);
            tripStartEndDate = itemView.findViewById(R.id.trip_start_end_date);
            isTripFavorite = itemView.findViewById(R.id.favorite_trip);
        }
    }
}
