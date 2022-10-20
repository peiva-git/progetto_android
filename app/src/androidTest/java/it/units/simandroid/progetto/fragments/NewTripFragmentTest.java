package it.units.simandroid.progetto.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.RealtimeDatabase;
import it.units.simandroid.progetto.Trip;

public class NewTripFragmentTest {

    public static final String TRIP_NAME = "My trip";
    public static final String TRIP_DESTINATION = "Trieste";
    public static final String TRIP_START_DATE = "23/02/2022";
    public static final String TRIP_END_DATE = "25/02/2022";
    public static final String TRIP_DESCRIPTION = "My favorite trip so far!";
    private FirebaseDatabase database;
    private FragmentScenario<NewTripFragment> scenario;
    private ValueEventListener listener;

    @Test
    public void addNewTripWithoutAnImage() {
        database = FirebaseDatabase.getInstance(RealtimeDatabase.DB_URL);
        listener = new ValueEventListener() {
            private boolean isFirstEvent = true;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFirstEvent) {
                    Assert.assertNull(snapshot.getValue());
                    isFirstEvent = false;
                } else {
                    DataSnapshot tripSnapshot = snapshot.child("0");
                    Trip retrievedTrip = tripSnapshot.getValue(Trip.class);
                    Trip expectedTrip = new Trip(TRIP_NAME, TRIP_START_DATE, TRIP_END_DATE, TRIP_DESCRIPTION, TRIP_DESTINATION);
                    Assert.assertEquals(expectedTrip, retrievedTrip);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new AssertionError("Failed to retrieve remote trip: " + error.getMessage());
            }
        };
        database.getReference("trips").addValueEventListener(listener);
        scenario = FragmentScenario.launchInContainer(NewTripFragment.class, Bundle.EMPTY, R.style.Theme_ProgettoSIMAndroid);
        Espresso.onView(ViewMatchers.withId(R.id.trip_name))
                .perform(ViewActions.typeText(TRIP_NAME))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_destination))
                .perform(ViewActions.typeText(TRIP_DESTINATION))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_start_date))
                .perform(ViewActions.typeText(TRIP_START_DATE))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_end_date))
                .perform(ViewActions.typeText(TRIP_END_DATE))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_description))
                .perform(ViewActions.typeText(TRIP_DESCRIPTION))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.save_new_trip_button))
                .perform(ViewActions.click());
    }

    @After
    public void cleanup() {
        scenario.close();
        database.getReference("trips").removeEventListener(listener);
        database.getReference("trips").removeValue();
    }
}