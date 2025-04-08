package com.example.foodorderapp.model;

public class MenuItem {
    private int iconResId;
    private String title;
    private String category;

    public MenuItem(int iconResId, String title, String category) {
        this.iconResId = iconResId;
        this.title = title;
        this.category = category;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }
}