package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Job;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SavedJobsApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Job> data; // Danh sách các công việc đã lưu

    // Trường meta có thể có hoặc không, tùy thuộc vào API của bạn
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

    public List<Job> getData() {
        return data;
    }

    public Object getMeta() {
        return meta;
    }

    // Setters (thường không cần thiết cho response)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<Job> data) {
        this.data = data;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }
}
