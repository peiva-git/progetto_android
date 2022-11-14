package it.units.simandroid.progetto.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import it.units.simandroid.progetto.R;
import it.units.simandroid.progetto.User;

public class SelectedUserAdapter extends RecyclerView.Adapter<SelectedUserAdapter.ItemViewHolder> {

    private List<User> pickedUsers;

    public SelectedUserAdapter(List<User> pickedUsers) {
        this.pickedUsers = pickedUsers;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_info, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        User user = pickedUsers.get(position);
        holder.userEmail.setText(user.getEmail());
        holder.userNameSurname.setText(String.format("%s %s", user.getName(), user.getSurname()));
    }

    @Override
    public int getItemCount() {
        return pickedUsers.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView userNameSurname;
        private final MaterialTextView userEmail;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameSurname = itemView.findViewById(R.id.user_name_surname);
            userEmail = itemView.findViewById(R.id.user_email);
        }
    }
}
