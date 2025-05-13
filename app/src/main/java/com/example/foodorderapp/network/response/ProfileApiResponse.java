package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.User;
import com.google.gson.annotations.SerializedName;

public class ProfileApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private User data;

    // Getters
    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public User getData() { return data; } // Hoặc public User getUser()
}