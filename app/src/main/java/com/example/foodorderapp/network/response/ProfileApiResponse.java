package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.User;
import com.google.gson.annotations.SerializedName;

public class ProfileApiResponse {

    @SerializedName("success") // Hoặc tên trường tương ứng từ API của bạn
    private boolean success;

    @SerializedName("statusCode") // Hoặc tên trường tương ứng
    private int statusCode;

    @SerializedName("message") // Hoặc tên trường tương ứng
    private String message;

    @SerializedName("data") // QUAN TRỌNG: Tên trường chứa đối tượng User
    private User data; // Hoặc private User user; nếu key trong JSON là "user"

    // Getters
    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public User getData() { return data; } // Hoặc public User getUser()
}