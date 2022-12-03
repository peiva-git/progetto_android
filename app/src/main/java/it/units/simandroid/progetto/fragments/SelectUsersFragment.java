package it.units.simandroid.progetto.fragments;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.viewmodels.TripsViewModel;
import it.units.simandroid.progetto.User;
import it.units.simandroid.progetto.viewmodels.UsersViewModel;
import it.units.simandroid.progetto.adapters.OnUserClickListener;
import it.units.simandroid.progetto.adapters.SelectUserAdapter;
import it.units.simandroid.progetto.fragments.directions.SelectUsersFragmentArgs;

public class SelectUsersFragment extends Fragment implements OnUserClickListener {

    public static final String SELECT_USER_TAG = "SELECT_USER";
    private RecyclerView recyclerView;
    private EditText searchField;
    private MaterialButton negativeButton;
    private SelectUserAdapter selectUserAdapter;
    private Set<String> selectedUserIds;
    private MaterialButton positiveButton;
    private FirebaseAuth authentication;

    public SelectUsersFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedUserIds = new HashSet<>();
        authentication = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_select_users, container, false);
        String tripId = SelectUsersFragmentArgs.fromBundle(requireArguments()).getTripId();
        recyclerView = fragmentView.findViewById(R.id.users_recycler_view);
        searchField = fragmentView.findViewById(R.id.search_field_text);
        negativeButton = fragmentView.findViewById(R.id.dialog_negative_button);
        positiveButton = fragmentView.findViewById(R.id.dialog_positive_button);

        selectUserAdapter = new SelectUserAdapter(getContext(), Collections.emptyList(), selectedUserIds, this);
  /*
            @Override
            public void onUserClick(User user) {
            }

            @Override
            public void onUserCheckedStateChanged(User user, CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    selectedUserIds.add(user.getId());
                    Log.d(SELECT_USER_TAG, "User " + user.getId() + " added to candidates list");
                } else {
                    if (selectedUserIds.remove(user.getId())) {
                        Log.d(SELECT_USER_TAG, "User " + user.getId() + " removed from candidates list");
                    } else {
                        Log.d(SELECT_USER_TAG, "User " + user.getId() + " already not in candidates list");
                    }
                }
            }
*/
        recyclerView.setAdapter(selectUserAdapter);
        MaterialDividerItemDecoration divider = new MaterialDividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(divider);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        UsersViewModel usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        TripsViewModel tripsViewModel = new ViewModelProvider(this).get(TripsViewModel.class);
        tripsViewModel.getAuthorizedUserIds(tripId).observe(getViewLifecycleOwner(), authorizationsByUserId -> {
            Log.d(SELECT_USER_TAG, "Authorized users database data changed, setting up selected users list");
            if (authorizationsByUserId != null) {
                for (String userId : authorizationsByUserId.keySet()) {
                    Boolean isUserAuthorized = authorizationsByUserId.get(userId);
                    if (isUserAuthorized != null && isUserAuthorized) {
                        selectedUserIds.add(userId);
                    } else {
                        selectedUserIds.remove(userId);
                    }
                }
            }
        });
        usersViewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            for (User user : users) {
                if (user.getId().equals(authentication.getUid())) {
                    users.remove(user);
                    break;
                }
            }
            Log.d(SELECT_USER_TAG, "New list of users available, setting up adapter");
            selectUserAdapter.setAvailableUsers(users);
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
        positiveButton.setOnClickListener(view ->
                tripsViewModel.shareTripWithUsers(tripId, selectedUserIds)
                .addOnCompleteListener(setAuthorizedUsersTask -> {
            if (setAuthorizedUsersTask.isSuccessful()) {
                Snackbar.make(SelectUsersFragment.this.requireActivity().findViewById(R.id.activity_layout), R.string.trip_shared, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(SelectUsersFragment.this.requireActivity().findViewById(R.id.activity_layout), R.string.trip_shared_error, Snackbar.LENGTH_SHORT).show();
            }
            NavHostFragment.findNavController(this).navigateUp();
        }));
        return fragmentView;
    }

    @Override
    public void onUserClick(int position) {
    }

    @Override
    public void onUserCheckedStateChanged(int position, CompoundButton compoundButton, boolean isChecked) {
        User selectedUser = selectUserAdapter.getVisibleUsers().get(position);
        if (isChecked) {
            selectedUserIds.add(selectedUser.getId());
            Log.d(SELECT_USER_TAG, "User " + selectedUser.getId() + " added to candidates list");
        } else {
            if (selectedUserIds.remove(selectedUser.getId())) {
                Log.d(SELECT_USER_TAG, "User " + selectedUser.getId() + " removed from candidates list");
            } else {
                Log.d(SELECT_USER_TAG, "User " + selectedUser.getId() + " already not in candidates list");
            }
        }
    }
}
