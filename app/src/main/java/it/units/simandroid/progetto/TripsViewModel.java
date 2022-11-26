package it.units.simandroid.progetto;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.api.internal.TaskUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import it.units.simandroid.progetto.exceptions.TripNotFoundException;

public class TripsViewModel extends ViewModel {
    public static final String USERS = "users";
    public static final String TRIPS = "trips";
    public static final String NEW_TRIP_DB_TAG = "NEW_TRIP_DB";
    public static final String IS_FAVORITE_FIELD_NAME = "favorite";
    public static final String AUTHORIZED_USERS_FIELD_NAME = "authorizedUsers";
    private final MutableLiveData<List<Trip>> databaseTrips;
    private final FirebaseDatabase database;
    private final FirebaseStorage storage;

    public TripsViewModel() {
        database = FirebaseDatabase.getInstance(DB_URL);
        storage = FirebaseStorage.getInstance();
        databaseTrips = new MutableLiveData<>();
        database.getReference(TRIPS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {
                };
                Map<String, Object> tripsById = snapshot.getValue(type);
                if (tripsById != null) {
                    List<Trip> trips = new ArrayList<>(tripsById.size());
                    for (String tripKey : tripsById.keySet()) {
                        DataSnapshot tripSnapshot = snapshot.child(tripKey);
                        Trip databaseTrip = tripSnapshot.getValue(Trip.class);
                        trips.add(databaseTrip);
                    }
                    databaseTrips.setValue(trips);
                } else {
                    Log.i("GET_TRIPS", "No trips found in database");
                    databaseTrips.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GET_TRIPS", "Unable to retrieve trips from database: " + error.getMessage());
            }
        });
    }

    public LiveData<List<Trip>> getTrips() {
        return databaseTrips;
    }

    public LiveData<Trip> getTripById(String tripId) throws TripNotFoundException {
        return Transformations.map(databaseTrips, trips -> {
            for (Trip trip : trips) {
                if (trip.getId().equals(tripId)) {
                    return trip;
                }
            }
            throw new TripNotFoundException("Unable to find trip with specified id");
        });
    }

    public LiveData<List<Trip>> getTripsByOwner(String ownerId) {
        return Transformations.map(databaseTrips, trips -> {
            List<Trip> filteredTrips = new ArrayList<>();
            for (Trip trip : trips) {
                if (trip.getOwnerId().equals(ownerId)) {
                    filteredTrips.add(trip);
                }
            }
            return filteredTrips;
        });
    }

    public LiveData<List<Trip>> getTripsSharedWithUser(String userId) {
        return Transformations.map(databaseTrips, trips -> {
            List<Trip> filteredTrips = new ArrayList<>();
            for (Trip trip : trips) {
                if (trip.getAuthorizedUsers() != null) {
                    if (trip.getAuthorizedUsers().containsKey(userId)) {
                        filteredTrips.add(trip);
                    }
                }
            }
            return filteredTrips;
        });
    }

    public LiveData<List<Trip>> getFavoriteTrips(String ownerId) {
        return Transformations.map(getTripsByOwner(ownerId), trips -> {
            List<Trip> filteredTrips = new ArrayList<>();
            for (Trip trip : trips) {
                if (trip.isFavorite()) {
                    filteredTrips.add(trip);
                }
            }
            return filteredTrips;
        });
    }

    public FileDownloadTask getTripImage(Trip trip, String imageId, File image) {
        return storage.getReference(USERS)
                .child(trip.getOwnerId())
                .child(trip.getId())
                .child(imageId)
                .getFile(image);
    }

    public List<Task<Void>> removeTrips(List<Trip> trips) {
        List<Task<Void>> tasks = new ArrayList<>(trips.size());
        for (Trip trip : trips) {
            Task<Void> task = database.getReference(TRIPS)
                    .child(trip.getId())
                    .removeValue();
            tasks.add(task);
        }
        return tasks;
    }

    public Trip uploadTripData(List<Uri> tripImages,
                               String tripName,
                               String tripStartDate,
                               String tripEndDate,
                               String tripDescription,
                               String tripDestination,
                               String tripOwner) {
        DatabaseReference tripReference = database.getReference(TRIPS).push();
        Map<String, String> imagesById = new HashMap<>();
        for (Uri tripImage : tripImages) {
            String imageKey = UUID.randomUUID().toString();
            imagesById.put(imageKey, tripImage.toString());
        }
        Trip trip = new Trip(imagesById,
                tripName,
                tripStartDate,
                tripEndDate,
                tripDescription,
                tripDestination,
                Objects.requireNonNull(tripReference.getKey()),
                null,
                tripOwner);
        tripReference.setValue(trip)
                .addOnSuccessListener(task -> Log.d(NEW_TRIP_DB_TAG, "Added new trip to realtime DB"))
                .addOnFailureListener(exception -> Log.e(NEW_TRIP_DB_TAG, "Failed to add new trip to realtime DB: " + exception.getMessage()));
        return trip;
    }

    public List<UploadTask> uploadTripImages(@NonNull Trip trip) {
        List<UploadTask> tasks = new ArrayList<>();
        if (trip.getImagesUris() != null) {
            for (Map.Entry<String, String> image : trip.getImagesUris().entrySet()) {
                UploadTask task = storage.getReference(USERS)
                        .child(trip.getOwnerId())
                        .child(trip.getId())
                        .child(image.getKey())
                        .putFile(Uri.parse(image.getValue()));
                tasks.add(task);
            }
        }
        return tasks;
    }

    public Task<Void> setTripFavorite(String tripId, boolean isFavorite) {
        return database.getReference(TRIPS)
                .child(tripId)
                .child(IS_FAVORITE_FIELD_NAME)
                .setValue(isFavorite);
    }

    public LiveData<Map<String, Boolean>> getAuthorizedUserIds(String tripId) throws TripNotFoundException {
        return Transformations.map(databaseTrips, trips -> {
            for (Trip trip : trips) {
                if (trip.getId().equals(tripId)) {
                    return trip.getAuthorizedUsers();
                }
            }
            throw new TripNotFoundException("Unable to find trip with specified id");
        });
    }

    public Task<Void> shareTripWithUsers(String tripId, Set<String> userIds) {
        Map<String, Boolean> tripAuthorizations = new HashMap<>(userIds.size());
        for (String userId : userIds) {
            tripAuthorizations.put(userId, true);
        }
        return database.getReference(TRIPS)
                .child(tripId)
                .child(AUTHORIZED_USERS_FIELD_NAME)
                .setValue(tripAuthorizations);
    }

    public void shareTripWithUser(String tripId, String userId) {
        database.getReference(TRIPS)
                .child(tripId)
                .child(AUTHORIZED_USERS_FIELD_NAME)
                .child(userId)
                .setValue(true);
    }

    public void unshareTripWithUser(String tripId, String userId) {
        database.getReference(TRIPS)
                .child(tripId)
                .child(AUTHORIZED_USERS_FIELD_NAME)
                .child(userId)
                .setValue(false);
    }
}
