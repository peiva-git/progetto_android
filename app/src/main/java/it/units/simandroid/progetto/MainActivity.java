package it.units.simandroid.progetto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView tripsRecyclerView = findViewById(R.id.trips_recycler_view);

        List<Trip> trips = new ArrayList<>();
        trips.add(new Trip("Trieste trip", R.drawable.ic_baseline_account_circle_24, "This was a trip to Trieste"));
        TripAdapter tripAdapter = new TripAdapter(this, trips);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        tripsRecyclerView.setLayoutManager(linearLayoutManager);
        tripsRecyclerView.setAdapter(tripAdapter);
    }
}