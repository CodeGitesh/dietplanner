package com.example.dietplanner.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dietplanner.data.HistoryItem;
import com.example.dietplanner.databinding.ListItemHistoryDayBinding;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> historyItems;

    public HistoryAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemHistoryDayBinding binding = ListItemHistoryDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(historyItems.get(position));
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemHistoryDayBinding binding;

        public ViewHolder(ListItemHistoryDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HistoryItem item) {
            binding.textViewHistoryDate.setText(item.date);
            binding.textViewHistoryCalories.setText(item.calories);
            binding.textViewHistoryMeals.setText(item.meals);
        }
    }
}