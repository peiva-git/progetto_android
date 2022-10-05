package it.units.simandroid.progetto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView tripsRecyclerView = findViewById(R.id.trips_recycler_view);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout navigationDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(view -> {
            Log.d("NAV", "Navigation icon clicked!");
            navigationDrawer.open();
        });

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            navigationDrawer.close();
            return true;
        });

        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip("Trieste trip", R.drawable.ic_baseline_account_circle_24, "This was a trip to Trieste"));
        TripAdapter tripAdapter = new TripAdapter(this, trips);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        tripsRecyclerView.setLayoutManager(linearLayoutManager);
        tripsRecyclerView.setAdapter(tripAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_account:
                return true;
            case R.id.settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}