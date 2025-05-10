package com.example.foodorderapp.core.model;

import java.io.Serializable;
import java.util.List;

public class JobCategory implements Serializable {
    private int id;
    private String name;
    private String iconUrl;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private List<Job> jobs;

    public JobCategory() {}

    public JobCategory(int id, String name, String iconUrl, String createdAt, String updatedAt, String deletedAt, List<Job> jobs) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.jobs = jobs;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public List<Job> getJobs() { return jobs; }
    public void setJobs(List<Job> jobs) { this.jobs = jobs; }
} 