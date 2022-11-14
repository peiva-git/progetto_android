package it.units.simandroid.progetto.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import it.units.simandroid.progetto.R;

public class SelectedUsersFragment extends Fragment {

    private RecyclerView recyclerView;

    public SelectedUsersFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_selected_users, container, false);
        recyclerView = fragmentView.findViewById(R.id.selected_users_recycler_view);

        return fragmentView;
    }
}
