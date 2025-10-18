package com.example.dietplanner;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dietplanner.data.FoodItem;
import com.example.dietplanner.databinding.ListItemCustomMealBinding;
import java.util.List;
import java.util.Locale;

public class CustomMealAdapter extends RecyclerView.Adapter<CustomMealAdapter.ViewHolder> {

    private final List<FoodItem> foodItems;
    private final OnItemSelectionChangedListener listener;

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged();
    }

    public CustomMealAdapter(List<FoodItem> foodItems, OnItemSelectionChangedListener listener) {
        this.foodItems = foodItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemCustomMealBinding binding = ListItemCustomMealBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemCustomMealBinding binding;

        public ViewHolder(ListItemCustomMealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodItem item) {
            binding.checkboxFoodItem.setText(item.name);
            binding.textViewFoodCalories.setText(String.format(Locale.US, "%.0f kcal", item.calories));

            binding.checkboxFoodItem.setOnCheckedChangeListener(null);
            binding.checkboxFoodItem.setChecked(item.isSelected);

            binding.checkboxFoodItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.isSelected = isChecked;
                listener.onSelectionChanged();
            });
        }
    }
}