package it.units.simandroid.progetto.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ItemViewHolder> {

    private final Context context;
    private final OnTripClickListener clickListener;
    private final SparseBooleanArray selectedTrips;
    private boolean isSharedModeOn = false;
    private final OnFavoriteStateChangedListener favoriteChangedListener;

    private static final DiffUtil.ItemCallback<Trip> DIFF_CALLBACK = new DiffUtil.ItemCallback<Trip>() {
        @Override
        public boolean areItemsTheSame(@NonNull Trip oldTrip, @NonNull Trip newTrip) {
            return oldTrip.equals(newTrip);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Trip oldTrip, @NonNull Trip newTrip) {
            return Objects.equals(oldTrip.getImagesUris(), newTrip.getImagesUris())
                    && oldTrip.getId().equals(newTrip.getId())
                    && oldTrip.getName().equals(newTrip.getName())
                    && oldTrip.getDestination().equals(newTrip.getDestination())
                    && oldTrip.getDescription().equals(newTrip.getDescription())
                    && oldTrip.getStartDate().equals(newTrip.getStartDate())
                    && oldTrip.getEndDate().equals(newTrip.getEndDate())
                    && oldTrip.isFavorite() == newTrip.isFavorite()
                    && oldTrip.getOwnerId().equals(newTrip.getOwnerId())
                    && Objects.equals(oldTrip.getAuthorizedUsers(), newTrip.getAuthorizedUsers());
        }
    };
    private final AsyncListDiffer<Trip> differ = new AsyncListDiffer<>(this, DIFF_CALLBACK);

    public TripAdapter(Context context, OnTripClickListener clickListener, OnFavoriteStateChangedListener favoriteChangedListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.favoriteChangedListener = favoriteChangedListener;
        selectedTrips = new SparseBooleanArray();
    }

    public boolean isSharedModeOn() {
        return isSharedModeOn;
    }

    public void setSharedModeOn(boolean sharedModeOn) {
        isSharedModeOn = sharedModeOn;
    }

    public List<Trip> getAdapterTrips() {
        return differ.getCurrentList();
    }

    public void submitList(List<Trip> trips) {
        differ.submitList(new ArrayList<>(trips));
    }

    public List<Integer> getSelectedTripsPositions() {
        List<Integer> selectedPositions = new ArrayList<>(selectedTrips.size());
        for (int i = 0; i < selectedTrips.size(); i++) {
            selectedPositions.add(selectedTrips.keyAt(i));
        }
        return selectedPositions;
    }
    public boolean isTripAtPositionSelected(int position) {
        return getSelectedTripsPositions().contains(position);
    }

    public void toggleTripSelection(int position) {
        if (selectedTrips.get(position, false)) {
            selectedTrips.delete(position);
        } else {
            selectedTrips.put(position, true);
        }
        notifyItemChanged(position);
    }

    public int getSelectedTripsCount() {
        return selectedTrips.size();
    }

    public void clearTripSelection() {
        List<Integer> selection = getSelectedTripsPositions();
        selectedTrips.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    public void removeTripsByPositions(List<Integer> positions) {
        List<Trip> newTripsList = new ArrayList<>(differ.getCurrentList());
        // need to sort list first to prevent changes in position values (need to start from largest)
        Collections.sort(positions, Collections.reverseOrder());
        for (int positionToDelete : positions) {
            newTripsList.remove(positionToDelete);
        }
        submitList(newTripsList);
    }

    public Trip getAdapterTrip(int position) {
        return  differ.getCurrentList().get(position);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_trip, parent, false);
        return new ItemViewHolder(view, clickListener, favoriteChangedListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        final Trip trip = differ.getCurrentList().get(position);
        String from = context.getResources().getString(R.string.from);
        String until = context.getResources().getString(R.string.until);
        // need to set everything, otherwise old data is going to stay there when the holder is recycled
        holder.tripName.setText(trip.getName());
        holder.tripDescription.setText(trip.getDescription());
        holder.tripDestination.setText(trip.getDestination());
        holder.tripStartDate.setText(String.format("%s: %s", from, trip.getStartDate()));
        holder.tripEndDate.setText(String.format("%s: %s", until, trip.getEndDate()));
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
            holder.tripMainPicture.setImageResource(R.drawable.ic_baseline_image_not_supported_24);
        }
        holder.cardView.setChecked(isTripAtPositionSelected(position));
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        private final ImageView tripMainPicture;
        private final MaterialTextView tripName;
        private final MaterialTextView tripDestination;
        private final MaterialTextView tripDescription;
        private final MaterialTextView tripStartDate;
        private final MaterialCheckBox isTripFavorite;
        private final MaterialCardView cardView;
        private final OnTripClickListener selectListener;
        private final OnFavoriteStateChangedListener favoriteListener;
        private final MaterialTextView tripEndDate;

        public ItemViewHolder(@NonNull View itemView, OnTripClickListener selectListener, OnFavoriteStateChangedListener favoriteListener) {
            super(itemView);
            tripMainPicture = itemView.findViewById(R.id.main_trip_picture);
            tripDescription = itemView.findViewById(R.id.trip_description);
            tripName = itemView.findViewById(R.id.trip_name);
            tripDestination = itemView.findViewById(R.id.trip_location);
            tripStartDate = itemView.findViewById(R.id.trip_card_start_date);
            tripEndDate = itemView.findViewById(R.id.trip_card_end_date);
            isTripFavorite = itemView.findViewById(R.id.favorite_trip);
            cardView = itemView.findViewById(R.id.trip_card);
            this.selectListener = selectListener;
            this.favoriteListener = favoriteListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            isTripFavorite.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            if (selectListener != null) {
                selectListener.onTripClick(getBindingAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (selectListener != null) {
                return selectListener.onTripLongClick(getBindingAdapterPosition());
            }
            return false;
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteStateChanged(getBindingAdapterPosition(), compoundButton, isChecked);
            }
        }
    }
}
