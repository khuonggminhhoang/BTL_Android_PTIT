package com.example.foodorderapp;

public class SpecialFoodItem {
    private String time;
    private String name;
    private int imageResId;

    public SpecialFoodItem(String time, String name, int imageResId) {
        this.time = time;
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}