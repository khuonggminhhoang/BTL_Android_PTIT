package com.example.foodorderapp.core.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    private int id;
    private String name;
    private String email;
    private String username;
    private String phoneNumber;
    private String avatar;
    private String headline;
    private String location;
    private String dateOfBirth;
    private String aboutMe;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    // Quan hệ
    private List<Job> jobs;
    private List<Notification> notifications;
    private List<Skill> skills;
    private List<Experience> experiences;

    public User() {}

    public User(int id, String name, String email, String username, String phoneNumber, String avatar, String headline,
                String location, String dateOfBirth, String aboutMe, String createdAt, String updatedAt, String deletedAt,
                List<Skill> skills, List<Experience> experiences) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.avatar = avatar;
        this.headline = headline;
        this.location = location;
        this.dateOfBirth = dateOfBirth;
        this.aboutMe = aboutMe;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.skills = skills;
        this.experiences = experiences;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getAboutMe() { return aboutMe; }
    public void setAboutMe(String aboutMe) { this.aboutMe = aboutMe; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }
    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }
}