package it.units.simandroid.progetto.fragments;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import org.junit.Rule;

import it.units.simandroid.progetto.MainActivity;

@LargeTest
public class NewTripTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule = new ActivityScenarioRule<>(MainActivity.class);


}
