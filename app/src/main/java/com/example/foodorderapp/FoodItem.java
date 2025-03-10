package com.example.foodorderapp;

public class FoodItem {
    private String name;
    private String price;
    private String rating;
    private String deliveryTime;
    private int imageResId;

    public FoodItem(String name, String price, String rating, String deliveryTime, int imageResId) {
        this.name = name;
        this.price = price;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public int getImageResId() {
        return imageResId;
    }
}