package com.example.dietplanner;

import java.util.ArrayList;
import java.util.List;

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
                    String desc = parts[0];
                    float cal = Float.parseFloat(parts[1]);
                    float prot = Float.parseFloat(parts[2]);
                    options.add(new MealOption(desc, cal, prot));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return options;
    }
}


public class CoreCalculator {
    static {
        System.loadLibrary("dietplanner-lib");
    }

    public native void loadMealsFromCSV(String filePath);
    public native String getDietaryGoals(String name, int age, float weightKg, float heightCm, String gender);

    public native String getShortlistedMeals(String mealType, int targetCalories, int targetProteinG, String dietPref);
}