package com.example.dietplanner.data;

public class CoreCalculator {
    static {
        try {
            System.loadLibrary("dietplanner-lib");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load native library: " + e.getMessage());
            throw new RuntimeException("Native library not available", e);
        }
    }

    public native void load_csv(String path);
    public native String get_goals(String name, int age, float weight, float height, String gender);
    public native String get_foods(String diet);
    public native String get_recommendation(String meal, float tarcalorie, float tarprotein, String diet);
    public native String get_alt_recommendation();

}