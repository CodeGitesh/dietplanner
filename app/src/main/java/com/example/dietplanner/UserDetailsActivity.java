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
            load_user_edit();
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

    private void load_user_edit() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        binding.editTextName.setText(prefs.getString("username", ""));
        binding.editTextAge.setText(String.valueOf(prefs.getInt("age", 0)));
        binding.editTextWeight.setText(String.valueOf(prefs.getFloat("weight", 0f)));
        binding.editTextHeight.setText(String.valueOf(prefs.getFloat("height", 0f)));

        String gender = prefs.getString("gender", "Male");
        if (gender.equals("Female")) {
            binding.radioGroupGender.check(R.id.radioButton_female);
        } else {
            binding.radioGroupGender.check(R.id.radioButton_male);
        }

        String diet = prefs.getString("userdietpref", "Vegetarian");
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

        editor.putString("username", binding.editTextName.getText().toString());
        editor.putInt("age", Integer.parseInt(binding.editTextAge.getText().toString()));
        editor.putFloat("weight", Float.parseFloat(binding.editTextWeight.getText().toString()));
        editor.putFloat("height", Float.parseFloat(binding.editTextHeight.getText().toString()));

        int selected_gender_id = binding.radioGroupGender.getCheckedRadioButtonId();
        RadioButton selected_gender_btn = findViewById(selected_gender_id);
        editor.putString("gender", selected_gender_btn.getText().toString());

        int selected_diet_id = binding.radioGroupDiet.getCheckedRadioButtonId();
        RadioButton selected_diet_btn = findViewById(selected_diet_id);
        editor.putString("userdietpref", selected_diet_btn.getText().toString());

        // Clear all old meal and history data as the user's profile has changed
        editor.remove("total_cal");
        editor.remove("select_bf");
        editor.remove("select_ln");
        editor.remove("select_dinner");
        editor.remove("bf_cal");
        editor.remove("ln_cal");
        editor.remove("dn_cal");

        Map<String, ?> allEntries = settings.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("history_")) {
                editor.remove(entry.getKey());
            }
        }

        editor.apply();
    }
}