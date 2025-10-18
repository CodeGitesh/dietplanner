package com.example.dietplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment; // <-- Important new import
import androidx.navigation.ui.NavigationUI;
import com.example.dietplanner.databinding.ActivityHomeBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(UserDetailsActivity.PREFS_NAME, 0);
        if (!prefs.contains("userName")) {
            startActivity(new Intent(this, UserDetailsActivity.class));
            finish();
            return;
        }

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadCsvData();

        // **THIS IS THE SAFER WAY TO GET THE NAV CONTROLLER**
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavView, navController);
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