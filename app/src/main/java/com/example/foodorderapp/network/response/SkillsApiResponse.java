package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Skill;

import java.util.List;

public class SkillsApiResponse { // Bạn nên tạo file riêng cho class này
    @com.google.gson.annotations.SerializedName("success")
    private boolean success;
    @com.google.gson.annotations.SerializedName("statusCode")
    private int statusCode;
    @com.google.gson.annotations.SerializedName("message")
    private String message;
    @com.google.gson.annotations.SerializedName("data")
    private List<Skill> data;

    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public List<Skill> getData() { return data; }
}
