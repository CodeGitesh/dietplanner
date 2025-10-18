#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <cstdio>
#include <cstring>
#include <algorithm>

// --- Data Structures ---
struct Meal {
    std::string name;
    float calories;
    float carbs_g;
    float protein_g;
    float fat_g;
};

struct UserProfile {
    std::string name;
    int age;
    float weight_kg;
    float height_cm;
    std::string gender;
};

std::vector<Meal> allMeals;
bool mealsLoaded = false;

// --- Dietary Calculation Functions ---
float calculateBMR(const UserProfile& user) {
    float bmr = (10 * user.weight_kg) + (6.25 * user.height_cm) - (5 * user.age);
    if (user.gender == "Male") {
        bmr += 5;
    } else {
        bmr -= 161;
    }
    return bmr;
}

float calculateTDEE(float bmr, float activityMultiplier = 1.375f) {
    return bmr * activityMultiplier;
}

float calculateTargetCalories(float tdee, float deficit_percent = 0.20f) {
    return tdee * (1.0f - deficit_percent);
}

// --- JNI Functions ---

extern "C" JNIEXPORT void JNICALL
Java_com_example_dietplanner_CoreCalculator_loadMealsFromCSV(
        JNIEnv* env,
        jobject /* this */,
        jstring filePath) {

    if (mealsLoaded) return;

    const char* nativeFilePath = env->GetStringUTFChars(filePath, 0);
    FILE* file = fopen(nativeFilePath, "r");
    env->ReleaseStringUTFChars(filePath, nativeFilePath);

    if (file == NULL) return;

    char line[1024];
    fgets(line, sizeof(line), file);

    allMeals.clear();

    while (fgets(line, sizeof(line), file)) {
        Meal meal;
        char* mutable_line = line;

        char* token = strtok(mutable_line, ",");
        if (token) meal.name = token;

        token = strtok(NULL, ",");
        if (token) meal.calories = atof(token);

        token = strtok(NULL, ",");
        if (token) meal.carbs_g = atof(token);

        token = strtok(NULL, ",");
        if (token) meal.protein_g = atof(token);

        token = strtok(NULL, ",");
        if (token) meal.fat_g = atof(token);

        allMeals.push_back(meal);
    }

    fclose(file);
    mealsLoaded = true;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_CoreCalculator_getDietaryGoals(
        JNIEnv* env,
        jobject /* this */,
        jstring jName,
        jint jAge,
        jfloat jWeight,
        jfloat jHeight,
        jstring jGender) {

    const char* nativeName = env->GetStringUTFChars(jName, nullptr);
    const char* nativeGender = env->GetStringUTFChars(jGender, nullptr);

    UserProfile user;
    user.name = nativeName;
    user.age = jAge;
    user.weight_kg = jWeight;
    user.height_cm = jHeight;
    user.gender = nativeGender;

    float bmr = calculateBMR(user);
    float tdee = calculateTDEE(bmr);
    float targetCalories = calculateTargetCalories(tdee);

    int protein_g = static_cast<int>(round((targetCalories * 0.30f) / 4.0f));
    int carbs_g = static_cast<int>(round((targetCalories * 0.45f) / 4.0f));
    int fat_g = static_cast<int>(round((targetCalories * 0.25f) / 9.0f));

    char buffer[512];
    snprintf(buffer, sizeof(buffer),
             "Hello %s!\n\n"
             "Your estimated BMR: %d kcal\n"
             "Your estimated TDEE: %d kcal\n"
             "Daily Calorie Target (for weight loss): %d kcal\n\n"
             "Macro Goals:\n"
             "  Protein: %dg\n"
             "  Carbs: %dg\n"
             "  Fat: %dg",
             user.name.c_str(),
             static_cast<int>(round(bmr)),
             static_cast<int>(round(tdee)),
             static_cast<int>(round(targetCalories)),
             protein_g,
             carbs_g,
             fat_g);

    env->ReleaseStringUTFChars(jName, nativeName);
    env->ReleaseStringUTFChars(jGender, nativeGender);

    return env->NewStringUTF(buffer);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_CoreCalculator_getShortlistedMeals(
        JNIEnv* env,
        jobject /* this */,
        jstring jMealType,
        jint jTargetCalories,
        jint jTargetProteinG) {

    if (!mealsLoaded || allMeals.empty()) {
        return env->NewStringUTF("");
    }

    std::vector<Meal> candidates;
    for (const auto& meal : allMeals) {
        if (meal.calories > 10 && meal.calories < (jTargetCalories * 0.9)) {
            candidates.push_back(meal);
        }
    }

    if (candidates.empty()) return env->NewStringUTF("");

    std::sort(candidates.begin(), candidates.end(), [](const Meal& a, const Meal& b) {
        if (a.calories == 0) return false;
        if (b.calories == 0) return true;
        return (a.protein_g / a.calories) > (b.protein_g / b.calories);
    });

    std::vector<std::vector<Meal>> generatedMeals;
    const int MEALS_TO_GENERATE = 5;
    const int ITEMS_PER_MEAL = 4;

    for (int i = 0; i < MEALS_TO_GENERATE && i < candidates.size(); ++i) {
        std::vector<Meal> currentMeal;
        float currentCalories = 0;
        float currentProtein = 0;

        currentMeal.push_back(candidates[i]);
        currentCalories += candidates[i].calories;
        currentProtein += candidates[i].protein_g;

        for (const auto& item : candidates) {
            if (currentMeal.size() >= ITEMS_PER_MEAL) break;
            if ((currentCalories + item.calories) < (jTargetCalories * 1.2)) {
                bool alreadyAdded = false;
                for(const auto& meal_item : currentMeal) {
                    if (meal_item.name == item.name) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    currentMeal.push_back(item);
                    currentCalories += item.calories;
                    currentProtein += item.protein_g;
                }
            }
        }
        generatedMeals.push_back(currentMeal);
    }

    std::string resultString = "";
    for (size_t i = 0; i < generatedMeals.size(); ++i) {
        float totalCal = 0;
        float totalProt = 0;
        std::string mealStr = "";
        for (size_t j = 0; j < generatedMeals[i].size(); ++j) {
            const auto& item = generatedMeals[i][j];
            mealStr += item.name;
            totalCal += item.calories;
            totalProt += item.protein_g;
            if (j < generatedMeals[i].size() - 1) {
                mealStr += ", ";
            }
        }
        char buffer[128];
        snprintf(buffer, sizeof(buffer), "|%.0f|%.0f", totalCal, totalProt);
        resultString += mealStr + buffer;

        if (i < generatedMeals.size() - 1) {
            resultString += ";";
        }
    }

    return env->NewStringUTF(resultString.c_str());
}