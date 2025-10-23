package com.example.dietplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dietplanner.databinding.ActivityUserDetailsBinding;
import java.util.Map;

public class UserDetailsActivity extends AppCompatActivity {

    private ActivityUserDetailsBinding binding;
    public static final String PREFS_NAME = "DietPlannerPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().getBooleanExtra("EDIT_MODE", false)) {
            loadUserDetailsForEditing();
        }

        binding.buttonSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveUserDetails();
                Intent intent = new Intent(UserDetailsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadUserDetailsForEditing() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        binding.editTextName.setText(prefs.getString("userName", ""));
        binding.editTextAge.setText(String.valueOf(prefs.getInt("userAge", 0)));
        binding.editTextWeight.setText(String.valueOf(prefs.getFloat("userWeight", 0f)));
        binding.editTextHeight.setText(String.valueOf(prefs.getFloat("userHeight", 0f)));

        String gender = prefs.getString("userGender", "Male");
        if (gender.equals("Female")) {
            binding.radioGroupGender.check(R.id.radioButton_female);
        } else {
            binding.radioGroupGender.check(R.id.radioButton_male);
        }

        String diet = prefs.getString("userDietPref", "Vegetarian");
        if (diet.equals("Non-Vegetarian")) {
            binding.radioGroupDiet.check(R.id.radioButton_nonveg);
        } else {
            binding.radioGroupDiet.check(R.id.radioButton_veg);
        }
    }

    private boolean validateInput() {
        if (binding.editTextName.getText().toString().trim().isEmpty() ||
                binding.editTextAge.getText().toString().trim().isEmpty() ||
                binding.editTextWeight.getText().toString().trim().isEmpty() ||
                binding.editTextHeight.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveUserDetails() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("userName", binding.editTextName.getText().toString());
        editor.putInt("userAge", Integer.parseInt(binding.editTextAge.getText().toString()));
        editor.putFloat("userWeight", Float.parseFloat(binding.editTextWeight.getText().toString()));
        editor.putFloat("userHeight", Float.parseFloat(binding.editTextHeight.getText().toString()));

        int selectedGenderId = binding.radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        editor.putString("userGender", selectedGenderButton.getText().toString());

        int selectedDietId = binding.radioGroupDiet.getCheckedRadioButtonId();
        RadioButton selectedDietButton = findViewById(selectedDietId);
        editor.putString("userDietPref", selectedDietButton.getText().toString());

        // Clear all old meal and history data as the user's profile has changed
        editor.remove("totalCaloriesConsumed");
        editor.remove("selected_Breakfast");
        editor.remove("selected_Lunch");
        editor.remove("selected_Dinner");
        editor.remove("calories_Breakfast");
        editor.remove("calories_Lunch");
        editor.remove("calories_Dinner");

        Map<String, ?> allEntries = settings.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("history_")) {
                editor.remove(entry.getKey());
            }
        }

        editor.apply();
    }
}