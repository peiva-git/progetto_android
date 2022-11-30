package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TripKeyProvider extends ItemKeyProvider<String> {
    private final List<Trip> trips;

    public TripKeyProvider(int scope, List<Trip> trips) {
        super(scope);
        this.trips = trips;
    }

    @Nullable
    @Override
    public String getKey(int position) {
        return trips.get(position).getId();
    }

    @Override
    public int getPosition(@NonNull String key) {
        for (Trip trip : trips) {
            if (trip.getId().equals(key)) {
                return trips.indexOf(trip);
            }
        }
        return RecyclerView.NO_POSITION;
    }
}
