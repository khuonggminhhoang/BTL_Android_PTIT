package com.example.foodorderapp.core.model;

import java.io.Serializable;

public class Notification implements Serializable {
    private int id;
    private String title;
    private String message;
    private boolean isRead;
    private int userId;
    private int applicationId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private User user;
    private Application application;

    public Notification() {}

    public Notification(int id, String title, String message, boolean isRead, int userId, int applicationId, String createdAt, String updatedAt, String deletedAt, User user, Application application) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.userId = userId;
        this.applicationId = applicationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.user = user;
        this.application = application;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
} 