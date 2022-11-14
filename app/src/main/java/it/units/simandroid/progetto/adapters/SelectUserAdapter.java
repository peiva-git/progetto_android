package it.units.simandroid.progetto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.ItemViewHolder> implements Filterable {

    private List<User> users;
    private List<User> filteredUsers;
    private final OnUserClickListener listener;

    public SelectUserAdapter(List<User> users, OnUserClickListener listener) {
        this.users = users;
        this.filteredUsers = users;
        this.listener = listener;
    }

    public void updateUsers(List<User> users) {
        this.users = users;
        filteredUsers = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_info, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        User user = filteredUsers.get(position);
        holder.userEmail.setText(user.getEmail());
        holder.userNameSurname.setText(String.format("%s %s", user.getName(), user.getSurname()));

        holder.userSelected.setOnCheckedChangeListener(
                (compoundButton, isChecked) -> listener.onUserCheckedStateChanged(user, compoundButton, isChecked));

        holder.itemView.setOnClickListener(view -> {
            holder.userSelected.setChecked(!holder.userSelected.isChecked());
            listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return filteredUsers.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint == null || constraint.length() == 0) {
                    filteredUsers = users;
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    filteredUsers = new ArrayList<>();
                    for (User user : users) {
                        String userNameAndSurname = user.getName().toLowerCase() + user.getSurname().toLowerCase();
                        if (userNameAndSurname.contains(filterPattern)) {
                            filteredUsers.add(user);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredUsers;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                List<User> filteringResult = (List<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView userNameSurname;
        private final MaterialTextView userEmail;
        private final MaterialCheckBox userSelected;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameSurname = itemView.findViewById(R.id.user_name_surname);
            userEmail = itemView.findViewById(R.id.user_email);
            userSelected = itemView.findViewById(R.id.user_selected_checkbox);
        }
    }
}
