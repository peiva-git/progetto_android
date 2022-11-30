package it.units.simandroid.progetto;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.widget.RecyclerView;

import it.units.simandroid.progetto.adapters.TripAdapter;


public class TripDetailsLookup extends ItemDetailsLookup<String> {

    private final RecyclerView recyclerView;

    public TripDetailsLookup(RecyclerView recyclerView)  {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof TripAdapter.ItemViewHolder) {
                return ((TripAdapter.ItemViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
