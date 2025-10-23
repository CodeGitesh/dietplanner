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
    private List<FoodItem> food_items = new ArrayList<>();
    private CustomMealAdapter meal_adapter;
    private RecommendedMealAdapter rec_adapter;
    private RecommendedMeal current_rec;
    private String mealtype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMealSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        coreCalculator = new CoreCalculator();
        mealtype = getIntent().getStringExtra("MEAL_TYPE");
        if (mealtype == null) {
            finish();
            return;
        }

        binding.toolbar.setTitle("Build Your " + mealtype);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadmeals();
        setupSearch();
        recommendation_model();

        binding.buttonConfirmMeal.setOnClickListener(v -> confirm_meal());
    }

    private void loadmeals() {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        String dietpref = prefs.getString("userdietpref", "Vegetarian");

        String food_data = coreCalculator.get_foods(dietpref);
        String[] items = food_data.split(";");

        for (String itemData : items) {
            String[] parts = itemData.split("\\|");
            if (parts.length == 3) {
                try {
                    food_items.add(new FoodItem(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        binding.recyclerViewCustom.setLayoutManager(new LinearLayoutManager(this));
        meal_adapter = new CustomMealAdapter(food_items, this::update_total);
        binding.recyclerViewCustom.setAdapter(meal_adapter);
        update_total();
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                meal_adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                meal_adapter.filter(newText);
                return false;
            }
        });
    }

    private void update_total() {
        float totalCalories = 0;
        for (FoodItem item : food_items) {
            totalCalories += item.getTotalCalories();
        }
        binding.textViewCustomTotal.setText(String.format(Locale.US, "Total: %.0f kcal", totalCalories));
    }

    private void recommendation_model() {
        binding.recyclerViewRecommended.setLayoutManager(new LinearLayoutManager(this));
        rec_adapter = new RecommendedMealAdapter(this::on_rec_changed);
        binding.recyclerViewRecommended.setAdapter(rec_adapter);
        
        binding.buttonAcceptRecommendation.setOnClickListener(v -> accept_rec());
        binding.buttonRejectRecommendation.setOnClickListener(v -> load_new_rec());
        
        loadRecommendation();
    }

    private void loadRecommendation() {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        String dailyTarget = prefs.getString("daily_cal_target", "2000");
        float target_cal = Float.parseFloat(dailyTarget);
        
        float meal_cal = calc_cals(target_cal, mealtype);
        float meal_protein = calc_protein(meal_cal);
        
        String dietpref = prefs.getString("userdietpref", "Vegetarian");
        String recommendationData = coreCalculator.get_recommendation(
            mealtype, meal_cal, meal_protein, dietpref);
        
        current_rec = parse_rec_data(recommendationData);
        if (current_rec != null && !current_rec.items.isEmpty()) {
            rec_adapter.updateRecommendation(current_rec.items);
            binding.layoutRecommendation.setVisibility(View.VISIBLE);
        } else {
            binding.layoutRecommendation.setVisibility(View.GONE);
        }
    }

    private float calc_cals(float dailyCalories, String mealtype) {
        switch (mealtype) {
            case "Breakfast": return dailyCalories * 0.25f;
            case "Lunch": return dailyCalories * 0.40f;
            case "Dinner": return dailyCalories * 0.35f;
            default: return dailyCalories * 0.33f;
        }
    }

    private float calc_protein(float meal_cal) {
        return (meal_cal * 0.30f) / 4.0f;
    }

    private RecommendedMeal parse_rec_data(String data) {
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

    private void on_rec_changed() {
    }

    private void accept_rec() {
        if (current_rec == null) return;
        
        for (RecommendedItem item : current_rec.items) {
            for (FoodItem foodItem : food_items) {
                if (foodItem.name.equals(item.name)) {
                    foodItem.quantity = item.quantity;
                    break;
                }
            }
        }
        
        meal_adapter.notifyDataSetChanged();
        update_total();
        
        binding.layoutRecommendation.setVisibility(View.GONE);
        Toast.makeText(this, "Recommendation accepted!", Toast.LENGTH_SHORT).show();
    }

    private void load_new_rec() {
        loadRecommendation();
    }

    private void confirm_meal() {
        float new_cal = 0;
        StringBuilder description = new StringBuilder();

        for (FoodItem item : food_items) {
            if (item.quantity > 0) {
                new_cal += item.getTotalCalories();
                if (description.length() > 0) {
                    description.append(", ");
                }
                description.append(String.format(Locale.US, "%s (%dg)", item.name, item.quantity));
            }
        }

        if (new_cal > 0) {
            save_meal(description.toString(), new_cal);
        } else {
            // User confirmed an empty meal, which means they want to remove their selection
            save_meal("", 0);
        }
    }

    private void save_meal(String description, float new_cal) {
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        // **FIXED CALORIE LOGIC**
        float grand_total = prefs.getFloat("total_cal", 0f);
        float old_cal = prefs.getFloat("calories_" + mealtype, 0f);
        float new_total = (grand_total - old_cal) + new_cal;

        // Save all the new values
        editor.putFloat("total_cal", new_total);
        editor.putString("selected_" + mealtype, description);
        editor.putFloat("calories_" + mealtype, new_cal);

        editor.apply();

        Toast.makeText(this, "Meal plan updated!", Toast.LENGTH_SHORT).show();
        finish();
    }
}