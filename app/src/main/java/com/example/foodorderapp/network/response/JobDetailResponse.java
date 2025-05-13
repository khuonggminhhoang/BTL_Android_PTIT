package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Job; // Sử dụng lại Job model hiện có
import com.google.gson.annotations.SerializedName;

public class JobDetailResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Job data; // Dữ liệu là một đối tượng Job

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Job getData() {
        return data;
    }

    // Setters (Tùy chọn)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Job data) {
        this.data = data;
    }
}
