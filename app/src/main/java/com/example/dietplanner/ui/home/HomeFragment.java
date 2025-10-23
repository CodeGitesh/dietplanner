package com.example.dietplanner.ui.home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.dietplanner.MealSelectionActivity;
import com.example.dietplanner.UserDetailsActivity;
import com.example.dietplanner.data.CoreCalculator;
import com.example.dietplanner.databinding.FragmentHomeBinding;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SharedPreferences prefs;
    private CoreCalculator coreCalculator;
    private String goalsString = "";
    private int dailyCalorieTarget = 2000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = requireActivity().getSharedPreferences(UserDetailsActivity.PREFS_NAME, Context.MODE_PRIVATE);
        coreCalculator = new CoreCalculator();

        load_user_data();
        setup_buttons();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        load_user_data();
        updateConsumedCalories();
        update_meals();
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

        try {
            String[] lines = goalsString.split("\n");
            for (String line : lines) {
                if (line.contains("Daily Calorie Target")) {
                    String calPart = line.split(":")[1].trim().split(" ")[0];
                    dailyCalorieTarget = Integer.parseInt(calPart);
                    prefs.edit().putString("daily_cal_target", String.valueOf(dailyCalorieTarget)).apply();
                    break;
                }
            }
        } catch (Exception e) {
            dailyCalorieTarget = 2000;
        }
    }

    private void updateConsumedCalories() {
        float consumed = prefs.getFloat("total_cal", 0f);
        binding.textViewCaloriesConsumed.setText(String.format(Locale.US, "%.0f", consumed));

        int progress = (int) ((consumed / dailyCalorieTarget) * 100);
        binding.progressBar.setMax(100 * 100);
        ObjectAnimator animation = ObjectAnimator.ofInt(binding.progressBar, "progress", binding.progressBar.getProgress(), progress * 100);
        animation.setDuration(1500);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void update_meals() {
        String breakfast = prefs.getString("select_bf", "");
        if (!breakfast.isEmpty()) {
            binding.textViewBreakfastSelection.setText(format_meal_text(breakfast));
            binding.textViewBreakfastSelection.setVisibility(View.VISIBLE);
        } else {
            binding.textViewBreakfastSelection.setVisibility(View.GONE);
        }

        String lunch = prefs.getString("select_ln", "");
        if (!lunch.isEmpty()) {
            binding.textViewLunchSelection.setText(format_meal_text(lunch));
            binding.textViewLunchSelection.setVisibility(View.VISIBLE);
        } else {
            binding.textViewLunchSelection.setVisibility(View.GONE);
        }

        String dinner = prefs.getString("select_dinner", "");
        if (!dinner.isEmpty()) {
            binding.textViewDinnerSelection.setText(format_meal_text(dinner));
            binding.textViewDinnerSelection.setVisibility(View.VISIBLE);
        } else {
            binding.textViewDinnerSelection.setVisibility(View.GONE);
        }
    }

    private String format_meal_text(String mealData) {
        return "• " + mealData.replace(", ", "\n• ");
    }

    private void setup_buttons() {
        binding.buttonFindBreakfast.setOnClickListener(v -> meal_selection("Breakfast"));
        binding.buttonFindLunch.setOnClickListener(v -> meal_selection("Lunch"));
        binding.buttonFindDinner.setOnClickListener(v -> meal_selection("Dinner"));
    }

    private void meal_selection(String mealtype) {
        Intent intent = new Intent(getActivity(), MealSelectionActivity.class);
        intent.putExtra("MEAL_TYPE", mealtype);
        intent.putExtra("GOALS_STRING", this.goalsString);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}