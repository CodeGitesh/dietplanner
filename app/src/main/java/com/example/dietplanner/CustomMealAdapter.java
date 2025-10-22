package com.example.dietplanner;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dietplanner.data.FoodItem;
import com.example.dietplanner.databinding.ListItemCustomMealBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CustomMealAdapter extends RecyclerView.Adapter<CustomMealAdapter.ViewHolder> {

    private final List<FoodItem> foodItemsFull;
    private List<FoodItem> foodItemsFiltered;
    private final OnItemSelectionChangedListener listener;

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged();
    }

    public CustomMealAdapter(List<FoodItem> foodItems, OnItemSelectionChangedListener listener) {
        this.foodItemsFull = new ArrayList<>(foodItems);
        this.foodItemsFiltered = new ArrayList<>(foodItems);
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
        holder.bind(foodItemsFiltered.get(position));
    }

    @Override
    public int getItemCount() {
        return foodItemsFiltered.size();
    }

    public void filter(String text) {
        foodItemsFiltered.clear();
        if (text.isEmpty()) {
            foodItemsFiltered.addAll(foodItemsFull);
        } else {
            text = text.toLowerCase().trim();
            for (FoodItem item : foodItemsFull) {
                if (item.name.toLowerCase().contains(text)) {
                    foodItemsFiltered.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemCustomMealBinding binding;

        public ViewHolder(ListItemCustomMealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodItem item) {
            binding.textViewFoodName.setText(item.name);
            binding.textViewFoodCalories.setText(String.format(Locale.US, "%.0f kcal (per 100g)", item.caloriesPer100g));
            binding.textViewQuantity.setText(String.format(Locale.US, "%dg", item.quantity));

            binding.buttonPlus.setOnClickListener(v -> {
                item.quantity += 50; // Add in 50g increments
                binding.textViewQuantity.setText(String.format(Locale.US, "%dg", item.quantity));
                listener.onSelectionChanged();
            });

            binding.buttonMinus.setOnClickListener(v -> {
                if (item.quantity > 0) {
                    item.quantity -= 50;
                    if (item.quantity < 0) item.quantity = 0;
                    binding.textViewQuantity.setText(String.format(Locale.US, "%dg", item.quantity));
                    listener.onSelectionChanged();
                }
            });
        }
    }
}