package it.units.simandroid.progetto.fragments;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Map;

import it.units.simandroid.progetto.MainActivity;
import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.RealtimeDatabase;
import it.units.simandroid.progetto.Trip;

@LargeTest
public class NewTripFragmentTest {

    public static final String TRIP_NAME = "My trip";
    public static final String TRIP_DESTINATION = "Trieste";
    public static final Calendar TRIP_START_DATE = Calendar.getInstance();
    public static final Calendar TRIP_END_DATE = Calendar.getInstance();
    public static final String TRIP_DESCRIPTION = "My favorite trip so far!";
    private FirebaseDatabase database;
    private ValueEventListener listener;

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void init() {
        database = FirebaseDatabase.getInstance(RealtimeDatabase.DB_URL);
        listener = new ValueEventListener() {
            private boolean isFirstEvent = true;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFirstEvent) {
                    Assert.assertNull(snapshot.getValue());
                    isFirstEvent = false;
                } else {
                    GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {
                    };
                    Map<String, Object> tripsById = snapshot.getValue(type);
                    assert tripsById != null;
                    String firstKey = tripsById.keySet().iterator().next();
                    DataSnapshot tripSnapshot = snapshot.child(firstKey);
                    Trip retrievedTrip = tripSnapshot.getValue(Trip.class);
                    Trip expectedTrip = new Trip(TRIP_NAME, DateFormat.getDateInstance().format(TRIP_START_DATE.getTime()), DateFormat.getDateInstance().format(TRIP_END_DATE.getTime()), TRIP_DESCRIPTION, TRIP_DESTINATION);
                    Assert.assertEquals(expectedTrip, retrievedTrip);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new AssertionError("Failed to retrieve remote trip: " + error.getMessage());
            }
        };
        database.getReference("trips").addValueEventListener(listener);
    }

    @Test
    public void addNewTripWithoutAnImageAndDefaultDates() {
        Espresso.onView(ViewMatchers.withId(R.id.new_trip_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.trip_name))
                .perform(ViewActions.typeText(TRIP_NAME))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_destination))
                .perform(ViewActions.typeText(TRIP_DESTINATION))
                .perform(ViewActions.closeSoftKeyboard());
        // can't test with PickerActions, MaterialDatePicker uses complex custom layout
        Espresso.onView(ViewMatchers.withId(R.id.trip_start_date))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.trip_end_date))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText("OK"))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.trip_description))
                .perform(ViewActions.typeText(TRIP_DESCRIPTION))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.save_new_trip_button))
                .perform(ViewActions.click());
    }

    @After
    public void cleanup() {
        database.getReference("trips").removeEventListener(listener);
        database.getReference("trips").removeValue();
    }
}