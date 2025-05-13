package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Notification;
import com.google.gson.annotations.SerializedName;

public class NotificationDetailApiResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("statusCode")
    private int statusCode;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private Notification data;
    @SerializedName("meta")
    private Object meta;

    public boolean isSuccess() { return success; }
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Notification getData() { return data; }
    public Object getMeta() { return meta; }

}