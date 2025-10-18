package com.example.dietplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dietplanner.databinding.ActivityMealSelectionBinding;
import java.util.List;

public class MealSelectionActivity extends AppCompatActivity {

    private ActivityMealSelectionBinding binding;
    private CoreCalculator coreCalculator;
    private MealAdapter mealAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMealSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        coreCalculator = new CoreCalculator();
        String mealType = getIntent().getStringExtra("MEAL_TYPE");
        if (mealType == null) {
            finish();
            return;
        }

        binding.toolbar.setTitle("Select Your " + mealType);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadAndDisplayMeals(mealType);
    }

    private void loadAndDisplayMeals(String mealType) {
        String goals = getIntent().getStringExtra("GOALS_STRING");
        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        String dietPref = prefs.getString("userDietPref", "Vegetarian");

        int totalTargetCalories = 2000;
        int totalTargetProtein = 100;

        if (goals != null) {
            String[] lines = goals.split("\n");
            try {
                for (String line : lines) {
                    if (line.contains("Daily Calorie Target")) {
                        String calPart = line.split(":")[1].trim().split(" ")[0];
                        totalTargetCalories = Integer.parseInt(calPart);
                    }
                    if (line.contains("Protein:")) {
                        String protPart = line.split(":")[1].trim().replace("g", "");
                        totalTargetProtein = Integer.parseInt(protPart);
                    }
                }
            } catch (Exception e) { /* ignore */ }
        }

        int mealTargetCalories;
        int mealTargetProtein;

        switch (mealType) {
            case "Breakfast":
                mealTargetCalories = (int) (totalTargetCalories * 0.40);
                mealTargetProtein = (int) (totalTargetProtein * 0.40);
                break;
            case "Lunch":
                mealTargetCalories = (int) (totalTargetCalories * 0.35);
                mealTargetProtein = (int) (totalTargetProtein * 0.35);
                break;
            default:
                mealTargetCalories = (int) (totalTargetCalories * 0.25);
                mealTargetProtein = (int) (totalTargetProtein * 0.25);
                break;
        }

        String mealsDataString = coreCalculator.getShortlistedMeals(mealType, mealTargetCalories, mealTargetProtein, dietPref);
        List<MealOption> mealOptions = MealOption.parseMealOptionsString(mealsDataString);

        if (mealOptions.isEmpty()) {
            Toast.makeText(this, "Could not generate meals for your goals.", Toast.LENGTH_LONG).show();
            return;
        }

        mealAdapter = new MealAdapter(this, mealOptions, mealType);
        binding.recyclerViewMeals.setAdapter(mealAdapter);
    }
}