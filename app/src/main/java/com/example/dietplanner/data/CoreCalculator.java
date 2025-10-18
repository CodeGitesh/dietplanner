package com.example.dietplanner.data;

import java.util.ArrayList;
import java.util.List;

// Represents a single food item for the checklist
public class FoodItem {
    public String name;
    public float calories;
    public float protein;
    public boolean isSelected = false;

    public FoodItem(String name, float calories, float protein) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
    }
}

// Represents a pre-built meal combo for recommendations
class MealOption {
    public String description;
    public float totalCalories;
    public float totalProtein;

    public MealOption(String desc, float cal, float prot) {
        this.description = desc;
        this.totalCalories = cal;
        this.totalProtein = prot;
    }

    public static List<MealOption> parseMealOptionsString(String dataString) {
        List<MealOption> options = new ArrayList<>();
        if (dataString == null || dataString.isEmpty()) return options;
        String[] meals = dataString.split(";");
        for (String mealData : meals) {
            String[] parts = mealData.split("\\|");
            if (parts.length == 3) {
                try {
                    options.add(new MealOption(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2])));
                } catch (NumberFormatException e) { e.printStackTrace(); }
            }
        }
        return options;
    }
}

// The JNI bridge to your C++ code
public class CoreCalculator {
    static {
        System.loadLibrary("dietplanner-lib");
    }

    public native void loadMealsFromCSV(String filePath);
    public native String getDietaryGoals(String name, int age, float weightKg, float heightCm, String gender);
    public native String getMealCombos(int targetCalories, int targetProteinG, String dietPref);
    public native String getFilteredFoodList(String dietPref);
}