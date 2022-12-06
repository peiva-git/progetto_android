package it.units.simandroid.progetto.viewmodels;

import android.net.Uri;

import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class NewTripViewModel extends ViewModel {
    public static final String START_DATE_KEY = "START_DATE";
    public static final String END_DATE_KEY = "END_DATE";
    public static final String PICKED_IMAGES_KEY = "PICKED_IMAGES";

    private SavedStateHandle state;

    public NewTripViewModel(SavedStateHandle savedStateHandle) {
        this.state = savedStateHandle;
    }

    public void saveStartDate(long startDate) {
        state.set(START_DATE_KEY, startDate);
    }

    public void saveEndDate(long endDate) {
        state.set(END_DATE_KEY, endDate);
    }

    public void savePickedImages(List<Uri> images) {
        state.set(PICKED_IMAGES_KEY, images);
    }

    public Long getStartDate() {
        return state.get(START_DATE_KEY);
    }

    public Long getEndDate() {
        return state.get(END_DATE_KEY);
    }

    public List<Uri> getPickedImages() {
        return state.get(PICKED_IMAGES_KEY);
    }

}
