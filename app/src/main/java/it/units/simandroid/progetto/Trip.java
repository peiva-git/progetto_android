package it.units.simandroid.progetto;

import java.time.LocalDate;

public class Trip {

    private String name;
    private int mainPictureId;
    private String startDate;
    private String endDate;
    private String description;
    private String destination;

    public Trip(String name, int mainPictureId, String startDate, String endDate, String description) {
        this.name = name;
        this.mainPictureId = mainPictureId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public Trip(String name, int mainPictureId, String description) {
        this.name = name;
        this.mainPictureId = mainPictureId;
        this.description = description;
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

    public void setName(String name) {
        this.name = name;
    }

    public int getMainPictureId() {
        return mainPictureId;
    }

    public void setMainPictureId(int mainPictureId) {
        this.mainPictureId = mainPictureId;
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
}
