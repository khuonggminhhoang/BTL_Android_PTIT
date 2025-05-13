package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Experience; // Đảm bảo import đúng model Experience
import com.google.gson.annotations.SerializedName;

public class ExperienceDetailApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("data")
    private Experience data;

    @SerializedName("meta")
    private Object meta;


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

    public String getError() {
        return error;
    }

    public Experience getData() {
        return data;
    }

    public Object getMeta() {
        return meta;
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

    public void setError(String error) {
        this.error = error;
    }

    public void setData(Experience data) {
        this.data = data;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }
}
