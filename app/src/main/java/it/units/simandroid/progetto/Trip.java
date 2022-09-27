package it.units.simandroid.progetto;

import java.time.LocalDate;

public class Trip {

    private String name;
    private int mainPicture;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    public Trip(String name, int mainPicture, LocalDate startDate, LocalDate endDate, String description) {
        this.name = name;
        this.mainPicture = mainPicture;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public Trip(String name, int mainPicture, String description) {
        this.name = name;
        this.mainPicture = mainPicture;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMainPicture() {
        return mainPicture;
    }

    public void setMainPicture(int mainPicture) {
        this.mainPicture = mainPicture;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
