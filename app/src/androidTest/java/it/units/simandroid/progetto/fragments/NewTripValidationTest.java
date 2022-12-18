package it.units.simandroid.progetto.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.Contract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.units.simandroid.progetto.R;

@LargeTest
public class NewTripValidationTest {

    public static final int START_DATE_DAY_OF_MONTH = 25;
    public static final int END_DATE_DAY_OF_MONTH = 26;
    public static final String SAVE_DATES = "Save";
    public static final String TRIP_NAME = "My trip name";
    public static final String TRIP_DESTINATION = "Trieste";
    public static final String TRIP_DESCRIPTION = "My favorite trip so far!";

    private FragmentScenario<NewTripFragment> scenario;
    private String expectedErrorText;
    private String fromString;
    private String untilString;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(NewTripFragment.class, Bundle.EMPTY, R.style.Theme_ProgettoSIMAndroid);
        scenario.onFragment(newTripFragment -> {
            expectedErrorText = newTripFragment.getResources().getString(R.string.field_required);
            fromString = newTripFragment.getResources().getString(R.string.from);
            untilString = newTripFragment.getResources().getString(R.string.until);
        });
    }

    @Test
    public void checkIfAlertDialogAndErrorMessagesAreDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.save_new_trip_button))
                .perform(ViewActions.click());
        // no dates picked, check if the alert dialog and error messages are displayed
        Espresso.onView(ViewMatchers.withText(R.string.got_it))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click());
        checkNewTripFormErrorMessages();
    }

    @Test
    public void checkIfPickDatesDialogIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.trip_dates))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(R.string.date_picker_title))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void pickDatesAndCheckIfButtonLabelIsUpdated() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        pickTripDates(startDate, endDate);
        String formattedStartDate = DateFormat.getDateInstance().format(new Date(startDate.getTimeInMillis()));
        String formattedEndDate = DateFormat.getDateInstance().format(new Date(endDate.getTimeInMillis()));
        String expectedLabel = String.format("%s: %s - %s: %s", fromString, formattedStartDate, untilString, formattedEndDate);
        Espresso.onView(ViewMatchers.withId(R.id.trip_dates))
                .check(ViewAssertions.matches(ViewMatchers.withText(expectedLabel)));
    }

    @Test
    public void checkIfErrorMessagesAreDisplayedWhileDatesArePicked() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        pickTripDates(startDate, endDate);
        Espresso.onView(ViewMatchers.withId(R.id.save_new_trip_button))
                .perform(ViewActions.click());
        checkNewTripFormErrorMessages();
    }

    @Test
    public void checkIfNoImagesAlertDialogIsDisplayedWhileDatesArePicked() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        pickTripDates(startDate, endDate);
        Espresso.onView(ViewMatchers.withId(R.id.trip_name))
                .perform(ViewActions.typeText(TRIP_NAME))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_destination))
                .perform(ViewActions.typeText(TRIP_DESTINATION))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.trip_description))
                .perform(ViewActions.typeText(TRIP_DESCRIPTION))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.save_new_trip_button))
                .perform(ViewActions.click());
        Espresso.onView(hasTextInputLayoutErrorText(expectedErrorText))
                .check(ViewAssertions.doesNotExist());
        Espresso.onView(ViewMatchers.withText(R.string.picked_images_dialog_no))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click());
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }

    @NonNull
    @Contract("_ -> new")
    public static Matcher<View> hasTextInputLayoutErrorText(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (!(item instanceof TextInputLayout)) {
                    return false;
                }
                CharSequence error = ((TextInputLayout) item).getError();

                if (error == null) {
                    return false;
                }
                String hint = error.toString();
                return expectedErrorText.equals(hint);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    private void pickTripDates(Calendar startDate, Calendar endDate) {
        startDate.set(Calendar.DAY_OF_MONTH, START_DATE_DAY_OF_MONTH);
        endDate.set(Calendar.DAY_OF_MONTH, END_DATE_DAY_OF_MONTH);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.ENGLISH);
        String startDateDescription = simpleDateFormat.format(new Date(startDate.getTimeInMillis()));
        String endDateDescription = simpleDateFormat.format(new Date(endDate.getTimeInMillis()));
        Espresso.onView(ViewMatchers.withId(R.id.trip_dates))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withContentDescription(startDateDescription))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withContentDescription(endDateDescription))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withText(SAVE_DATES))
                .perform(ViewActions.click());
    }

    private void checkNewTripFormErrorMessages() {
        Espresso.onView(ViewMatchers.withId(R.id.trip_name_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(expectedErrorText)));
        Espresso.onView(ViewMatchers.withId(R.id.trip_destination_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(expectedErrorText)));
        Espresso.onView(ViewMatchers.withId(R.id.trip_description_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(expectedErrorText)));
    }
}
