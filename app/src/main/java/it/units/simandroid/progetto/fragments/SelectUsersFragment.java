package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_ERROR;
import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
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

        selectUserAdapter = new SelectUserAdapter(getContext(), Collections.emptyList(), Collections.emptyList(), new OnUserClickListener() {
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

        Tasks.whenAllComplete(database.getReference("trips").child(tripId).child("authorizedUsers").get(),
                database.getReference("users").get()).addOnSuccessListener(tasks -> {
            Task<DataSnapshot> getAuthorizedUsersTask = (Task<DataSnapshot>) tasks.get(0);
            Task<DataSnapshot> getUsersTask = (Task<DataSnapshot>) tasks.get(1);

            GenericTypeIndicator<List<String>> listType = new GenericTypeIndicator<List<String>>() {
            };
            List<String> authorizedUsers = getAuthorizedUsersTask.getResult().getValue(listType);
            if (authorizedUsers != null) {
                selectUserAdapter.updateAuthorizedUserIds(authorizedUsers);
            } else {
                Log.d("GET_TRIP", "List of authorized users unavailable for this trip");
            }

            GenericTypeIndicator<Map<String, Object>> mapType = new GenericTypeIndicator<Map<String, Object>>() {};
            Map<String, Object> usersById = getUsersTask.getResult().getValue(mapType);
            if (usersById != null) {
                List<User> users = new ArrayList<>(usersById.values().size());
                for (String key : usersById.keySet()) {
                    User retrievedUser = getUsersTask.getResult().child(key).getValue(User.class);
                    users.add(retrievedUser);
                    Log.d(GET_DB_USERS, "User with id " + key + " added to list");
                }
                selectUserAdapter.setAvailableUsers(users);
            } else {
                Log.d(GET_DB_USERS, "No users found");
            }
        }).addOnFailureListener(e -> {
            Log.w(DB_ERROR, "Unable to retrieve user list: " + e.getMessage());
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
                Log.d("FILTER", "Filter string is: " + editable);
                selectUserAdapter.getFilter().filter(editable.toString());
            }
        });

        negativeButton.setOnClickListener(view -> NavHostFragment.findNavController(this).navigateUp());
        positiveButton.setOnClickListener(
                view -> database.getReference("trips").child(tripId).child("authorizedUsers").setValue(selectedUsers)
                        .addOnSuccessListener(
                                task -> {
                                    NavHostFragment.findNavController(this).navigateUp();
                                    Snackbar.make(requireActivity().findViewById(R.id.activity_layout), R.string.trip_shared, Snackbar.LENGTH_SHORT).show();
                                })
                        .addOnFailureListener(
                                exception -> {
                                    NavHostFragment.findNavController(this).navigateUp();
                                    Snackbar.make(requireActivity().findViewById(R.id.activity_layout), R.string.trip_shared_error, Snackbar.LENGTH_SHORT).show();
                                }));
        return fragmentView;
    }
}
