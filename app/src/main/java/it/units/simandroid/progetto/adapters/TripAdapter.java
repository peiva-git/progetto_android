package it.units.simandroid.progetto.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ItemViewHolder> {

    private final Context context;
    private List<Trip> trips;
    private final OnTripClickListener clickListener;
    private final SparseBooleanArray selectedTrips;
    private boolean isSharedModeOn = false;
    private final OnFavoriteStateChangedListener favoriteChangedListener;

    public TripAdapter(Context context, List<Trip> trips, OnTripClickListener clickListener, OnFavoriteStateChangedListener favoriteChangedListener) {
        this.context = context;
        this.trips = new ArrayList<>(trips);
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
        return trips;
    }

    public void setAdapterTrips(List<Trip> trips) {
        this.trips = new ArrayList<>(trips);
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedTripsPositions() {
        List<Integer> items = new ArrayList<>(selectedTrips.size());
        for (int i = 0; i < selectedTrips.size(); i++) {
            items.add(selectedTrips.keyAt(i));
        }
        return items;
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

    public void removeTripByPosition(int position) {
        trips.remove(position);
        notifyItemRemoved(position);
    }

    public void removeTripsByPositions(List<Integer> positions) {
        // reverse sort
        Collections.sort(positions, (lhs, rhs) -> rhs - lhs);
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeTripByPosition(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }
                if (count == 1) {
                    removeTripByPosition(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }
                if (count > 0) {
                    positions.subList(0, count).clear();
                }
            }
        }
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            trips.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    public Trip getAdapterTrip(int position) {
        return trips.get(position);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_trip, parent, false);
        return new ItemViewHolder(view, clickListener, favoriteChangedListener);
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
        holder.cardView.setChecked(isTripAtPositionSelected(position));
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
        private final ImageView tripMainPicture;
        private final TextView tripName;
        private final TextView tripDestination;
        private final TextView tripDescription;
        private final TextView tripStartEndDate;
        private final CheckBox isTripFavorite;
        private final MaterialCardView cardView;
        private final OnTripClickListener selectListener;
        private final OnFavoriteStateChangedListener favoriteListener;

        public ItemViewHolder(@NonNull View itemView, OnTripClickListener selectListener, OnFavoriteStateChangedListener favoriteListener) {
            super(itemView);
            tripMainPicture = itemView.findViewById(R.id.main_trip_picture);
            tripDescription = itemView.findViewById(R.id.trip_description);
            tripName = itemView.findViewById(R.id.trip_name);
            tripDestination = itemView.findViewById(R.id.trip_location);
            tripStartEndDate = itemView.findViewById(R.id.trip_start_end_date);
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
