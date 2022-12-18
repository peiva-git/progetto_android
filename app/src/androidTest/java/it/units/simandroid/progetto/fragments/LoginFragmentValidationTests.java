package it.units.simandroid.progetto.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static it.units.simandroid.progetto.fragments.NewTripFragmentValidationTests.hasTextInputLayoutErrorText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.units.simandroid.progetto.R;

@LargeTest
public class LoginFragmentValidationTests {

    private FragmentScenario<LoginFragment> scenario;
    private String fieldRequiredError;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(LoginFragment.class, Bundle.EMPTY, R.style.Theme_ProgettoSIMAndroid);
        scenario.onFragment(loginFragment -> fieldRequiredError = loginFragment.getString(R.string.field_required));
    }

    @Test
    public void checkErrorsOnEmptyForm() {
        onView(withId(R.id.login_button))
                .perform(click());
        onView(withId(R.id.login_username_layout))
                .check(matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        onView(withId(R.id.login_password_layout))
                .check(matches(hasTextInputLayoutErrorText(fieldRequiredError)));
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
