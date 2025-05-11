package com.example.foodorderapp.core.model;

import com.google.gson.annotations.SerializedName; // << QUAN TRỌNG: Thêm import này
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

    @SerializedName("phone_number") // Khớp với cột DB 'phone_number'
    private String phoneNumber;

    @SerializedName("avatar") // Khớp với cột DB 'avatar'
    private String avatar;

    @SerializedName("headline") // Khớp với cột DB 'headline'
    private String headline;

    @SerializedName("location") // Khớp với cột DB 'location'
    private String location;

    @SerializedName("date_of_birth") // Khớp với cột DB 'date_of_birth'
    private String dateOfBirth;

    @SerializedName("about_me") // Khớp với cột DB 'about_me'
    private String aboutMe;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("deleted_at")
    private String deletedAt;

    // Các trường quan hệ (nếu API /profile/me có trả về, cũng cần @SerializedName nếu key JSON khác)
    // Ví dụ:
    // @SerializedName("user_jobs")
    private List<Job> jobs;
    // @SerializedName("user_notifications")
    private List<Notification> notifications;
    // @SerializedName("user_skills")
    private List<Skill> skills;
    // @SerializedName("user_experiences")
    private List<Experience> experiences;

    public User() {}

    // Constructor và các getter/setter giữ nguyên như bạn đã có
    // ... (toàn bộ getters và setters của bạn) ...
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