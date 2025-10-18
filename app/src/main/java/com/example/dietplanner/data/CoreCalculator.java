package com.example.dietplanner.data;

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