package com.example.dietplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.dietplanner.data.CoreCalculator;
import com.example.dietplanner.databinding.ActivityHomeBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        checkDateAndResetData(prefs);

        if (!prefs.contains("userName")) {
            startActivity(new Intent(this, UserDetailsActivity.class));
            finish();
            return;
        }

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        loadCsvData();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavView, navController);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void checkDateAndResetData(SharedPreferences prefs) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastLoginDate = prefs.getString("lastLoginDate", "");

        if (!todayDate.equals(lastLoginDate) && !lastLoginDate.isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();

            float consumed = prefs.getFloat("totalCaloriesConsumed", 0f);
            if (consumed > 0) {
                String breakfast = prefs.getString("selected_Breakfast", "None");
                String lunch = prefs.getString("selected_Lunch", "None");
                String dinner = prefs.getString("selected_Dinner", "None");
                String caloriesTarget = prefs.getString("dailyCalorieTarget", "2000");

                String historyEntry = "Total: " + String.format(Locale.US, "%.0f", consumed) + " / " + caloriesTarget + " kcal\n" +
                        "B: " + breakfast + "\n" +
                        "L: " + lunch + "\n" +
                        "D: " + dinner;

                editor.putString("history_" + lastLoginDate, historyEntry);
            }

            editor.remove("totalCaloriesConsumed");
            editor.remove("selected_Breakfast");
            editor.remove("selected_Lunch");
            editor.remove("selected_Dinner");
            editor.remove("calories_Breakfast");
            editor.remove("calories_Lunch");
            editor.remove("calories_Dinner");

            editor.putString("lastLoginDate", todayDate);
            editor.apply();
        } else if (lastLoginDate.isEmpty()) {
            // First time login
            prefs.edit().putString("lastLoginDate", todayDate).apply();
        }
    }

    private void loadCsvData() {
        File dataFile = new File(getFilesDir(), "Indian_Food_Nutrition_Processed.csv");
        if (!dataFile.exists()) {
            copyCsvFromAssets();
        }
        new CoreCalculator().loadMealsFromCSV(dataFile.getAbsolutePath());
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
}