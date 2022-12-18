package it.units.simandroid.progetto.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.ItemViewHolder> implements Filterable {

    private final Context context;
    private List<User> allUsers;
    private Set<String> authorizedUserIds;
    private final OnUserClickListener listener;

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldUser, @NonNull User newUser) {
            return oldUser.equals(newUser);
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldUser, @NonNull User newUser) {
            return oldUser.getId().equals(newUser.getId())
                    && oldUser.getEmail().equals(newUser.getEmail())
                    && oldUser.getName().equals(newUser.getName())
                    && oldUser.getSurname().equals(newUser.getSurname());
        }
    };
    private final AsyncListDiffer<User> differAllUsers = new AsyncListDiffer<>(this, DIFF_CALLBACK);

    public SelectUserAdapter(Context context, OnUserClickListener listener) {
        this.context = context;
        this.listener = listener;
        authorizedUserIds = new HashSet<>();
    }

    public void setAvailableUsers(List<User> users) {
        this.allUsers = users;
        differAllUsers.submitList(users);
    }

    public void setSelectedUserIds(Set<String> selectedUserIds) {
        authorizedUserIds = selectedUserIds;
    }

    public List<User> getVisibleUsers() {
        return differAllUsers.getCurrentList();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_info, parent, false);
        return new ItemViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        User user = differAllUsers.getCurrentList().get(position);
        holder.userEmail.setText(user.getEmail());
        holder.userNameSurname.setText(String.format("%s %s", user.getName(), user.getSurname()));
        holder.userSelected.setChecked(authorizedUserIds.contains(user.getId()));
    }

    @Override
    public int getItemCount() {
        return differAllUsers.getCurrentList().size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<User> filteredUsers = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredUsers.addAll(allUsers);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
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
                List<?> values = (List<?>) filterResults.values;
                List<User> filteringResult = new ArrayList<>(values.size());
                for (Object user : values) {
                    filteringResult.add((User) user);
                }
                differAllUsers.submitList(filteringResult);
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
