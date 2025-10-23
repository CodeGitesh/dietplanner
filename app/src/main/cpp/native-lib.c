#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <ctype.h>
#include <time.h>
#include <android/log.h>

#define LOG_TAG "DietPlanner"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


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

FoodItem* allFoods = NULL;
int foodCount = 0;
int foodsLoaded = 0;
RecommendedMeal* currentRecommendation = NULL;


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
    size_t nameLen = strlen(name);
    if (nameLen >= 255) return 0; // Prevent buffer overflow
    
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
    if (strstr(lowerName, "biryani")) return 1;
    if (strstr(lowerName, "curry") && (strstr(lowerName, "chicken") || strstr(lowerName, "mutton") || strstr(lowerName, "fish"))) return 1;

    return 0;
}

int isAppropriateForMealType(const char* foodName, const char* mealType) {
    if (!foodName || !mealType) return 0;
    
    char lowerName[256];
    size_t nameLen = strlen(foodName);
    if (nameLen >= 255) return 0;
    
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


JNIEXPORT void JNICALL
Java_com_example_dietplanner_data_CoreCalculator_loadMealsFromCSV(
        JNIEnv* env, jobject thiz, jstring filePath) {

    LOGI("Starting CSV load...");
    
    if (foodsLoaded) {
        cleanupFoodData();
    }

    const char* nativeFilePath = (*env)->GetStringUTFChars(env, filePath, 0);
    if (!nativeFilePath) {
        LOGE("Failed to get file path from JNI");
        return;
    }
    
    LOGI("Loading CSV from: %s", nativeFilePath);
    
    FILE* file = fopen(nativeFilePath, "r");
    (*env)->ReleaseStringUTFChars(env, filePath, nativeFilePath);

    if (file == NULL) {
        LOGE("Failed to open CSV file");
        return; // File not found or cannot be opened
    }

    char line[1024];
    // Skip header line
    if (fgets(line, sizeof(line), file) == NULL) {
        fclose(file);
        return;
    }

    while (fgets(line, sizeof(line), file)) {
        // Reallocate memory for new food item
        FoodItem* temp = realloc(allFoods, (foodCount + 1) * sizeof(FoodItem));
        if (temp == NULL) {
            // Memory allocation failed, cleanup and exit
            cleanupFoodData();
            fclose(file);
            return;
        }
        allFoods = temp;
        FoodItem* food = &allFoods[foodCount];

        // Parse CSV line safely - CSV format: Dish Name,Category,Calories (kcal),Carbohydrates (g),Protein (g),Fats (g)
        char* name = strtok(line, ",");           // 1. Dish Name
        char* category = strtok(NULL, ",");       // 2. Category (skip)
        char* calories = strtok(NULL, ",");      // 3. Calories (kcal)
        char* carbs = strtok(NULL, ",");         // 4. Carbohydrates (g) (skip)
        char* protein = strtok(NULL, ",");       // 5. Protein (g)
        char* fats = strtok(NULL, ",");          // 6. Fats (g) (skip)

        // Clean newline characters from the last token
        if (fats) {
            fats[strcspn(fats, "\r\n")] = 0;
        }

        // Validate and store data
        if (name && calories && protein) {
            food->name = strdup(name);
            food->calories_per_100g = atof(calories);
            food->protein_per_100g = atof(protein);
            
            // Only add if calories are valid
            if (food->calories_per_100g > 0) {
                foodCount++;
            } else {
                free(food->name);
            }
        }
    }
    fclose(file);
    foodsLoaded = 1;
    
    LOGI("CSV loading completed. Loaded %d food items", foodCount);
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

    LOGI("Getting filtered food list...");
    
    if (!foodsLoaded) {
        LOGE("Foods not loaded yet");
        return (*env)->NewStringUTF(env, "");
    }

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

    LOGI("Filtered food list generated, length: %zu", strlen(resultString));
    return finalResult;
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_generateMealRecommendation(
        JNIEnv* env, jobject thiz, jstring jMealType, jfloat targetCalories, jfloat targetProtein, jstring jDietPref) {
    
    LOGI("Generating meal recommendation...");
    
    if (!foodsLoaded || foodCount == 0) {
        LOGE("No foods loaded or foodCount is 0. foodsLoaded=%d, foodCount=%d", foodsLoaded, foodCount);
        return (*env)->NewStringUTF(env, "");
    }
    
    cleanupRecommendation();
    
    const char* mealType = (*env)->GetStringUTFChars(env, jMealType, NULL);
    const char* dietPref = (*env)->GetStringUTFChars(env, jDietPref, NULL);
    
    if (!mealType || !dietPref) {
        if (mealType) (*env)->ReleaseStringUTFChars(env, jMealType, mealType);
        if (dietPref) (*env)->ReleaseStringUTFChars(env, jDietPref, dietPref);
        return (*env)->NewStringUTF(env, "");
    }
    
    int isVeg = (strcmp(dietPref, "Vegetarian") == 0);
    
    currentRecommendation = malloc(sizeof(RecommendedMeal));
    if (!currentRecommendation) {
        (*env)->ReleaseStringUTFChars(env, jMealType, mealType);
        (*env)->ReleaseStringUTFChars(env, jDietPref, dietPref);
        return (*env)->NewStringUTF(env, "");
    }
    
    currentRecommendation->items = malloc(5 * sizeof(RecommendedItem));
    if (!currentRecommendation->items) {
        free(currentRecommendation);
        currentRecommendation = NULL;
        (*env)->ReleaseStringUTFChars(env, jMealType, mealType);
        (*env)->ReleaseStringUTFChars(env, jDietPref, dietPref);
        return (*env)->NewStringUTF(env, "");
    }
    
    currentRecommendation->itemCount = 0;
    currentRecommendation->totalCalories = 0;
    currentRecommendation->totalProtein = 0;
    
    srand(time(NULL));
    
    int attempts = 0;
    while (currentRecommendation->totalCalories < targetCalories * 0.8f && attempts < 100) {
        int randomIndex = rand() % foodCount;
        FoodItem* food = &allFoods[randomIndex];
        
        if (food->calories_per_100g <= 0) {
            attempts++;
            continue;
        }
        if (isVeg && isNonVeg(food->name)) {
            attempts++;
            continue;
        }
        if (!isAppropriateForMealType(food->name, mealType)) {
            attempts++;
            continue;
        }
        
        int quantity = 50 + (rand() % 150);
        float calories = (food->calories_per_100g / 100.0f) * quantity;
        float protein = (food->protein_per_100g / 100.0f) * quantity;
        
        if (currentRecommendation->totalCalories + calories > targetCalories * 1.2f) {
            attempts++;
            continue;
        }
        
        RecommendedItem* item = &currentRecommendation->items[currentRecommendation->itemCount];
        item->name = strdup(food->name);
        if (!item->name) {
            attempts++;
            continue;
        }
        
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

        if (strlen(buffer) + strlen(itemBuffer) < sizeof(buffer) - 1) {
            strcat(buffer, itemBuffer);
        } else {
            break;
        }
    }
    
    (*env)->ReleaseStringUTFChars(env, jMealType, mealType);
    (*env)->ReleaseStringUTFChars(env, jDietPref, dietPref);
    
    LOGI("Generated recommendation with %d items: %s", currentRecommendation->itemCount, buffer);
    return (*env)->NewStringUTF(env, buffer);
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_getAlternativeRecommendation(
        JNIEnv* env, jobject thiz) {
    
    if (!foodsLoaded || currentRecommendation == NULL) return (*env)->NewStringUTF(env, "");
    
    cleanupRecommendation();
    
    return (*env)->NewStringUTF(env, "");
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_debugGetFoodCount(
        JNIEnv* env, jobject thiz) {
    
    char buffer[256];
    snprintf(buffer, sizeof(buffer), "foodsLoaded=%d, foodCount=%d", foodsLoaded, foodCount);
    
    return (*env)->NewStringUTF(env, buffer);
}