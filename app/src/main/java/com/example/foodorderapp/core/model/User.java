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

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("headline")
    private String headline;

    @SerializedName("location")
    private String location;

    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("aboutMe")
    private String aboutMe;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("experiences")
    private List<Experience> experiences;

    @SerializedName("skills")
    private List<Skill> skills;

    @SerializedName("portfolio")
    private String resumeUrl;


    public User() {}

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
    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) {
        this.experiences = experiences;
    }
    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String resumeUrl) { this.resumeUrl = resumeUrl; }

}
