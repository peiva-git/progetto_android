package it.units.simandroid.progetto;

import android.net.Uri;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Trip {

    private List<String> imagesUris;
    private String name;
    private String startDate;
    private String endDate;
    private String description;
    private String destination;
    private boolean isFavorite = false;
    private String id;

    public Trip(String name, String startDate, String endDate, String description, String destination) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.destination = destination;
    }

    public Trip(List<String> imagesUris, String name, String startDate, String endDate, String description, String destination) {
        this.imagesUris = imagesUris;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.destination = destination;
    }

    public Trip() {

    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public List<String> getImagesUris() {
        return imagesUris;
    }

    public void setImagesUris(List<String> imagesUris) {
        this.imagesUris = imagesUris;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return Objects.equals(imagesUris, trip.imagesUris) && Objects.equals(name, trip.name) && Objects.equals(startDate, trip.startDate) && Objects.equals(endDate, trip.endDate) && Objects.equals(description, trip.description) && Objects.equals(destination, trip.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imagesUris, name, startDate, endDate, description, destination);
    }
}
