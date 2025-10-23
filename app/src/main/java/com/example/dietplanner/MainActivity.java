package com.example.dietplanner;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.dietplanner.data.CoreCalculator;
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

        if (!prefs.contains("username")) {
            startActivity(new Intent(this, UserDetailsActivity.class));
            finish();
            return;
        }

        loadCsvData();
        load_user_data();
        setup_buttons();
    }

    private void loadCsvData() {
        File dataFile = new File(getFilesDir(), "Indian_Food_Nutrition_Processed.csv");
        if (!dataFile.exists()) {
            copyCsvFromAssets();
        }
        coreCalculator.load_csv(dataFile.getAbsolutePath());
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

    private void load_user_data() {
        String name = prefs.getString("username", "User");
        int age = prefs.getInt("age", 0);
        float weight = prefs.getFloat("weight", 0f);
        float height = prefs.getFloat("height", 0f);
        String gender = prefs.getString("gender", "Male");

        binding.textViewGreeting.setText("Hello, " + name + "!");

        this.goalsString = coreCalculator.get_goals(name, age, weight, height, gender);
        binding.textViewGoals.setText(this.goalsString);
    }

    private void setup_buttons() {
        binding.buttonFindBreakfast.setOnClickListener(v -> meal_selection("Breakfast"));
        binding.buttonFindLunch.setOnClickListener(v -> meal_selection("Lunch"));
        binding.buttonFindDinner.setOnClickListener(v -> meal_selection("Dinner"));
    }

    private void meal_selection(String mealtype) {
        Intent intent = new Intent(this, MealSelectionActivity.class);
        intent.putExtra("MEAL_TYPE", mealtype);
        intent.putExtra("GOALS_STRING", this.goalsString);
        startActivity(intent);
    }
}