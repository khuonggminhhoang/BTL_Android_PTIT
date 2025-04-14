package com.example.foodorderapp.model;

// Lớp mô hình cho Danh mục công việc
public class Category {
    private String name;
    private int iconResId; // Resource ID cho icon (ví dụ: R.drawable.ic_remote)

    // Constructor
    public Category(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    // Setters (Nếu cần)
    public void setName(String name) {
        this.name = name;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
}
