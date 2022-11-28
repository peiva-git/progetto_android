package it.units.simandroid.progetto;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.fragments.SettingsFragment.USER_NAME_KEY;
import static it.units.simandroid.progetto.fragments.SettingsFragment.USER_SURNAME_KEY;
import static it.units.simandroid.progetto.fragments.TripsFragment.PERMISSION_DIALOG_SHOWN;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import it.units.simandroid.progetto.fragments.directions.TripsFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;

public class MainActivity extends AppCompatActivity {

    public static final String FAVORITE_TRIPS_TAG = "FAV_TRIPS";
    private AppBarConfiguration appBarConfiguration;
    private MaterialToolbar toolbar;
    private DrawerLayout navigationDrawer;
    private NavigationView navigationView;
    private LinearProgressIndicator progressIndicator;
    private NavigationRailView navigationRailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        navigationDrawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationRailView = findViewById(R.id.navigation_rail);
        progressIndicator = findViewById(R.id.progress_indicator);
        setSupportActionBar(toolbar);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        if (navigationDrawer != null) {
            appBarConfiguration = new AppBarConfiguration
                    .Builder(navController.getGraph())
                    .setOpenableLayout(navigationDrawer)
                    .build();
        } else {
            appBarConfiguration = new AppBarConfiguration
                    .Builder(navController.getGraph())
                    .build();
        }
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        if (navigationView != null) {
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // check destination id and handle accordingly
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.selectUsersFragment) {
                toolbar.setVisibility(View.GONE);
                if (navigationView != null) {
                    navigationView.setVisibility(View.GONE);
                }
                if (navigationRailView != null) {
                    navigationRailView.setVisibility(View.GONE);
                }
            } else if (destination.getId() == R.id.tripsFragment) {
                toolbar.setVisibility(View.VISIBLE);
                if (navigationView != null) {
                    navigationView.setVisibility(View.VISIBLE);
                }
                if (navigationRailView != null) {
                    navigationRailView.setVisibility(View.VISIBLE);
                }
                if (arguments != null) {
                    if (TripsFragmentArgs.fromBundle(arguments).isFilteringActive()) {
                        toolbar.setTitle(R.string.trips_fragment_favorites_label);
                    } else if (TripsFragmentArgs.fromBundle(arguments).isSharedTripsModeActive()) {
                        toolbar.setTitle(R.string.trips_fragment_shared_label);
                    } else {
                        toolbar.setTitle(R.string.fragment_trips_label);
                    }
                }
            } else if (destination.getId() == R.id.settings
                    || destination.getId() == R.id.newTripFragment
                    || destination.getId() == R.id.tripContentFragment) {
                toolbar.setVisibility(View.VISIBLE);
                if (navigationView != null) {
                    navigationView.setVisibility(View.VISIBLE);
                }
                if (navigationRailView != null) {
                    navigationRailView.setVisibility(View.GONE);
                }
            } else {
                toolbar.setVisibility(View.VISIBLE);
                if (navigationView != null) {
                    navigationView.setVisibility(View.VISIBLE);
                }
                if (navigationRailView != null) {
                    navigationRailView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.favorites) {
                    TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterTrips();
                    action.setFilteringActive(true);
                    Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                    navigationDrawer.close();
                    navController.navigate(action);
                    return true;
                } else if (item.getItemId() == R.id.my_trips) {
                    TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterTrips();
                    action.setFilteringActive(false);
                    Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                    navigationDrawer.close();
                    navController.navigate(action);
                    return true;
                } else if (item.getItemId() == R.id.shared_trips) {
                    TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterTrips();
                    action.setSharedTripsModeActive(true);
                    Log.d("SHARED_TRIPS", "Filtering by shared trips");
                    navigationDrawer.close();
                    navController.navigate(action);
                    return true;
                } else if (item.getItemId() == R.id.settings) {
                    navigationDrawer.close();
                    navController.navigate(R.id.action_global_settings);
                    return true;
                }
                return false;
            });
        }

        if (navigationRailView != null) {
            navigationRailView.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.favorites) {
                    TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterTrips();
                    action.setFilteringActive(true);
                    Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                    navController.navigate(action);
                    return true;
                } else if (item.getItemId() == R.id.my_trips) {
                    TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterTrips();
                    action.setFilteringActive(false);
                    Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                    navController.navigate(action);
                    return true;
                } else if (item.getItemId() == R.id.shared_trips) {
                    TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterTrips();
                    action.setSharedTripsModeActive(true);
                    Log.d("SHARED_TRIPS_TAG", "Filtering by shared trips");
                    navController.navigate(action);
                    return true;
                }
                return false;
            });
            navigationRailView.getHeaderView().setOnClickListener(
                    view -> navController.navigate(TripsFragmentDirections.actionTripsFragmentToNewTripFragment()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        if (navigationRailView != null) {
            MenuItem settingsItem = menu.add(Menu.NONE, R.id.settings, Menu.NONE, R.string.settings);
            settingsItem.setIcon(R.drawable.ic_baseline_settings_24);
            settingsItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        // automatic navigation if the menu item and the fragment item in the graph have the same id
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        if (item.getItemId() == R.id.loginFragment) {
            FirebaseAuth authentication = FirebaseAuth.getInstance();
            authentication.signOut();
        } else if (item.getTitle().equals(getString(R.string.settings))) {
            navController.navigate(R.id.action_global_settings);
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PERMISSION_DIALOG_SHOWN, false);
        editor.apply();
    }

}