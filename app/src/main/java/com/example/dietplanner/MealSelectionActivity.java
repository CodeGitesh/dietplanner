package com.example.dietplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.dietplanner.data.CoreCalculator;
import com.example.dietplanner.data.FoodItem;
import com.example.dietplanner.data.RecommendedItem;
import com.example.dietplanner.data.RecommendedMeal;
import com.example.dietplanner.databinding.ActivityMealSelectionBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealSelectionActivity extends AppCompatActivity {

    private ActivityMealSelectionBinding binding;
    private CoreCalculator coreCalculator;
    private List<FoodItem> allFoodItems = new ArrayList<>();
    private CustomMealAdapter customMealAdapter;
    private RecommendedMealAdapter recommendedAdapter;
    private RecommendedMeal currentRecommendation;
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
        setupRecommendationSystem();

        binding.buttonConfirmMeal.setOnClickListener(v -> confirmCustomMeal());
    }

    private void loadAndDisplayMeals() {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        String dietPref = prefs.getString("userDietPref", "Vegetarian");

        // Debug: Check if CSV was loaded
        String debugInfo = coreCalculator.debugGetFoodCount();
        android.util.Log.d("MealSelection", "Debug info: " + debugInfo);

        String foodListDataString = coreCalculator.getFilteredFoodList(dietPref);
        android.util.Log.d("MealSelection", "Food list data length: " + foodListDataString.length());
        
        String[] items = foodListDataString.split(";");
        android.util.Log.d("MealSelection", "Number of food items: " + items.length);

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

    private void setupRecommendationSystem() {
        binding.recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(this));
        recommendedAdapter = new RecommendedMealAdapter(this::onRecommendationItemChanged);
        binding.recyclerViewRecommended.setAdapter(recommendedAdapter);
        
        binding.buttonAcceptRecommendation.setOnClickListener(v -> acceptRecommendation());
        binding.buttonRejectRecommendation.setOnClickListener(v -> loadNewRecommendation());
        
        loadRecommendation();
    }

    private void loadRecommendation() {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        String dailyTarget = prefs.getString("dailyCalorieTarget", "2000");
        float targetCalories = Float.parseFloat(dailyTarget);
        
        float mealCalories = calculateMealCalories(targetCalories, mealType);
        float mealProtein = calculateMealProtein(mealCalories);
        
        String dietPref = prefs.getString("userDietPref", "Vegetarian");
        
        android.util.Log.d("MealSelection", "Generating recommendation for " + mealType + 
                          " with " + mealCalories + " calories, " + mealProtein + " protein");
        
        String recommendationData = coreCalculator.generateMealRecommendation(
            mealType, mealCalories, mealProtein, dietPref);
        
        android.util.Log.d("MealSelection", "Recommendation data: " + recommendationData);
        
        currentRecommendation = parseRecommendationData(recommendationData);
        if (currentRecommendation != null && !currentRecommendation.items.isEmpty()) {
            android.util.Log.d("MealSelection", "Recommendation parsed successfully with " + 
                              currentRecommendation.items.size() + " items");
            recommendedAdapter.updateRecommendation(currentRecommendation.items);
            binding.layoutRecommendation.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.d("MealSelection", "No recommendation generated");
            binding.layoutRecommendation.setVisibility(View.GONE);
        }
    }

    private float calculateMealCalories(float dailyCalories, String mealType) {
        switch (mealType) {
            case "Breakfast": return dailyCalories * 0.25f;
            case "Lunch": return dailyCalories * 0.40f;
            case "Dinner": return dailyCalories * 0.35f;
            default: return dailyCalories * 0.33f;
        }
    }

    private float calculateMealProtein(float mealCalories) {
        return (mealCalories * 0.30f) / 4.0f;
    }

    private RecommendedMeal parseRecommendationData(String data) {
        if (data == null || data.isEmpty()) return null;
        
        RecommendedMeal meal = new RecommendedMeal();
        String[] items = data.split(";");
        
        for (String itemData : items) {
            String[] parts = itemData.split("\\|");
            if (parts.length == 4) {
                try {
                    String name = parts[0];
                    float calories = Float.parseFloat(parts[1]);
                    float protein = Float.parseFloat(parts[2]);
                    int quantity = Integer.parseInt(parts[3]);
                    
                    meal.addItem(new RecommendedItem(name, calories, protein, quantity));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return meal;
    }

    private void onRecommendationItemChanged() {
    }

    private void acceptRecommendation() {
        if (currentRecommendation == null) return;
        
        for (RecommendedItem item : currentRecommendation.items) {
            for (FoodItem foodItem : allFoodItems) {
                if (foodItem.name.equals(item.name)) {
                    foodItem.quantity = item.quantity;
                    break;
                }
            }
        }
        
        customMealAdapter.notifyDataSetChanged();
        updateCustomMealTotal();
        
        binding.layoutRecommendation.setVisibility(View.GONE);
        Toast.makeText(this, "Recommendation accepted!", Toast.LENGTH_SHORT).show();
    }

    private void loadNewRecommendation() {
        loadRecommendation();
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