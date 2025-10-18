package com.example.dietplanner.data;

import java.util.ArrayList;
import java.util.List;

// Represents a pre-built meal combo for recommendations
public class MealOption {
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