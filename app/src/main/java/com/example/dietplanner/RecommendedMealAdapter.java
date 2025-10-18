package com.example.dietplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dietplanner.data.MealOption;
import com.example.dietplanner.databinding.ListItemRecommendedMealBinding;
import java.util.List;
import java.util.Locale;

public class RecommendedMealAdapter extends RecyclerView.Adapter<RecommendedMealAdapter.ViewHolder> {

    private final List<MealOption> mealOptions;
    private final OnMealSelectedListener listener;

    public interface OnMealSelectedListener {
        void onMealSelected(MealOption meal);
    }

    public RecommendedMealAdapter(List<MealOption> mealOptions, OnMealSelectedListener listener) {
        this.mealOptions = mealOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemRecommendedMealBinding binding = ListItemRecommendedMealBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealOption meal = mealOptions.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return mealOptions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemRecommendedMealBinding binding;

        public ViewHolder(ListItemRecommendedMealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MealOption meal) {
            binding.textViewMealName.setText("• " + meal.description.replace(", ", "\n• "));
            binding.textViewMealCalories.setText(String.format(Locale.US, "%.0f kcal", meal.totalCalories));
            binding.buttonSelectMeal.setOnClickListener(v -> listener.onMealSelected(meal));
        }
    }
}