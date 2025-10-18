package com.example.dietplanner.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.dietplanner.CoreCalculator;
import com.example.dietplanner.MealSelectionActivity;
import com.example.dietplanner.UserDetailsActivity;
import com.example.dietplanner.databinding.FragmentHomeBinding;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences prefs;
    private CoreCalculator coreCalculator;
    private String goalsString = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = requireActivity().getSharedPreferences(UserDetailsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        coreCalculator = new CoreCalculator();

        loadUserDataAndDisplayGoals();
        setupButtonClickListeners();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // This runs every time you return to the home screen
        updateConsumedCalories();
        updateSelectedMeals();
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

    private void updateConsumedCalories() {
        float consumed = prefs.getFloat("caloriesConsumed", 0f);
        binding.textViewCaloriesConsumed.setText(String.format(Locale.US, "%.0f kcal", consumed));
    }

    private void updateSelectedMeals() {
        String breakfast = prefs.getString("selected_Breakfast", "");
        if (!breakfast.isEmpty()) {
            binding.textViewBreakfastSelection.setText(breakfast.replace(", ", "\n• "));
            binding.textViewBreakfastSelection.setVisibility(View.VISIBLE);
        } else {
            binding.textViewBreakfastSelection.setVisibility(View.GONE);
        }

        String lunch = prefs.getString("selected_Lunch", "");
        if (!lunch.isEmpty()) {
            binding.textViewLunchSelection.setText(lunch.replace(", ", "\n• "));
            binding.textViewLunchSelection.setVisibility(View.VISIBLE);
        } else {
            binding.textViewLunchSelection.setVisibility(View.GONE);
        }

        String dinner = prefs.getString("selected_Dinner", "");
        if (!dinner.isEmpty()) {
            binding.textViewDinnerSelection.setText(dinner.replace(", ", "\n• "));
            binding.textViewDinnerSelection.setVisibility(View.VISIBLE);
        } else {
            binding.textViewDinnerSelection.setVisibility(View.GONE);
        }
    }

    private void setupButtonClickListeners() {
        binding.buttonFindBreakfast.setOnClickListener(v -> openMealSelection("Breakfast"));
        binding.buttonFindLunch.setOnClickListener(v -> openMealSelection("Lunch"));
        binding.buttonFindDinner.setOnClickListener(v -> openMealSelection("Dinner"));
    }

    private void openMealSelection(String mealType) {
        Intent intent = new Intent(getActivity(), MealSelectionActivity.class);
        intent.putExtra("MEAL_TYPE", mealType);
        intent.putExtra("GOALS_STRING", this.goalsString);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}