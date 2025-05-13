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

    // Giả sử API GET /profile/me trả về key "dateOfBirth" (camelCase) cho ngày sinh
    // Nếu API GET trả về "date_of_birth" (snake_case), hãy sửa lại @SerializedName ở đây
    @SerializedName("dateOfBirth")
    private String dateOfBirth;

    @SerializedName("aboutMe") // Sửa thành "aboutMe" để khớp với JSON từ API
    private String aboutMe;

    @SerializedName("createdAt") // Giả sử API GET trả về "createdAt"
    private String createdAt;

    @SerializedName("updatedAt") // Giả sử API GET trả về "updatedAt"
    private String updatedAt;

    // Giả sử API GET /profile/me trả về danh sách experiences với key là "experiences"
    @SerializedName("experiences")
    private List<Experience> experiences;

    // Giả sử API GET /profile/me trả về danh sách skills với key là "skills"
    @SerializedName("skills")
    private List<Skill> skills;

    // Thêm trường này để lưu URL/tên file CV từ API
    // Thay "resume_url" bằng key JSON thực tế mà API GET /profile/me trả về cho thông tin CV
    @SerializedName("portfolio") // Sửa "resume_url" thành "portfolio"
    private String resumeUrl;

    // @SerializedName("resume_last_uploaded") // Ví dụ cho ngày upload CV, nếu API có trả về
    // private String resumeLastUploaded;

    public User() {}

    // Getters and Setters (đầy đủ cho các trường bạn cần)
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
    // public String getResumeLastUploaded() { return resumeLastUploaded; }
    // public void setResumeLastUploaded(String resumeLastUploaded) { this.resumeLastUploaded = resumeLastUploaded; }

}
