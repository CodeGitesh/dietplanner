#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

typedef struct {
    char* name;
    char* category;
    float calories_per_100g;
    float protein_per_100g;
} FoodItem;

typedef struct {
    const char* name;
    int age;
    float weight_kg;
    float height_cm;
    const char* gender;
} UserProfile;

// Globals for storing food data
FoodItem* allFoods = NULL;
int foodCount = 0;
int foodsLoaded = 0;

// Function to free all dynamically allocated memory
void cleanupFoodData() {
    if (allFoods != NULL) {
        for (int i = 0; i < foodCount; i++) {
            free(allFoods[i].name);
            free(allFoods[i].category);
        }
        free(allFoods);
        allFoods = NULL;
        foodCount = 0;
    }
    foodsLoaded = 0;
}

// --- JNI Functions in Pure C ---

JNIEXPORT void JNICALL
Java_com_example_dietplanner_data_CoreCalculator_loadMealsFromCSV(
        JNIEnv* env, jobject thiz, jstring filePath) {

    // Clean up old data before loading new
    if (foodsLoaded) {
        cleanupFoodData();
    }

    const char* nativeFilePath = (*env)->GetStringUTFChars(env, filePath, 0);
    FILE* file = fopen(nativeFilePath, "r");
    (*env)->ReleaseStringUTFChars(env, filePath, nativeFilePath);

    if (file == NULL) return;

    char line[1024];
    fgets(line, sizeof(line), file); // Skip header line

    // Read file line by line and dynamically resize the array
    while (fgets(line, sizeof(line), file)) {
        allFoods = realloc(allFoods, (foodCount + 1) * sizeof(FoodItem));
        FoodItem* food = &allFoods[foodCount];

        // Using strdup to allocate memory for strings
        char* name = strtok(line, ",");
        char* category = strtok(NULL, ",");
        char* calories = strtok(NULL, ",");
        strtok(NULL, ","); // Skip Carbohydrates
        char* protein = strtok(NULL, ",");

        // Clean newline characters from the last token
        if (protein) protein[strcspn(protein, "\r\n")] = 0;

        food->name = (name) ? strdup(name) : strdup("");
        food->category = (category) ? strdup(category) : strdup("Veg");
        food->calories_per_100g = (calories) ? atof(calories) : 0;
        food->protein_per_100g = (protein) ? atof(protein) : 0;

        foodCount++;
    }
    fclose(file);
    foodsLoaded = 1;
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_getDietaryGoals(
        JNIEnv* env, jobject thiz, jstring jName, jint jAge, jfloat jWeight, jfloat jHeight, jstring jGender) {

    const char* nativeName = (*env)->GetStringUTFChars(env, jName, NULL);
    const char* nativeGender = (*env)->GetStringUTFChars(env, jGender, NULL);

    float bmr = (10 * jWeight) + (6.25 * jHeight) - (5 * jAge);
    if (strcmp(nativeGender, "Male") == 0) { bmr += 5; } else { bmr -= 161; }

    float tdee = bmr * 1.375f;
    float targetCalories = tdee * 0.80f; // 20% deficit
    int protein_g = (int)round((targetCalories * 0.30f) / 4.0f);

    char buffer[512];
    snprintf(buffer, sizeof(buffer),
             "Daily Calorie Target: %d kcal\n"
             "Daily Protein Target: %dg",
             (int)round(targetCalories), protein_g);

    (*env)->ReleaseStringUTFChars(env, jName, nativeName);
    (*env)->ReleaseStringUTFChars(env, jGender, nativeGender);
    return (*env)->NewStringUTF(env, buffer);
}


JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_getFilteredFoodList(
        JNIEnv *env, jobject thiz, jstring jDietPref) {

    if (!foodsLoaded) return (*env)->NewStringUTF(env, "");

    const char* nativeDietPref = (*env)->GetStringUTFChars(env, jDietPref, NULL);
    int isVeg = (strcmp(nativeDietPref, "Vegetarian") == 0);
    (*env)->ReleaseStringUTFChars(env, jDietPref, nativeDietPref);


    size_t bufferSize = foodCount * 256;
    char* resultString = malloc(bufferSize);
    if (resultString == NULL) return (*env)->NewStringUTF(env, "");
    resultString[0] = '\0';

    for (int i = 0; i < foodCount; i++) {
        int isNonVegDish = (strcmp(allFoods[i].category, "Non-Veg") == 0 || strcmp(allFoods[i].category, "Egg") == 0);

        if (isVeg && isNonVegDish) continue;
        if (allFoods[i].calories_per_100g <= 0) continue;

        char itemBuffer[256];

        snprintf(itemBuffer, sizeof(itemBuffer), "%s|%.0f|%.0f;",
                 allFoods[i].name, allFoods[i].calories_per_100g, allFoods[i].protein_per_100g);

        strcat(resultString, itemBuffer);
    }

    jstring finalResult = (*env)->NewStringUTF(env, resultString);
    free(resultString);

    return finalResult;
}