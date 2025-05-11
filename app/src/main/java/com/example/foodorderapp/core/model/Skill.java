package com.example.foodorderapp.core.model;

import java.io.Serializable;

public class Skill implements Serializable {
    private int id;
    private String name;
    private String level;
    private int userId;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private User user;

    public Skill() {}

    public Skill(int id, String name, String level, int userId, String createdAt, String updatedAt, String deletedAt, User user) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.user = user;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
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
} 