package com.example.foodorderapp.core.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Skill implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("name") // Từ cột DB: name
    private String name;

    @SerializedName("level") // Từ cột DB: level (API nên trả về chuỗi như "BEGINNER", "INTERMEDIATE", "ADVANCE")
    private String level;

    // @SerializedName("user_id")
    // private int userId;

    public Skill() {}

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLevel(String level) { this.level = level; }
}
