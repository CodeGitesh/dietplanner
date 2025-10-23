#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <time.h>


typedef struct {
    char* name;
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

typedef struct {
    char* name;
    float calories;
    float protein;
    int quantity;
} RecommendedItem;

typedef struct {
    RecommendedItem* items;
    int itemCount;
    float totalCalories;
    float totalProtein;
} RecommendedMeal;

// Globals for storing food data
FoodItem* allFoods = NULL;
int foodCount = 0;
int foodsLoaded = 0;
RecommendedMeal* currentRecommendation = NULL;

// Function to free all dynamically allocated memory
void cleanupFoodData() {
    if (allFoods != NULL) {
        for (int i = 0; i < foodCount; i++) {
            free(allFoods[i].name);
        }
        free(allFoods);
        allFoods = NULL;
        foodCount = 0;
    }
    foodsLoaded = 0;
}

void cleanupRecommendation() {
    if (currentRecommendation != NULL) {
        for (int i = 0; i < currentRecommendation->itemCount; i++) {
            free(currentRecommendation->items[i].name);
        }
        free(currentRecommendation->items);
        free(currentRecommendation);
        currentRecommendation = NULL;
    }
}

// Helper function to check for non-veg keywords
int isNonVeg(const char* name) {
    if (!name) return 0;
    
    char lowerName[256];
    strncpy(lowerName, name, 255);
    lowerName[255] = '\0';

    for(int i = 0; lowerName[i]; i++){
        lowerName[i] = tolower(lowerName[i]);
    }

    // Meat-based keywords
    if (strstr(lowerName, "chicken")) return 1;
    if (strstr(lowerName, "mutton")) return 1;
    if (strstr(lowerName, "fish")) return 1;
    if (strstr(lowerName, "egg")) return 1;
    if (strstr(lowerName, "prawn")) return 1;
    if (strstr(lowerName, "keema")) return 1;
    if (strstr(lowerName, "salami")) return 1;
    if (strstr(lowerName, "ham")) return 1;
    if (strstr(lowerName, "bacon")) return 1;
    if (strstr(lowerName, "meat")) return 1;
    if (strstr(lowerName, "beef")) return 1;
    if (strstr(lowerName, "pork")) return 1;
    if (strstr(lowerName, "lamb")) return 1;
    if (strstr(lowerName, "goat")) return 1;
    if (strstr(lowerName, "turkey")) return 1;
    if (strstr(lowerName, "duck")) return 1;
    if (strstr(lowerName, "seafood")) return 1;
    if (strstr(lowerName, "shrimp")) return 1;
    if (strstr(lowerName, "crab")) return 1;
    if (strstr(lowerName, "lobster")) return 1;
    if (strstr(lowerName, "sausage")) return 1;
    if (strstr(lowerName, "patty")) return 1;
    if (strstr(lowerName, "cutlet")) return 1;
    if (strstr(lowerName, "kebab")) return 1;
    if (strstr(lowerName, "tikka")) return 1;
    if (strstr(lowerName, "biryani")) return 1; // Often contains meat
    if (strstr(lowerName, "curry") && (strstr(lowerName, "chicken") || strstr(lowerName, "mutton") || strstr(lowerName, "fish"))) return 1;

    return 0;
}

int isAppropriateForMealType(const char* foodName, const char* mealType) {
    char lowerName[256];
    strncpy(lowerName, foodName, 255);
    lowerName[255] = '\0';
    
    for(int i = 0; lowerName[i]; i++){
        lowerName[i] = tolower(lowerName[i]);
    }
    
    if (strcmp(mealType, "Breakfast") == 0) {
        if (strstr(lowerName, "tea")) return 1;
        if (strstr(lowerName, "coffee")) return 1;
        if (strstr(lowerName, "bread")) return 1;
        if (strstr(lowerName, "cereal")) return 1;
        if (strstr(lowerName, "milk")) return 1;
        if (strstr(lowerName, "egg")) return 1;
        if (strstr(lowerName, "poha")) return 1;
        if (strstr(lowerName, "upma")) return 1;
        if (strstr(lowerName, "idli")) return 1;
        if (strstr(lowerName, "dosa")) return 1;
        if (strstr(lowerName, "paratha")) return 1;
        if (strstr(lowerName, "dosa")) return 1;
    }
    else if (strcmp(mealType, "Lunch") == 0) {
        if (strstr(lowerName, "rice")) return 1;
        if (strstr(lowerName, "dal")) return 1;
        if (strstr(lowerName, "curry")) return 1;
        if (strstr(lowerName, "roti")) return 1;
        if (strstr(lowerName, "chicken")) return 1;
        if (strstr(lowerName, "mutton")) return 1;
        if (strstr(lowerName, "fish")) return 1;
        if (strstr(lowerName, "vegetable")) return 1;
        if (strstr(lowerName, "sabzi")) return 1;
    }
    else if (strcmp(mealType, "Dinner") == 0) {
        if (strstr(lowerName, "soup")) return 1;
        if (strstr(lowerName, "salad")) return 1;
        if (strstr(lowerName, "rice")) return 1;
        if (strstr(lowerName, "dal")) return 1;
        if (strstr(lowerName, "roti")) return 1;
        if (strstr(lowerName, "curry")) return 1;
        if (strstr(lowerName, "vegetable")) return 1;
    }
    return 0;
}

float calculateMealCalories(float dailyCalories, const char* mealType) {
    if (strcmp(mealType, "Breakfast") == 0) return dailyCalories * 0.25f;
    if (strcmp(mealType, "Lunch") == 0) return dailyCalories * 0.40f;
    if (strcmp(mealType, "Dinner") == 0) return dailyCalories * 0.35f;
    return dailyCalories * 0.33f;
}


// --- JNI Functions in Pure C ---

JNIEXPORT void JNICALL
Java_com_example_dietplanner_data_CoreCalculator_loadMealsFromCSV(
        JNIEnv* env, jobject thiz, jstring filePath) {

    if (foodsLoaded) {
        cleanupFoodData();
    }

    const char* nativeFilePath = (*env)->GetStringUTFChars(env, filePath, 0);
    FILE* file = fopen(nativeFilePath, "r");
    (*env)->ReleaseStringUTFChars(env, filePath, nativeFilePath);

    if (file == NULL) return;

    char line[1024];
    fgets(line, sizeof(line), file); // Skip header line

    while (fgets(line, sizeof(line), file)) {
        allFoods = realloc(allFoods, (foodCount + 1) * sizeof(FoodItem));
        FoodItem* food = &allFoods[foodCount];

        // *** THIS IS THE CORRECTED PARSING FOR YOUR ORIGINAL CSV ***
        // 1. Dish Name
        char* name = strtok(line, ",");
        // 2. Calories (kcal)
        char* calories = strtok(NULL, ",");
        // 3. Carbohydrates (g)
        char* carbs = strtok(NULL, ",");
        // 4. Protein (g)
        char* protein = strtok(NULL, ",");

        // Clean newline characters from the last token
        if (protein) protein[strcspn(protein, "\r\n")] = 0;

        food->name = (name) ? strdup(name) : strdup("");
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

// Returns a formatted string of all food items based on dietary preference
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
        if (isVeg && isNonVeg(allFoods[i].name)) {
            continue; // Skip non-veg items if user is vegetarian
        }
        if (allFoods[i].calories_per_100g <= 0) continue;

        char itemBuffer[256];
        // Format: Name|Calories|Protein;
        snprintf(itemBuffer, sizeof(itemBuffer), "%s|%.2f|%.2f;",
                 allFoods[i].name, allFoods[i].calories_per_100g, allFoods[i].protein_per_100g);

        strcat(resultString, itemBuffer);
    }

    jstring finalResult = (*env)->NewStringUTF(env, resultString);
    free(resultString);

    return finalResult;
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_generateMealRecommendation(
        JNIEnv* env, jobject thiz, jstring jMealType, jfloat targetCalories, jfloat targetProtein, jstring jDietPref) {
    
    if (!foodsLoaded) return (*env)->NewStringUTF(env, "");
    
    cleanupRecommendation();
    
    const char* mealType = (*env)->GetStringUTFChars(env, jMealType, NULL);
    const char* dietPref = (*env)->GetStringUTFChars(env, jDietPref, NULL);
    int isVeg = (strcmp(dietPref, "Vegetarian") == 0);
    
    currentRecommendation = malloc(sizeof(RecommendedMeal));
    currentRecommendation->items = malloc(5 * sizeof(RecommendedItem));
    currentRecommendation->itemCount = 0;
    currentRecommendation->totalCalories = 0;
    currentRecommendation->totalProtein = 0;
    
    srand(time(NULL));
    
    int attempts = 0;
    while (currentRecommendation->totalCalories < targetCalories * 0.8f && attempts < 100) {
        int randomIndex = rand() % foodCount;
        FoodItem* food = &allFoods[randomIndex];
        
        if (food->calories_per_100g <= 0) continue;
        if (isVeg && isNonVeg(food->name)) continue;
        if (!isAppropriateForMealType(food->name, mealType)) continue;
        
        int quantity = 50 + (rand() % 150);
        float calories = (food->calories_per_100g / 100.0f) * quantity;
        float protein = (food->protein_per_100g / 100.0f) * quantity;
        
        if (currentRecommendation->totalCalories + calories > targetCalories * 1.2f) continue;
        
        RecommendedItem* item = &currentRecommendation->items[currentRecommendation->itemCount];
        item->name = strdup(food->name);
        item->calories = calories;
        item->protein = protein;
        item->quantity = quantity;
        
        currentRecommendation->totalCalories += calories;
        currentRecommendation->totalProtein += protein;
        currentRecommendation->itemCount++;
        
        if (currentRecommendation->itemCount >= 5) break;
        attempts++;
    }
    
    char buffer[2048];
    buffer[0] = '\0';
    
    for (int i = 0; i < currentRecommendation->itemCount; i++) {
        char itemBuffer[256];
        snprintf(itemBuffer, sizeof(itemBuffer), "%s|%.2f|%.2f|%d;",
                 currentRecommendation->items[i].name,
                 currentRecommendation->items[i].calories,
                 currentRecommendation->items[i].protein,
                 currentRecommendation->items[i].quantity);
        strcat(buffer, itemBuffer);
    }
    
    (*env)->ReleaseStringUTFChars(env, jMealType, mealType);
    (*env)->ReleaseStringUTFChars(env, jDietPref, dietPref);
    
    return (*env)->NewStringUTF(env, buffer);
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_getAlternativeRecommendation(
        JNIEnv* env, jobject thiz) {
    
    if (!foodsLoaded || currentRecommendation == NULL) return (*env)->NewStringUTF(env, "");
    
    cleanupRecommendation();
    
    return (*env)->NewStringUTF(env, "");
}