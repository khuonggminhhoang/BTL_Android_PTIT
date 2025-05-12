package com.example.foodorderapp.core.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    // Giả sử API GET trả về key "phoneNumber" (camelCase) như API POST/Register
    // Nếu API GET trả về "phone_number" (snake_case) thì giữ nguyên @SerializedName("phone_number")
    @SerializedName("phoneNumber") // << KIỂM TRA KEY NÀY TỪ JSON RESPONSE CỦA API GET
    private String phoneNumber;

    // Giả sử API GET trả về key "avatar" (camelCase)
    @SerializedName("avatar")
    private String avatar;

    // Giả sử API GET trả về key "headline" (camelCase)
    @SerializedName("headline")
    private String headline;

    // Giả sử API GET trả về key "location" (camelCase)
    @SerializedName("location")
    private String location;

    // *** SỬA CHỖ NÀY ***
    @SerializedName("dateOfBirth") // << ĐỔI THÀNH "dateOfBirth" (camelCase) để khớp với JSON response của API GET
    private String dateOfBirth;

    // Giả sử API GET trả về key "aboutMe" (camelCase)
    @SerializedName("aboutMe")
    private String aboutMe;

    // Giả sử API GET trả về key "createdAt" (camelCase)
    @SerializedName("createdAt")
    private String createdAt;

    // Giả sử API GET trả về key "updatedAt" (camelCase)
    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("deletedAt") // Có thể là null, nên kiểu String là ổn
    private String deletedAt;


    private List<Job> jobs;
    private List<Notification> notifications;
    private List<Skill> skills;
    private List<Experience> experiences;

    public User() {}

    // Getters and Setters
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
    public List<Job> getJobs() { return jobs; }
    public void setJobs(List<Job> jobs) { this.jobs = jobs; }
    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills; }
    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }
}