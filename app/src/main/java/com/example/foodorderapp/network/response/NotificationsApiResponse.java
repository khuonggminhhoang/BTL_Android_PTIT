package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Notification;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NotificationsApiResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Notification> data;
    @SerializedName("meta")
    private Object meta;

    // Getters
    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public List<Notification> getData() { return data; }
    public Object getMeta() { return meta; }

}