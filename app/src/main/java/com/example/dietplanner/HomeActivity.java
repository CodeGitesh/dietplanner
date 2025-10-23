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
        check_date(prefs);

        if (!prefs.contains("username")) {
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

    private void check_date(SharedPreferences prefs) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String last_login = prefs.getString("lastLoginDate", "");

        if (!today.equals(last_login) && !last_login.isEmpty()) {
            SharedPreferences.Editor editor = prefs.edit();

            float consumed = prefs.getFloat("total_cal", 0f);
            if (consumed > 0) {
                String breakfast = prefs.getString("select_bf", "None");
                String lunch = prefs.getString("select_ln", "None");
                String dinner = prefs.getString("select_dinner", "None");
                String cal_target = prefs.getString("daily_cal_target", "2000");

                String history = "Total: " + String.format(Locale.US, "%.0f", consumed) + " / " + cal_target + " kcal\n" +
                        "B: " + breakfast + "\n" +
                        "L: " + lunch + "\n" +
                        "D: " + dinner;

                editor.putString("history_" + last_login, history);
            }

            editor.remove("total_cal");
            editor.remove("select_bf");
            editor.remove("select_ln");
            editor.remove("select_dinner");
            editor.remove("bf_cal");
            editor.remove("ln_cal");
            editor.remove("dn_cal");

            editor.putString("lastLoginDate", today);
            editor.apply();
        } else if (last_login.isEmpty()) {
            // First time login
            prefs.edit().putString("lastLoginDate", today).apply();
        }
    }

    private void loadCsvData() {
        File dataFile = new File(getFilesDir(), "Indian_Food_Nutrition_Processed.csv");
        if (!dataFile.exists()) {
            copy_csv();
        }
        new CoreCalculator().load_csv(dataFile.getAbsolutePath());
    }

    private void copy_csv() {
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