package com.example.dietplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dietplanner.data.MealOption;
import com.example.dietplanner.databinding.ListItemMealBinding;
import java.util.List;
import java.util.Locale;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private final List<MealOption> mealOptions;
    private final SharedPreferences prefs;
    private final String mealType;

    public MealAdapter(Context context, List<MealOption> mealOptions, String mealType) {
        this.mealOptions = mealOptions;
        this.prefs = context.getSharedPreferences(UserDetailsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        this.mealType = mealType;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemMealBinding binding = ListItemMealBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MealViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealOption meal = mealOptions.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return mealOptions.size();
    }

    class MealViewHolder extends RecyclerView.ViewHolder {
        private final ListItemMealBinding binding;

        public MealViewHolder(ListItemMealBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MealOption meal) {
            String formattedDescription = "• " + meal.description.replace(", ", "\n• ");
            binding.textViewMealName.setText(formattedDescription);
            binding.textViewMealCalories.setText(String.format(Locale.US, "%.0f kcal", meal.totalCalories));
            binding.textViewMealProtein.setText(String.format(Locale.US, "%.0f g Protein", meal.totalProtein));

            binding.checkboxAteMeal.setOnCheckedChangeListener(null);
            binding.checkboxAteMeal.setChecked(false);

            binding.checkboxAteMeal.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    float currentCalories = prefs.getFloat("caloriesConsumed", 0f);
                    float newTotal = currentCalories + meal.totalCalories;

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putFloat("caloriesConsumed", newTotal);
                    editor.putString("selected_" + mealType, meal.description);
                    editor.apply();

                    Toast.makeText(itemView.getContext(), "Meal added to your daily plan!", Toast.LENGTH_SHORT).show();

                    // You might want to navigate back or disable other checkboxes here
                }
            });
        }
    }
}