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

    public static final String CORRECT_FORMAT_EMAIL = "test@units.it";
    public static final String WRONG_FORMAT_EMAIL = "test";
    public static final String SHORT_PASSWORD = "pass";
    public static final String CORRECT_PASSWORD = "password";

    private FragmentScenario<RegistrationFragment> scenario;
    private String fieldRequiredError;
    private String emailsMismatchError;
    private String passwordsMismatchError;
    private String emailFormatError;
    private String passwordLengthError;

    @Before
    public void init() {
        scenario = FragmentScenario.launchInContainer(RegistrationFragment.class, Bundle.EMPTY, R.style.Theme_ProgettoSIMAndroid);
        scenario.onFragment(registrationFragment -> {
            fieldRequiredError = registrationFragment.getString(R.string.field_required);
            emailsMismatchError = registrationFragment.getString(R.string.email_mismatch);
            passwordsMismatchError = registrationFragment.getString(R.string.password_mismatch);
            emailFormatError = registrationFragment.getString(R.string.email_bad_format);
            passwordLengthError = registrationFragment.getString(R.string.password_weak);
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

    @Test
    public void checkErrorsWhenEmailsAreDifferent() {
        Espresso.onView(ViewMatchers.withId(R.id.registration_email))
                .perform(ViewActions.typeText(CORRECT_FORMAT_EMAIL))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.registration_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.registration_email_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(emailsMismatchError)));
        Espresso.onView(ViewMatchers.withId(R.id.registration_email_confirm_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(emailsMismatchError)));
    }

    @Test
    public void checkErrorsWhenPasswordsAreDifferent() {
        Espresso.onView(ViewMatchers.withId(R.id.registration_password))
                .perform(ViewActions.typeText(CORRECT_PASSWORD))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.registration_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.registration_password_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(passwordsMismatchError)));
        Espresso.onView(ViewMatchers.withId(R.id.registration_password_confirm_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(passwordsMismatchError)));
    }

    @Test
    public void checkEmailFormatError() {
        Espresso.onView(ViewMatchers.withId(R.id.registration_email))
                .perform(ViewActions.typeText(WRONG_FORMAT_EMAIL))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.registration_email_confirm))
                .perform(ViewActions.typeText(WRONG_FORMAT_EMAIL))
                .perform(ViewActions.closeSoftKeyboard());
        Espresso.onView(ViewMatchers.withId(R.id.registration_button))
                .perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.registration_email_layout))
                .check(ViewAssertions.matches(hasTextInputLayoutErrorText(emailFormatError)));
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
