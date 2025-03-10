package com.example.foodorderapp;

public class FoodItem {
    private String name;
    private String price;
    private String rating;
    private String time;
    private int imageResId;

    // Constructor
    public FoodItem(String name, String price, String rating, String time, int imageResId) {
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.time = time;
        this.imageResId = imageResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public String getTime() {
        return time;
    }

    public int getImageResId() {
        return imageResId;
    }
}