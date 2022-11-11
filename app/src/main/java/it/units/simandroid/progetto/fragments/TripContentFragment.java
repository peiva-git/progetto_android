package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_ERROR;
import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.SlideshowPagerAdapter;
import it.units.simandroid.progetto.Trip;
import it.units.simandroid.progetto.User;
import it.units.simandroid.progetto.UserAdapter;
import it.units.simandroid.progetto.fragments.directions.TripContentFragmentArgs;

public class TripContentFragment extends Fragment {

    public static final String TRIP_TAG = "TRIP";
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private TextView tripName;
    private TextView tripDates;
    private TextView tripDestination;
    private TextView tripDescription;
    private FirebaseDatabase database;

    public TripContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance(DB_URL);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_trip_content, container, false);
        Trip trip = TripContentFragmentArgs.fromBundle(requireArguments()).getTrip();
        Log.d(TRIP_TAG, "Received trip as an argument to fragment " + getClass().getName() + ": " + trip.getName());

        viewPager = fragmentView.findViewById(R.id.trip_image_pager);
        tripName = fragmentView.findViewById(R.id.content_trip_name);
        tripDestination = fragmentView.findViewById(R.id.content_trip_destination);
        tripDates = fragmentView.findViewById(R.id.content_trip_dates);
        tripDescription = fragmentView.findViewById(R.id.content_trip_description);

        pagerAdapter = new SlideshowPagerAdapter(this, trip.getImagesUris());
        viewPager.setAdapter(pagerAdapter);

        requireActivity().addMenuProvider(new MenuProvider() {
            private EditText searchField;
            private RecyclerView recyclerView;
            private UserAdapter userAdapter;

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                MenuItem userAccount = menu.findItem(R.id.user_account);
                userAccount.setVisible(false);
                menuInflater.inflate(R.menu.top_app_bar_trip_content, menu);
                MaterialCheckBox isTripFavorite = (MaterialCheckBox) menu.findItem(R.id.favorite_trip).getActionView();
                isTripFavorite.setButtonIconDrawableResource(R.drawable.sl_baseline_favorite_24);
                isTripFavorite.setButtonDrawable(R.drawable.ic_baseline_favorite_border_24);
                isTripFavorite.setButtonIconTintList(ColorStateList.valueOf(com.google.android.material.R.attr.colorOnPrimary));
                isTripFavorite.setChecked(trip.isFavorite());

                isTripFavorite.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                    trip.setFavorite(isChecked);
                    String tripId = TripContentFragmentArgs.fromBundle(requireArguments()).getTrip().getId();
                    FirebaseDatabase.getInstance(DB_URL)
                            .getReference("trips")
                            .child(tripId)
                            .setValue(trip);
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.favorite_trip) {
                    return true;
                } else if (menuItem.getItemId() == R.id.share_trip) {
                    AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.share_trip_title)
                            .setMessage(R.string.share_trip_description)
                            .setNeutralButton(R.string.share_trip_cancel, (dialogInterface, i) -> dialogInterface.cancel())
                            .setPositiveButton(R.string.share_trip_confirm, (dialogInterface, i) -> {

                            })
                            .setView(R.layout.fragment_share_dialog)
                            .create();
                    dialog.show();

                    recyclerView = dialog.findViewById(R.id.users_recycler_view);
                    searchField = dialog.findViewById(R.id.search_field_text);

                    MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
                    recyclerView.addItemDecoration(divider);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(dialog.getContext(), LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(layoutManager);

                    database.getReference("users").get().addOnSuccessListener(snapshot -> {
                        GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {};
                        Map<String, Object> usersById = snapshot.getValue(type);
                        if (usersById != null) {
                            List<User> users = new ArrayList<>(usersById.values().size());
                            for (String key : usersById.keySet()) {
                                User retrievedUser = snapshot.child(key).getValue(User.class);
                                users.add(retrievedUser);
                            }
                            userAdapter = new UserAdapter(users);
                            recyclerView.setAdapter(userAdapter);
                        }
                    }).addOnFailureListener(exception -> {
                        Log.w(DB_ERROR, "Unable to retrieve user list: " + exception.getMessage());
                        Snackbar.make(requireView(), R.string.load_users_failed, Snackbar.LENGTH_SHORT).show();
                    });
                    searchField.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence oldText, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence newText, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            userAdapter.getFilter().filter(editable.toString());
                        }
                    });
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        tripName.setText(trip.getName());
        tripDestination.setText(trip.getDestination());
        tripDates.setText(String.format("%s - %s", trip.getStartDate(), trip.getEndDate()));
        tripDescription.setText(trip.getDescription());

        return fragmentView;
    }
}