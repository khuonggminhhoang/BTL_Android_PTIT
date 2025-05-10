package com.example.foodorderapp.core.model;

import java.io.Serializable;

public class SavedJob implements Serializable {
    private int jobId;
    private int userId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private User user;
    private Job job;

    public SavedJob() {}

    public SavedJob(int jobId, int userId, String createdAt, String updatedAt, String deletedAt, User user, Job job) {
        this.jobId = jobId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.user = user;
        this.job = job;
    }

    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Job getJob() { return job; }
    public void setJob(Job job) { this.job = job; }
} 