package it.units.simandroid.progetto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class Trip {

    @Nullable
    private Map<String, String> imagesUris;
    @NotNull
    private String name;
    @NotNull
    private String startDate;
    @NotNull
    private String endDate;
    @NotNull
    private String description;
    @NotNull
    private String destination;
    private boolean isFavorite = false;
    @NotNull
    private String id;
    @Nullable
    private Map<String, Boolean> authorizedUsers;
    @NotNull
    private String ownerId;

    public Trip(
            @Nullable Map<String, String> imagesUris,
            @NotNull String name,
            @NotNull String startDate,
            @NotNull String endDate,
            @NotNull String description,
            @NotNull String destination,
            @NotNull String tripId,
            @Nullable Map<String, Boolean> authorizedUsers,
            @NotNull String ownerId) {
        this.imagesUris = imagesUris;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.destination = destination;
        this.id = tripId;
        this.authorizedUsers = authorizedUsers;
        this.ownerId = ownerId;
    }

    // needed for firebase database
    public Trip() {

    }

    @NotNull
    public String getDestination() {
        return destination;
    }

    public void setDestination(@NotNull String destination) {
        this.destination = destination;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Map<String, String> getImagesUris() {
        return imagesUris;
    }

    public void setImagesUris(@Nullable Map<String, String> imagesUris) {
        this.imagesUris = imagesUris;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(@NotNull String startDate) {
        this.startDate = startDate;
    }

    @NotNull
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(@NotNull String endDate) {
        this.endDate = endDate;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    @Nullable
    public Map<String, Boolean> getAuthorizedUsers() {
        return authorizedUsers;
    }

    @NotNull
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@NotNull String ownerId) {
        this.ownerId = ownerId;
    }

    public void setAuthorizedUsers(@Nullable Map<String, Boolean> authorizedUsers) {
        this.authorizedUsers = authorizedUsers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return id.equals(trip.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
