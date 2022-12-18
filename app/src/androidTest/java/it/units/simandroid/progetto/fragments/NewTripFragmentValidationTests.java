package it.units.simandroid.progetto.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
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
public class NewTripFragmentValidationTests {

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
            expectedErrorText = newTripFragment.getString(R.string.field_required);
            fromString = newTripFragment.getString(R.string.from);
            untilString = newTripFragment.getString(R.string.until);
        });
    }

    @Test
    public void checkIfAlertDialogAndErrorMessagesAreDisplayed() {
        onView(withId(R.id.save_new_trip_button))
                .perform(click());
        // no dates picked, check if the alert dialog and error messages are displayed
        onView(withText(R.string.got_it))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        checkNewTripFormErrorMessages();
    }

    @Test
    public void checkIfPickDatesDialogIsDisplayed() {
        onView(withId(R.id.trip_dates))
                .perform(click());
        onView(withText(R.string.date_picker_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void pickDatesAndCheckIfButtonLabelIsUpdated() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        pickTripDates(startDate, endDate);
        String formattedStartDate = DateFormat.getDateInstance().format(new Date(startDate.getTimeInMillis()));
        String formattedEndDate = DateFormat.getDateInstance().format(new Date(endDate.getTimeInMillis()));
        String expectedLabel = String.format("%s: %s - %s: %s", fromString, formattedStartDate, untilString, formattedEndDate);
        onView(withId(R.id.trip_dates))
                .check(matches(withText(expectedLabel)));
    }

    @Test
    public void checkIfErrorMessagesAreDisplayedWhileDatesArePicked() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        pickTripDates(startDate, endDate);
        onView(withId(R.id.save_new_trip_button))
                .perform(click());
        checkNewTripFormErrorMessages();
    }

    @Test
    public void checkIfNoImagesAlertDialogIsDisplayedWhileDatesArePicked() {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        pickTripDates(startDate, endDate);
        onView(withId(R.id.trip_name))
                .perform(typeText(TRIP_NAME))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.trip_destination))
                .perform(typeText(TRIP_DESTINATION))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.trip_description))
                .perform(typeText(TRIP_DESCRIPTION))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.save_new_trip_button))
                .perform(click());
        onView(hasTextInputLayoutErrorText(expectedErrorText))
                .check(doesNotExist());
        onView(withText(R.string.picked_images_dialog_no))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
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
        onView(withId(R.id.trip_dates))
                .perform(click());
        onView(ViewMatchers.withContentDescription(startDateDescription))
                .perform(click());
        onView(ViewMatchers.withContentDescription(endDateDescription))
                .perform(click());
        onView(withText(SAVE_DATES))
                .perform(click());
    }

    private void checkNewTripFormErrorMessages() {
        onView(withId(R.id.trip_name_layout))
                .check(matches(hasTextInputLayoutErrorText(expectedErrorText)));
        onView(withId(R.id.trip_destination_layout))
                .check(matches(hasTextInputLayoutErrorText(expectedErrorText)));
        onView(withId(R.id.trip_description_layout))
                .check(matches(hasTextInputLayoutErrorText(expectedErrorText)));
    }
}
