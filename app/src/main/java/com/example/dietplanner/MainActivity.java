package com.example.dietplanner;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.dietplanner.databinding.ActivityMainBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences prefs;
    private CoreCalculator coreCalculator;
    private String goalsString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        coreCalculator = new CoreCalculator();

        if (!prefs.contains("userName")) {
            startActivity(new Intent(this, UserDetailsActivity.class));
            finish();
            return;
        }

        loadCsvData();
        loadUserDataAndDisplayGoals();
        setupButtonClickListeners();
    }

    private void loadCsvData() {
        File dataFile = new File(getFilesDir(), "Indian_Food_Nutrition_Processed.csv");
        if (!dataFile.exists()) {
            copyCsvFromAssets();
        }
        coreCalculator.loadMealsFromCSV(dataFile.getAbsolutePath());
    }

    private void copyCsvFromAssets() {
        try (InputStream in = getAssets().open("Indian_Food_Nutrition_Processed.csv");
             OutputStream out = new FileOutputStream(new File(getFilesDir(), "Indian_Food_Nutrition_Processed.csv"))) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUserDataAndDisplayGoals() {
        String name = prefs.getString("userName", "User");
        int age = prefs.getInt("userAge", 0);
        float weight = prefs.getFloat("userWeight", 0f);
        float height = prefs.getFloat("userHeight", 0f);
        String gender = prefs.getString("userGender", "Male");

        binding.textViewGreeting.setText("Hello, " + name + "!");

        this.goalsString = coreCalculator.getDietaryGoals(name, age, weight, height, gender);
        binding.textViewGoals.setText(this.goalsString);
    }

    private void setupButtonClickListeners() {
        binding.buttonFindBreakfast.setOnClickListener(v -> openMealSelection("Breakfast"));
        binding.buttonFindLunch.setOnClickListener(v -> openMealSelection("Lunch"));
        binding.buttonFindDinner.setOnClickListener(v -> openMealSelection("Dinner"));
    }

    private void openMealSelection(String mealType) {
        Intent intent = new Intent(this, MealSelectionActivity.class);
        intent.putExtra("MEAL_TYPE", mealType);
        intent.putExtra("GOALS_STRING", this.goalsString);
        startActivity(intent);
    }
}