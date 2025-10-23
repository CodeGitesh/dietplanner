package com.example.dietplanner.data;

public class CoreCalculator {
    static {
        System.loadLibrary("dietplanner-lib");
    }

    public native void loadMealsFromCSV(String filePath);
    public native String getDietaryGoals(String name, int age, float weightKg, float heightCm, String gender);
    public native String getFilteredFoodList(String dietPref);
    public native String generateMealRecommendation(String mealType, float targetCalories, float targetProtein, String dietPref);
    public native String getAlternativeRecommendation();
}