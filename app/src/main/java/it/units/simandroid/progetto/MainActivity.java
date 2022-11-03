package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import it.units.simandroid.progetto.fragments.SettingsFragment;
import it.units.simandroid.progetto.fragments.directions.TripsFragmentDirections;

public class MainActivity extends AppCompatActivity {

    public static final String FAVORITE_TRIPS_TAG = "FAV_TRIPS";
    private AppBarConfiguration appBarConfiguration;
    private MaterialToolbar toolbar;
    private DrawerLayout navigationDrawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        navigationDrawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
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
            if (destination.getId() == R.id.loginFragment) {
                toolbar.setVisibility(View.GONE);
                navigationView.setVisibility(View.GONE);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                navigationView.setVisibility(View.VISIBLE);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.favorites) {
                TripsFragmentDirections.FilterByFavoriteTripsAction action = TripsFragmentDirections.actionFilterByFavoriteTrips();
                action.setFilteringActive(true);
                Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                navigationDrawer.close();
                navController.navigate(action);
                return true;
            } else if (item.getItemId() == R.id.my_trips) {
                TripsFragmentDirections.FilterByFavoriteTripsAction action = TripsFragmentDirections.actionFilterByFavoriteTrips();
                action.setFilteringActive(false);
                Log.d(FAVORITE_TRIPS_TAG, action.isFilteringActive() ? "Filtering active" : "Filtering inactive");
                navigationDrawer.close();
                navController.navigate(action);
                return true;
            }
            return false;
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