package it.units.simandroid.progetto.fragments;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static it.units.simandroid.progetto.fragments.NewTripFragmentValidationTests.*;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
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
        onView(withId(R.id.registration_button))
                .perform(click());
        onView(withId(R.id.user_name_layout))
                .check(matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        onView(withId(R.id.user_surname_layout))
                .check(matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        onView(withId(R.id.registration_email_layout))
                .check(matches(hasTextInputLayoutErrorText(fieldRequiredError)));
        onView(withId(R.id.registration_password_layout))
                .check(matches(hasTextInputLayoutErrorText(fieldRequiredError)));
    }

    @Test
    public void checkErrorsWhenEmailsAreDifferent() {
        onView(withId(R.id.registration_email))
                .perform(typeText(CORRECT_FORMAT_EMAIL))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.registration_button))
                .perform(click());
        onView(withId(R.id.registration_email_layout))
                .check(matches(hasTextInputLayoutErrorText(emailsMismatchError)));
        onView(withId(R.id.registration_email_confirm_layout))
                .check(matches(hasTextInputLayoutErrorText(emailsMismatchError)));
    }

    @Test
    public void checkErrorsWhenPasswordsAreDifferent() {
        onView(withId(R.id.registration_password))
                .perform(typeText(CORRECT_PASSWORD))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.registration_button))
                .perform(click());
        onView(withId(R.id.registration_password_layout))
                .check(matches(hasTextInputLayoutErrorText(passwordsMismatchError)));
        onView(withId(R.id.registration_password_confirm_layout))
                .check(matches(hasTextInputLayoutErrorText(passwordsMismatchError)));
    }

    @Test
    public void checkEmailFormatError() {
        onView(withId(R.id.registration_email))
                .perform(typeText(WRONG_FORMAT_EMAIL))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.registration_email_confirm))
                .perform(typeText(WRONG_FORMAT_EMAIL))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.registration_button))
                .perform(click());
        onView(withId(R.id.registration_email_layout))
                .check(matches(hasTextInputLayoutErrorText(emailFormatError)));
    }

    @Test
    public void checkPasswordStrengthError() {
        onView(withId(R.id.registration_password))
                .perform(typeText(SHORT_PASSWORD))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.registration_password_confirm))
                .perform(typeText(SHORT_PASSWORD))
                .perform(closeSoftKeyboard());
        onView(withId(R.id.registration_button))
                .perform(click());
        onView(withId(R.id.registration_password_layout))
                .check(matches(hasTextInputLayoutErrorText(passwordLengthError)));
    }

    @After
    public void cleanup() {
        if (scenario != null) {
            scenario.close();
        }
    }
}
