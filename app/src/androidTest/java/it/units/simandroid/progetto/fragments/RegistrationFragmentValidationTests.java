package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.fragments.NewTripFragmentValidationTests.*;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.units.simandroid.progetto.R;

@LargeTest
public class RegistrationFragmentValidationTests {

    private FragmentScenario<RegistrationFragment> scenario;
    private String fieldRequiredError;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(RegistrationFragment.class, Bundle.EMPTY, R.style.Theme_ProgettoSIMAndroid);
        scenario.onFragment(registrationFragment -> {
            fieldRequiredError = registrationFragment.getString(R.string.field_required);
        });
    }

    @Test
    public void checkErrorsWhenSubmittingEmptyForm() {
        Espresso.onView(ViewMatchers.withId(R.id.registration_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.user_name_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        Espresso.onView(ViewMatchers.withId(R.id.user_surname_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        Espresso.onView(ViewMatchers.withId(R.id.registration_email_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        Espresso.onView(ViewMatchers.withId(R.id.registration_password_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(fieldRequiredError)));
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
