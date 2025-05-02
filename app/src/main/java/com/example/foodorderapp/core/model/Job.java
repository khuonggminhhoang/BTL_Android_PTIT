package com.example.foodorderapp.core.model; // Sử dụng package của bạn

import java.io.Serializable;
import java.util.List;

// Lớp mô hình cho Công việc - Thêm các trường thông tin công ty
public class Job implements Serializable {
    private String companyName;
    private String jobTitle;
    private String location;
    private String salary;
    private String postTime;
    private int companyLogoResId;
    private boolean isFavorite;
    private String description;
    private String companyInfo; // Mô tả ngắn về công ty (hiển thị trong tab Company)
    private int applicantCount;
    private List<String> tags;

    // --- Các trường mới cho thông tin chi tiết công ty ---
    private String website;
    private String industry;
    private String companySize;
    private String officeAddress;


    // Constructor cập nhật - Giờ có 15 tham số
    public Job(String companyName, String jobTitle, String location, String salary,
               String postTime, int companyLogoResId, boolean isFavorite,
               String description, String companyInfo, int applicantCount, List<String> tags,
               String website, String industry, String companySize, String officeAddress) { // Thêm 4 tham số mới
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.location = location;
        this.salary = salary;
        this.postTime = postTime;
        this.companyLogoResId = companyLogoResId;
        this.isFavorite = isFavorite;
        this.description = description;
        this.companyInfo = companyInfo; // Mô tả ngắn về công ty
        this.applicantCount = applicantCount;
        this.tags = tags;
        // Gán các trường mới
        this.website = website;
        this.industry = industry;
        this.companySize = companySize;
        this.officeAddress = officeAddress;
    }

    // Getters (Thêm getters cho các trường mới)
    public String getCompanyName() { return companyName; }
    public String getJobTitle() { return jobTitle; }
    public String getLocation() { return location; }
    public String getSalary() { return salary; }
    public String getPostTime() { return postTime; }
    public int getCompanyLogoResId() { return companyLogoResId; }
    public boolean isFavorite() { return isFavorite; }
    public String getDescription() { return description; }
    public String getCompanyInfo() { return companyInfo; } // Mô tả ngắn
    public int getApplicantCount() { return applicantCount; }
    public List<String> getTags() { return tags; }
    public String getWebsite() { return website; }
    public String getIndustry() { return industry; }
    public String getCompanySize() { return companySize; }
    public String getOfficeAddress() { return officeAddress; }


    // Setters (Thêm setters nếu cần)
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setDescription(String description) { this.description = description; }
    public void setCompanyInfo(String companyInfo) { this.companyInfo = companyInfo; }
    public void setApplicantCount(int applicantCount) { this.applicantCount = applicantCount; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setWebsite(String website) { this.website = website; }
    public void setIndustry(String industry) { this.industry = industry; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public void setOfficeAddress(String officeAddress) { this.officeAddress = officeAddress; }

}