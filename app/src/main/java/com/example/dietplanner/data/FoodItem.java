package com.example.dietplanner.data;

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