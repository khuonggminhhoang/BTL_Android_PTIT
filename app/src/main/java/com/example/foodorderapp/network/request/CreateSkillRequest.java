package com.example.foodorderapp.network.request;

import com.google.gson.annotations.SerializedName;

public class CreateSkillRequest {

    @SerializedName("userId")
    private int userId;

    @SerializedName("name")
    private String name;

    @SerializedName("level")
    private String level; // Ví dụ: "SKILL_LEVEL.BEGINNER"

    public CreateSkillRequest(int userId, String name, String level) {
        this.userId = userId;
        this.name = name;
        this.level = level;
    }

    // Getters (không bắt buộc, nhưng có thể hữu ích)
    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
