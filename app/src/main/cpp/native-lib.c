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

FoodItem* foods = NULL;
int food_count = 0;
int loaded = 0;
RecommendedMeal* current_rec = NULL;


void cleanup_food() {
    if (foods != NULL) {
        for (int i = 0; i < food_count; i++) {
            free(foods[i].name);
        }
        free(foods);
        foods = NULL;
        food_count = 0;
    }
    loaded = 0;
}

void cleanup_rec() {
    if (current_rec != NULL) {
        for (int i = 0; i < current_rec->itemCount; i++) {
            free(current_rec->items[i].name);
        }
        free(current_rec->items);
        free(current_rec);
        current_rec = NULL;
    }
}

// Helper function to check for non-veg keywords
int isnonveg(const char* name) {
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

int ismealok(const char* foodName, const char* mealType) {
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

float calc_meal_cal(float dailyCalories, const char* mealType) {
    if (strcmp(mealType, "Breakfast") == 0) return dailyCalories * 0.25f;
    if (strcmp(mealType, "Lunch") == 0) return dailyCalories * 0.40f;
    if (strcmp(mealType, "Dinner") == 0) return dailyCalories * 0.35f;
    return dailyCalories * 0.33f;
}


JNIEXPORT void JNICALL
Java_com_example_dietplanner_data_CoreCalculator_load_1csv(
        JNIEnv* env, jobject thiz, jstring path) {

    if (loaded) {
        cleanup_food();
    }

    const char* filepath = (*env)->GetStringUTFChars(env, path, 0);
    if (!filepath) {
        return;
    }
    
    FILE* file = fopen(filepath, "r");
    (*env)->ReleaseStringUTFChars(env, path, filepath);

    if (file == NULL) {
        return;
    }

    char line[1024];
    // Skip header line
    if (fgets(line, sizeof(line), file) == NULL) {
        fclose(file);
        return;
    }

    while (fgets(line, sizeof(line), file)) {
        // Reallocate memory for new food item
        FoodItem* temp = realloc(foods, (food_count + 1) * sizeof(FoodItem));
        if (temp == NULL) {
            // Memory allocation failed, cleanup and exit
            cleanup_food();
            fclose(file);
            return;
        }
        foods = temp;
        FoodItem* food = &foods[food_count];

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
                food_count++;
            } else {
                free(food->name);
            }
        }
    }
    fclose(file);
    loaded = 1;
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_get_1goals(
        JNIEnv* env, jobject thiz, jstring name, jint age, jfloat weight, jfloat height, jstring gender) {

    const char* name_str = (*env)->GetStringUTFChars(env, name, NULL);
    const char* gender_str = (*env)->GetStringUTFChars(env, gender, NULL);

    float bmr = (10 * weight) + (6.25 * height) - (5 * age);
    if (strcmp(gender_str, "Male") == 0) { bmr += 5; } else { bmr -= 161; }

    float tdee = bmr * 1.375f;
    float target_calories = tdee * 0.80f;
    int protein_g = (int)round((target_calories * 0.30f) / 4.0f);

    char buffer[512];
    snprintf(buffer, sizeof(buffer),
             "Daily Calorie Target: %d kcal\n"
             "Daily Protein Target: %dg",
             (int)round(target_calories), protein_g);

    (*env)->ReleaseStringUTFChars(env, name, name_str);
    (*env)->ReleaseStringUTFChars(env, gender, gender_str);
    return (*env)->NewStringUTF(env, buffer);
}

// Returns a formatted string of all food items based on dietary preference
JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_get_1foods(
        JNIEnv *env, jobject thiz, jstring diet) {

    if (!loaded) {
        return (*env)->NewStringUTF(env, "");
    }

    const char* dietpref = (*env)->GetStringUTFChars(env, diet, NULL);
    int isveg = (strcmp(dietpref, "Vegetarian") == 0);
    (*env)->ReleaseStringUTFChars(env, diet, dietpref);

    size_t bufferSize = food_count * 256;
    char* resultString = malloc(bufferSize);
    if (resultString == NULL) return (*env)->NewStringUTF(env, "");
    resultString[0] = '\0';

    for (int i = 0; i < food_count; i++) {
        if (isveg && isnonveg(foods[i].name)) {
            continue;
        }
        if (foods[i].calories_per_100g <= 0) continue;

        char itemBuffer[256];
        snprintf(itemBuffer, sizeof(itemBuffer), "%s|%.2f|%.2f;",
                 foods[i].name, foods[i].calories_per_100g, foods[i].protein_per_100g);

        strcat(resultString, itemBuffer);
    }

    jstring finalResult = (*env)->NewStringUTF(env, resultString);
    free(resultString);

    return finalResult;
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_get_1recommendation(
        JNIEnv* env, jobject thiz, jstring meal, jfloat tarcalorie, jfloat tarprotein, jstring diet) {
    
    if (!loaded || food_count == 0) {
        return (*env)->NewStringUTF(env, "");
    }
    
    cleanup_rec();
    
    const char* meal_type = (*env)->GetStringUTFChars(env, meal, NULL);
    const char* dietpref = (*env)->GetStringUTFChars(env, diet, NULL);
    
    if (!meal_type || !dietpref) {
        if (meal_type) (*env)->ReleaseStringUTFChars(env, meal, meal_type);
        if (dietpref) (*env)->ReleaseStringUTFChars(env, diet, dietpref);
        return (*env)->NewStringUTF(env, "");
    }
    
    int isveg = (strcmp(dietpref, "Vegetarian") == 0);
    
    current_rec = malloc(sizeof(RecommendedMeal));
    if (!current_rec) {
        (*env)->ReleaseStringUTFChars(env, meal, meal_type);
        (*env)->ReleaseStringUTFChars(env, diet, dietpref);
        return (*env)->NewStringUTF(env, "");
    }
    
    current_rec->items = malloc(5 * sizeof(RecommendedItem));
    if (!current_rec->items) {
        free(current_rec);
        current_rec = NULL;
        (*env)->ReleaseStringUTFChars(env, meal, meal_type);
        (*env)->ReleaseStringUTFChars(env, diet, dietpref);
        return (*env)->NewStringUTF(env, "");
    }
    
    current_rec->itemCount = 0;
    current_rec->totalCalories = 0;
    current_rec->totalProtein = 0;
    
    srand(time(NULL));
    
    int attempts = 0;
    while (current_rec->totalCalories < tarcalorie * 0.8f && attempts < 100) {
        int randomIndex = rand() % food_count;
        FoodItem* food = &foods[randomIndex];
        
        if (food->calories_per_100g <= 0) {
            attempts++;
            continue;
        }
        if (isveg && isnonveg(food->name)) {
            attempts++;
            continue;
        }
        if (!ismealok(food->name, meal_type)) {
            attempts++;
            continue;
        }
        
        int quantity = 50 + (rand() % 150);
        float food_calories = (food->calories_per_100g / 100.0f) * quantity;
        float food_protein = (food->protein_per_100g / 100.0f) * quantity;
        
        if (current_rec->totalCalories + food_calories > tarcalorie * 1.2f) {
            attempts++;
            continue;
        }
        
        RecommendedItem* item = &current_rec->items[current_rec->itemCount];
        item->name = strdup(food->name);
        if (!item->name) {
            attempts++;
            continue;
        }
        
        item->calories = food_calories;
        item->protein = food_protein;
        item->quantity = quantity;
        
        current_rec->totalCalories += food_calories;
        current_rec->totalProtein += food_protein;
        current_rec->itemCount++;
        
        if (current_rec->itemCount >= 5) break;
        attempts++;
    }
    
    char buffer[2048];
    buffer[0] = '\0';
    
    for (int i = 0; i < current_rec->itemCount; i++) {
        char itemBuffer[256];
        snprintf(itemBuffer, sizeof(itemBuffer), "%s|%.2f|%.2f|%d;",
                 current_rec->items[i].name,
                 current_rec->items[i].calories,
                 current_rec->items[i].protein,
                 current_rec->items[i].quantity);

        if (strlen(buffer) + strlen(itemBuffer) < sizeof(buffer) - 1) {
            strcat(buffer, itemBuffer);
        } else {
            break;
        }
    }
    
    (*env)->ReleaseStringUTFChars(env, meal, meal_type);
    (*env)->ReleaseStringUTFChars(env, diet, dietpref);
    
    return (*env)->NewStringUTF(env, buffer);
}

JNIEXPORT jstring JNICALL
Java_com_example_dietplanner_data_CoreCalculator_get_1alt_1recommendation(
        JNIEnv* env, jobject thiz) {
    
    if (!loaded || current_rec == NULL) return (*env)->NewStringUTF(env, "");
    
    cleanup_rec();
    
    return (*env)->NewStringUTF(env, "");
}
