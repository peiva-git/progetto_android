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
    private List<Trip> trips;

    public OnTripClickListener getOnTripClickListener() {
        return onTripClickListener;
    }

    public void setOnTripClickListener(OnTripClickListener onTripClickListener) {
        this.onTripClickListener = onTripClickListener;
    }

    private OnTripClickListener onTripClickListener;
    private OnFavoriteStateChangedListener onFavoriteStateChangedListener;

    public OnTripLongClickListener getOnTripLongClickListener() {
        return onTripLongClickListener;
    }

    public void setOnTripLongClickListener(OnTripLongClickListener onTripLongClickListener) {
        this.onTripLongClickListener = onTripLongClickListener;
    }

    private OnTripLongClickListener onTripLongClickListener;
    private boolean isSharedModeOn = false;

    public TripAdapter(Context context, List<Trip> trips,
                       OnTripClickListener onTripClickListener,
                       OnFavoriteStateChangedListener onFavoriteStateChangedListener,
                       OnTripLongClickListener onTripLongClickListener) {
        this.context = context;
        this.trips = trips;
        this.onTripClickListener = onTripClickListener;
        this.onFavoriteStateChangedListener = onFavoriteStateChangedListener;
        this.onTripLongClickListener = onTripLongClickListener;
    }

    public void updateTrips(List<Trip> trips) {
        this.trips = trips;
        notifyDataSetChanged();
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
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            SecureRandom random = new SecureRandom();
            params.width += random.nextInt(30);
            view.setLayoutParams(params);
        }
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Trip trip = trips.get(position);
        holder.tripName.setText(trip.getName());
        holder.tripDescription.setText(trip.getDescription());
        holder.tripDestination.setText(trip.getDestination());
        StringBuilder sb = new StringBuilder(trip.getStartDate());
        holder.tripStartEndDate.setText(sb.append(" - ").append(trip.getEndDate()).toString());
        if (isSharedModeOn) {
            holder.isTripFavorite.setVisibility(View.GONE);
        } else {
            holder.isTripFavorite.setChecked(trip.isFavorite());
        }
        if (trip.getImagesUris() != null && !trip.getImagesUris().isEmpty()) {
            Iterator<String> iterator = trip.getImagesUris().values().iterator();
            Uri mainImageUri = Uri.parse(iterator.next());
            holder.tripMainPicture.setImageURI(mainImageUri);
        }
        holder.cardView.setOnLongClickListener(view -> onTripLongClickListener.onLongClick(trip, view));
        holder.cardView.setOnClickListener(view -> onTripClickListener.onClick(trip, view));
        holder.isTripFavorite.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> onFavoriteStateChangedListener.onFavoriteStateChanged(trip, compoundButton, isChecked));
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
        private final MaterialCardView cardView;
        private final CheckBox isTripFavorite;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tripMainPicture = itemView.findViewById(R.id.main_trip_picture);
            tripDescription = itemView.findViewById(R.id.trip_description);
            tripName = itemView.findViewById(R.id.trip_name);
            tripDestination = itemView.findViewById(R.id.trip_location);
            tripStartEndDate = itemView.findViewById(R.id.trip_start_end_date);
            cardView = itemView.findViewById(R.id.trip_card);
            isTripFavorite = itemView.findViewById(R.id.favorite_trip);
        }
    }
}
