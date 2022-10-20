package it.units.simandroid.progetto;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private final Context context;
    private final List<Trip> trips;

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = trips;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = trips.get(position);
        Uri mainImageUri = Uri.parse(trip.getImagesUris().get(0));
        holder.tripName.setText(trip.getName());
        holder.tripDescription.setText(trip.getDescription());
        holder.tripMainPicture.setImageURI(mainImageUri);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView tripMainPicture;
        private final TextView tripName;
        private final TextView tripDestination;
        private final TextView tripDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripMainPicture = itemView.findViewById(R.id.main_trip_picture);
            tripDescription = itemView.findViewById(R.id.trip_description);
            tripName = itemView.findViewById(R.id.trip_name);
            tripDestination = itemView.findViewById(R.id.trip_location);
        }
    }
}
