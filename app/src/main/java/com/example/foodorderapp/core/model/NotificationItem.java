package com.example.foodorderapp.core.model; // Hoặc package phù hợp

import java.io.Serializable;

public class NotificationItem implements Serializable {
    private String id;
    private String title;
    private String message;
    private String timestamp; // Hoặc long/Date
    private String iconUrl;   // URL ảnh icon
    private boolean isRead;
    private String type;      // Loại thông báo để xử lý click (vd: "job_applied", "new_message", "promotion")
    private String relatedId; // ID liên quan (vd: jobId, chatId, promotionId)

    // Constructor
    public NotificationItem(String id, String title, String message, String timestamp, String iconUrl, boolean isRead, String type, String relatedId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.iconUrl = iconUrl;
        this.isRead = isRead;
        this.type = type;
        this.relatedId = relatedId;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getIconUrl() { return iconUrl; }
    public boolean isRead() { return isRead; }
    public String getType() { return type; }
    public String getRelatedId() { return relatedId; }

    // Setters
    public void setRead(boolean read) { isRead = read; }
    // ... các setter khác nếu cần
}