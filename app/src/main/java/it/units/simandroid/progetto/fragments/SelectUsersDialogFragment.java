package it.units.simandroid.progetto.fragments;

import static it.units.simandroid.progetto.RealtimeDatabase.DB_ERROR;
import static it.units.simandroid.progetto.RealtimeDatabase.DB_URL;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.divider.MaterialDividerItemDecoration;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;
import it.units.simandroid.progetto.UserAdapter;

public class SelectUsersDialogFragment extends BottomSheetDialogFragment {

    public static final String GET_DB_USERS = "GET_DB_USERS";
    private RecyclerView recyclerView;
    private EditText searchField;
    private FirebaseDatabase database;
    private UserAdapter userAdapter;

    public static String TAG = "USER_SELECTION_DIALOG";

    public SelectUsersDialogFragment() {
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
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_select_users_dialog, container, false);
        recyclerView = fragmentView.findViewById(R.id.users_recycler_view);
        searchField = fragmentView.findViewById(R.id.search_field_text);

        userAdapter = new UserAdapter(Collections.emptyList());
        recyclerView.setAdapter(userAdapter);
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
                userAdapter.updateUsers(users);
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
                userAdapter.getFilter().filter(editable.toString());
            }
        });
        return fragmentView;
    }
}