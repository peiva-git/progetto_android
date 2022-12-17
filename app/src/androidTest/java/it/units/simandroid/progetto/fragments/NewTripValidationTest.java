package it.units.simandroid.progetto.fragments;

import android.os.Bundle;
import android.view.View;

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
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;

import it.units.simandroid.progetto.R;

@LargeTest
public class NewTripValidationTest {

    private FragmentScenario<NewTripFragment> scenario;
    private String expectedErrorText;

    @Test
    public void checkNewTripFragmentValidation() {
        scenario = FragmentScenario.launchInContainer(NewTripFragment.class, Bundle.EMPTY, R.style.Theme_ProgettoSIMAndroid);
        scenario.onFragment(newTripFragment -> expectedErrorText = newTripFragment.getResources().getString(R.string.field_required));

        Espresso.onView(ViewMatchers.withId(R.id.save_new_trip_button))
                .perform(ViewActions.click());

        // no dates picked, check if the alert dialog and error messages are shown
        Espresso.onView(ViewMatchers.withText(R.string.got_it))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.trip_name_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(expectedErrorText)));
        Espresso.onView(ViewMatchers.withId(R.id.trip_destination_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(expectedErrorText)));
        Espresso.onView(ViewMatchers.withId(R.id.trip_description_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(expectedErrorText)));
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }

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
}
