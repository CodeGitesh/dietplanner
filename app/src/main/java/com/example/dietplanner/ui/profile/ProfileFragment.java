package com.example.dietplanner.ui.profile;

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
import com.example.dietplanner.UserDetailsActivity;
import com.example.dietplanner.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        prefs = requireActivity().getSharedPreferences(UserDetailsActivity.PREFS_NAME, Context.MODE_PRIVATE);

        displayProfileDetails();

        binding.buttonEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
            intent.putExtra("EDIT_MODE", true); // Tell UserDetails to pre-fill data
            startActivity(intent);
        });

        return binding.getRoot();
    }

    private void displayProfileDetails() {
        String name = prefs.getString("userName", "N/A");
        int age = prefs.getInt("userAge", 0);
        float weight = prefs.getFloat("userWeight", 0f);
        float height = prefs.getFloat("userHeight", 0f);
        String gender = prefs.getString("userGender", "N/A");
        String diet = prefs.getString("userDietPref", "N/A");

        String profileText = "Name: " + name + "\n" +
                "Age: " + age + " years\n" +
                "Weight: " + weight + " kg\n" +
                "Height: " + height + " cm\n" +
                "Gender: " + gender + "\n" +
                "Diet: " + diet;

        binding.textViewProfileDetails.setText(profileText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}