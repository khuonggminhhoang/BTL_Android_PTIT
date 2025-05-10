package com.example.foodorderapp.core.model;

import java.io.Serializable;

public class Application implements Serializable {
    private int id;
    private int userId;
    private int jobId;
    private String status;
    private String resumeUrl;
    private String coverLetter;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private User user;
    private Job job;

    public Application() {}

    public Application(int id, int userId, int jobId, String status, String resumeUrl, String coverLetter, String createdAt, String updatedAt, String deletedAt, User user, Job job) {
        this.id = id;
        this.userId = userId;
        this.jobId = jobId;
        this.status = status;
        this.resumeUrl = resumeUrl;
        this.coverLetter = coverLetter;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.user = user;
        this.job = job;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
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