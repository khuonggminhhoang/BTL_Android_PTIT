package com.example.foodorderapp.core.model; // Sử dụng package của bạn

import java.io.Serializable;
import java.util.List;

// Lớp mô hình cho Công việc - Đã thêm ID và đổi Logo sang URL
public class Job implements Serializable {
    private String id; // <<< THÊM: ID duy nhất cho công việc
    private String companyName;
    private String jobTitle;
    private String location;
    private String salary;
    private String postTime;
    private String companyLogoUrl; // <<< THAY ĐỔI: Logo từ URL
    private boolean isFavorite;
    private String description;
    private String companyInfo;
    private int applicantCount;
    private List<String> tags;
    private String website;
    private String industry;
    private String companySize;
    private String officeAddress;

    // Constructor cập nhật - Thêm id, thay logoResId bằng logoUrl
    public Job(String id, String companyName, String jobTitle, String location, String salary, // Thêm id
               String postTime, String companyLogoUrl, boolean isFavorite, // Thay logoResId
               String description, String companyInfo, int applicantCount, List<String> tags,
               String website, String industry, String companySize, String officeAddress) {
        this.id = id; // Gán id
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.location = location;
        this.salary = salary;
        this.postTime = postTime;
        this.companyLogoUrl = companyLogoUrl; // Gán logoUrl
        this.isFavorite = isFavorite;
        this.description = description;
        this.companyInfo = companyInfo;
        this.applicantCount = applicantCount;
        this.tags = tags;
        this.website = website;
        this.industry = industry;
        this.companySize = companySize;
        this.officeAddress = officeAddress;
    }

    // Getters (Thêm getter cho id, sửa getter cho logo)
    public String getId() { return id; } // <<< THÊM
    public String getCompanyName() { return companyName; }
    public String getJobTitle() { return jobTitle; }
    public String getLocation() { return location; }
    public String getSalary() { return salary; }
    public String getPostTime() { return postTime; }
    public String getCompanyLogoUrl() { return companyLogoUrl; } // <<< THAY ĐỔI
    public boolean isFavorite() { return isFavorite; }
    public String getDescription() { return description; }
    public String getCompanyInfo() { return companyInfo; }
    public int getApplicantCount() { return applicantCount; }
    public List<String> getTags() { return tags; }
    public String getWebsite() { return website; }
    public String getIndustry() { return industry; }
    public String getCompanySize() { return companySize; }
    public String getOfficeAddress() { return officeAddress; }

    // Setters (Thêm setter cho id, sửa setter cho logo, các setter khác nếu cần)
    public void setId(String id) { this.id = id; } // <<< THÊM
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public void setCompanyLogoUrl(String companyLogoUrl) { this.companyLogoUrl = companyLogoUrl; } // <<< THAY ĐỔI
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    // ... thêm các setter khác nếu bạn cần thay đổi giá trị sau khi tạo object

    // QUAN TRỌNG: Override equals và hashCode nếu bạn cần so sánh hoặc dùng trong Set/Map
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        // So sánh dựa trên ID duy nhất
        return id != null ? id.equals(job.id) : job.id == null;
    }

    @Override
    public int hashCode() {
        // Hash dựa trên ID duy nhất
        return id != null ? id.hashCode() : 0;
    }
}