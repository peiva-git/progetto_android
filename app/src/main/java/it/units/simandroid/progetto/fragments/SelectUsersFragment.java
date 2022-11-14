package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_ERROR;
import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.units.simandroid.progetto.adapters.OnUserClickListener;
import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;
import it.units.simandroid.progetto.adapters.SelectUserAdapter;
import it.units.simandroid.progetto.fragments.directions.SelectUsersFragmentArgs;

public class SelectUsersFragment extends Fragment {

    private static final String GET_DB_USERS = "GET_DB_USERS";
    private RecyclerView recyclerView;
    private EditText searchField;
    private MaterialButton negativeButton;
    private SelectUserAdapter selectUserAdapter;
    private FirebaseDatabase database;
    private List<User> selectedUsers;
    private MaterialButton positiveButton;

    public SelectUsersFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance(DB_URL);
        selectedUsers = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_select_users, container, false);
        String tripId = SelectUsersFragmentArgs.fromBundle(requireArguments()).getTripId();
        recyclerView = fragmentView.findViewById(R.id.users_recycler_view);
        searchField = fragmentView.findViewById(R.id.search_field_text);
        negativeButton = fragmentView.findViewById(R.id.dialog_negative_button);
        positiveButton = fragmentView.findViewById(R.id.dialog_positive_button);

        selectUserAdapter = new SelectUserAdapter(Collections.emptyList(), new OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
            }

            @Override
            public void onUserCheckedStateChanged(User user, CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    selectedUsers.add(user);
                    Log.d("CHECKED_USERS", "User " + user.getEmail() + " added to list");
                } else {
                    if (selectedUsers.remove(user)) {
                        Log.d("CHECKED_USERS", "User " + user.getEmail() + " removed from list");
                    } else {
                        Log.d("CHEKED_USERS", "User " + user.getEmail() + " not in list");
                    }
                }
                positiveButton.setEnabled(selectedUsers.size() > 0);
            }
        });

        recyclerView.setAdapter(selectUserAdapter);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(divider);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        database.getReference("users").get().addOnSuccessListener(snapshot -> {
            GenericTypeIndicator<Map<String, Object>> type = new GenericTypeIndicator<Map<String, Object>>() {
            };
            Map<String, Object> usersById = snapshot.getValue(type);
            if (usersById != null) {
                List<User> users = new ArrayList<>(usersById.values().size());
                for (String key : usersById.keySet()) {
                    User retrievedUser = snapshot.child(key).getValue(User.class);
                    users.add(retrievedUser);
                    Log.d(GET_DB_USERS, "User with id " + key + " added to list");
                }
                selectUserAdapter.updateUsers(users);
            } else {
                Log.d(GET_DB_USERS, "No users found");
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
                ImageSpan[] chipSpans = editable.getSpans(0, editable.length(), ImageSpan.class);
                if (chipSpans.length != 0) {
                    int startIndex = editable.getSpanEnd(chipSpans[chipSpans.length - 1]);
                    CharSequence filterSequence = editable.subSequence(startIndex, editable.length());
                    Log.d("FILTER", "Filter string is: " + filterSequence);
                    selectUserAdapter.getFilter().filter(filterSequence.toString());
                } else {
                    selectUserAdapter.getFilter().filter(editable.toString());
                }
            }
        });

        negativeButton.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        positiveButton.setOnClickListener(
                view -> database.getReference("trips").child(tripId).child("authorizedUsers").setValue(selectedUsers)
                .addOnSuccessListener(
                        task -> Snackbar.make(requireView(), R.string.trip_shared, Snackbar.LENGTH_SHORT).show())
                .addOnFailureListener(
                        exception -> Snackbar.make(requireView(), R.string.trip_shared_error, Snackbar.LENGTH_SHORT).show()));
        return fragmentView;
    }
}
