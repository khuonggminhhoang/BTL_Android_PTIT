package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Application;
import com.google.gson.annotations.SerializedName;

public class ApplicationResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Application data; // Đối tượng Application được trả về sau khi tạo thành công

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

    public Application getData() {
        return data;
    }

    // Setters (Tùy chọn, thường không cần cho response)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Application data) {
        this.data = data;
    }
}
