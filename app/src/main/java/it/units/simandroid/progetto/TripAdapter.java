package it.units.simandroid.progetto;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.List;

import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ItemViewHolder> {

    private final Context context;
    private final List<Trip> trips;

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
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
        holder.tripName.setText(trip.getName());
        holder.tripDescription.setText(trip.getDescription());
        holder.tripDestination.setText(trip.getDestination());
        StringBuilder sb = new StringBuilder(trip.getStartDate());
        holder.tripStartEndDate.setText(sb.append(" - ").append(trip.getEndDate()).toString());
        holder.isTripFavorite.setChecked(trip.isFavorite());
        if (trip.getImagesUris() != null) {
            Uri mainImageUri = Uri.parse(trip.getImagesUris().get(0));
            holder.tripMainPicture.setImageURI(mainImageUri);
        }
        holder.cardView.setOnClickListener(view -> {
            TripsFragmentDirections.ViewTripDetailsAction action = TripsFragmentDirections.actionViewTripDetails();
            action.setTrip(trip);
            Navigation.findNavController(view).navigate(action);
        });
        holder.isTripFavorite.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            trip.setFavorite(isChecked);
            FirebaseDatabase.getInstance(DB_URL)
                    .getReference("trips")
                    .child(trip.getId())
                    .setValue(trip);
        });
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
