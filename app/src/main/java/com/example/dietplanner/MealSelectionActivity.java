package com.example.dietplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.dietplanner.data.CoreCalculator;
import com.example.dietplanner.data.FoodItem;
import com.example.dietplanner.databinding.ActivityMealSelectionBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealSelectionActivity extends AppCompatActivity {

    private ActivityMealSelectionBinding binding;
    private CoreCalculator coreCalculator;
    private List<FoodItem> allFoodItems = new ArrayList<>();
    private CustomMealAdapter customMealAdapter;
    private String mealType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMealSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        coreCalculator = new CoreCalculator();
        mealType = getIntent().getStringExtra("MEAL_TYPE");
        if (mealType == null) {
            finish();
            return;
        }

        binding.toolbar.setTitle("Build Your " + mealType);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadAndDisplayMeals();
        setupSearch();

        binding.buttonConfirmMeal.setOnClickListener(v -> confirmCustomMeal());
    }

    private void loadAndDisplayMeals() {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        String dietPref = prefs.getString("userDietPref", "Vegetarian");

        String foodListDataString = coreCalculator.getFilteredFoodList(dietPref);
        String[] items = foodListDataString.split(";");

        for (String itemData : items) {
            String[] parts = itemData.split("\\|");
            if (parts.length == 3) {
                try {
                    allFoodItems.add(new FoodItem(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        binding.recyclerViewCustom.setLayoutManager(new LinearLayoutManager(this));
        customMealAdapter = new CustomMealAdapter(allFoodItems, this::updateCustomMealTotal);
        binding.recyclerViewCustom.setAdapter(customMealAdapter);
        updateCustomMealTotal();
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                customMealAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                customMealAdapter.filter(newText);
                return false;
            }
        });
    }

    private void updateCustomMealTotal() {
        float totalCalories = 0;
        for (FoodItem item : allFoodItems) {
            totalCalories += item.getTotalCalories();
        }
        binding.textViewCustomTotal.setText(String.format(Locale.US, "Total: %.0f kcal", totalCalories));
    }

    private void confirmCustomMeal() {
        float newMealCalories = 0;
        StringBuilder description = new StringBuilder();

        for (FoodItem item : allFoodItems) {
            if (item.quantity > 0) {
                newMealCalories += item.getTotalCalories();
                if (description.length() > 0) {
                    description.append(", ");
                }
                description.append(String.format(Locale.US, "%s (%dg)", item.name, item.quantity));
            }
        }

        if (newMealCalories > 0) {
            saveMeal(description.toString(), newMealCalories);
        } else {
            // User confirmed an empty meal, which means they want to remove their selection
            saveMeal("", 0);
        }
    }

    private void saveMeal(String description, float newMealCalories) {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        // **FIXED CALORIE LOGIC**
        float currentGrandTotal = prefs.getFloat("totalCaloriesConsumed", 0f);
        float oldMealCalories = prefs.getFloat("calories_" + mealType, 0f);
        float newGrandTotal = (currentGrandTotal - oldMealCalories) + newMealCalories;

        // Save all the new values
        editor.putFloat("totalCaloriesConsumed", newGrandTotal);
        editor.putString("selected_" + mealType, description);
        editor.putFloat("calories_" + mealType, newMealCalories);

        editor.apply();

        Toast.makeText(this, "Meal plan updated!", Toast.LENGTH_SHORT).show();
        finish();
    }
}