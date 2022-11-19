package it.units.simandroid.progetto;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;
import static it.units.simandroid.progetto.fragments.SettingsFragment.USER_NAME_KEY;
import static it.units.simandroid.progetto.fragments.SettingsFragment.USER_SURNAME_KEY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;

import it.units.simandroid.progetto.fragments.directions.TripsFragmentArgs;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;

public class MainActivity extends AppCompatActivity {

    public static final String FAVORITE_TRIPS_TAG = "FAV_TRIPS";
    private AppBarConfiguration appBarConfiguration;
    private MaterialToolbar toolbar;
    private DrawerLayout navigationDrawer;
    private NavigationView navigationView;
    private LinearProgressIndicator progressIndicator;
    private FirebaseAuth authentication;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authentication = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance(DB_URL);

        toolbar = findViewById(R.id.toolbar);
        navigationDrawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        progressIndicator = findViewById(R.id.progress_indicator);
        setSupportActionBar(toolbar);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration
                .Builder(navController.getGraph())
                .setOpenableLayout(navigationDrawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // check destination id and handle accordingly
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.selectUsersFragment) {
                toolbar.setVisibility(View.GONE);
                navigationView.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.tripsFragment) {
                toolbar.setVisibility(View.VISIBLE);
                navigationView.setVisibility(View.VISIBLE);
                if (arguments != null) {
                    if (TripsFragmentArgs.fromBundle(arguments).isFilteringActive()) {
                        toolbar.setTitle(R.string.trips_fragment_favorites_label);
                    } else if (TripsFragmentArgs.fromBundle(arguments).isSharedTripsModeActive()) {
                        toolbar.setTitle(R.string.trips_fragment_shared_label);
                    } else {
                        toolbar.setTitle(R.string.fragment_trips_label);
                    }
                }
            } else {
                toolbar.setVisibility(View.VISIBLE);
                navigationView.setVisibility(View.VISIBLE);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.favorites) {
                TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterByFavoriteTrips();
                action.setFilteringActive(true);
                Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                navigationDrawer.close();
                navController.navigate(action);
                return true;
            } else if (item.getItemId() == R.id.my_trips) {
                TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterByFavoriteTrips();
                action.setFilteringActive(false);
                Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                navigationDrawer.close();
                navController.navigate(action);
                return true;
            } else if (item.getItemId() == R.id.shared_trips) {
                TripsFragmentDirections.FilterTripsAction action = TripsFragmentDirections.actionFilterByFavoriteTrips();
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

        database.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (authentication.getUid() != null) {
                    String userName = snapshot
                            .child(authentication.getUid())
                            .child("name")
                            .getValue(String.class);
                    String userSurname = snapshot
                            .child(authentication.getUid())
                            .child("surname")
                            .getValue(String.class);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if (key.equals(USER_NAME_KEY)) {
                database.getReference("users")
                        .child(Objects.requireNonNull(authentication.getUid()))
                        .child("name")
                        .setValue(sharedPreferences.getString(key, ""));
                Log.i("SETTINGS", "Preference value was updated to: " + sharedPreferences.getString(key, ""));
            } else if (key.equals(USER_SURNAME_KEY)) {
                database.getReference("users")
                        .child(Objects.requireNonNull(authentication.getUid()))
                        .child("surname")
                        .setValue(sharedPreferences.getString(key, ""));
                Log.i("SETTINGS", "Preference value was updated to: " + sharedPreferences.getString(key, ""));
            } else {
                Log.w("SETTINGS", "No preference key matching");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        // automatic navigation if the menu item and the fragment item in the graph have the same id
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        if (item.getItemId() == R.id.loginFragment) {
            FirebaseAuth authentication = FirebaseAuth.getInstance();
            authentication.signOut();
        }
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}