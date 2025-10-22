package com.example.dietplanner.data;

// Represents a single food item for the checklist
public class FoodItem {
    public String name;
    public float caloriesPer100g;
    public float proteinPer100g;
    public int quantity; // in grams

    public FoodItem(String name, float calories, float protein) {
        this.name = name;
        this.caloriesPer100g = calories;
        this.proteinPer100g = protein;
        this.quantity = 0; // Default to 0, meaning not selected
    }

    // Calculates total calories for the selected quantity
    public float getTotalCalories() {
        return (caloriesPer100g / 100.0f) * quantity;
    }
}