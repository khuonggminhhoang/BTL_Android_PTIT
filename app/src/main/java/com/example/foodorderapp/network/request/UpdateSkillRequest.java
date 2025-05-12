package com.example.foodorderapp.network.request; // Hoặc package request của bạn

import com.google.gson.annotations.SerializedName;

public class UpdateSkillRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("level")
    private String level; // Sẽ gửi dạng "SKILL_LEVEL.BEGINNER", "SKILL_LEVEL.INTERMEDIATE", v.v.

    public UpdateSkillRequest(String name, String level) {
        this.name = name;
        this.level = level;
    }

    // Getters (không bắt buộc cho request body, nhưng có thể hữu ích)
    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    // Setters (quan trọng để tạo đối tượng trước khi gửi)
    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
