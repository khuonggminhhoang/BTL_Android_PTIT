package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Experience; // Đảm bảo import đúng model Experience
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExperiencesApiResponse {

    @SerializedName("success")
    private boolean success;
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Experience> data; // Trường data ở đây là List<Experience>

    // Getters
    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public List<Experience> getData() { return data; }
}