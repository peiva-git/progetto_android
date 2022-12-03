package it.units.simandroid.progetto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.ItemViewHolder> implements Filterable {

    private Context context;
    private List<User> allUsers;
    private List<User> filteredUsers;
    private final Set<String> authorizedUserIds;
    private final OnUserClickListener listener;

    public SelectUserAdapter(Context context, List<User> allUsers, Set<String> authorizedUserIds, OnUserClickListener listener) {
        this.context = context;
        this.allUsers = allUsers;
        this.filteredUsers = this.allUsers;
        this.authorizedUserIds = authorizedUserIds;
        this.listener = listener;
    }

    public void setAvailableUsers(List<User> users) {
        this.allUsers = users;
        this.filteredUsers = users;
        notifyDataSetChanged();
    }

    public List<User> getVisibleUsers() {
        return filteredUsers;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_info, parent, false);
        return new ItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        User user = filteredUsers.get(position);
        holder.userEmail.setText(user.getEmail());
        holder.userNameSurname.setText(String.format("%s %s", user.getName(), user.getSurname()));
        holder.userSelected.setChecked(authorizedUserIds.contains(user.getId()));
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
                    filteredUsers = allUsers;
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    filteredUsers = new ArrayList<>();
                    for (User user : allUsers) {
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

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
        private final MaterialTextView userNameSurname;
        private final MaterialTextView userEmail;
        private final MaterialCheckBox userSelected;
        private final OnUserClickListener listener;

        public ItemViewHolder(@NonNull View itemView, OnUserClickListener listener) {
            super(itemView);
            userNameSurname = itemView.findViewById(R.id.user_name_surname);
            userEmail = itemView.findViewById(R.id.user_email);
            userSelected = itemView.findViewById(R.id.user_selected_checkbox);

            this.listener = listener;
            itemView.setOnClickListener(this);
            userSelected.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if (listener != null) {
                listener.onUserCheckedStateChanged(getBindingAdapterPosition(), compoundButton, isChecked);
            }
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                userSelected.setChecked(!userSelected.isChecked());
                listener.onUserClick(getBindingAdapterPosition());
            }
        }
    }
}
