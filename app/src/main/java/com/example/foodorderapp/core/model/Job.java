package com.example.foodorderapp.core.model;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {
    private int id;
    private String title;
    private String description;
    private String location;
    private String salaryMin;
    private String salaryMax;
    private String salaryPeriod;
    private String jobType;
    private boolean isTopJob;
    private String status;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;
    private int companyId;
    private int categoryId;
    private Company company;
    private JobCategory category;
    private List<User> users;

    public Job() {}

    public Job(int id, String title, String description, String location, String salaryMin, String salaryMax, String salaryPeriod, String jobType, boolean isTopJob, String status, String createdAt, String updatedAt, String deletedAt, int companyId, int categoryId, Company company, JobCategory category, List<User> users) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryPeriod = salaryPeriod;
        this.jobType = jobType;
        this.isTopJob = isTopJob;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.companyId = companyId;
        this.categoryId = categoryId;
        this.company = company;
        this.category = category;
        this.users = users;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSalaryMin() { return salaryMin; }
    public void setSalaryMin(String salaryMin) { this.salaryMin = salaryMin; }
    public String getSalaryMax() { return salaryMax; }
    public void setSalaryMax(String salaryMax) { this.salaryMax = salaryMax; }
    public String getSalaryPeriod() { return salaryPeriod; }
    public void setSalaryPeriod(String salaryPeriod) { this.salaryPeriod = salaryPeriod; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public boolean isTopJob() { return isTopJob; }
    public void setTopJob(boolean topJob) { isTopJob = topJob; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public JobCategory getCategory() { return category; }
    public void setCategory(JobCategory category) { this.category = category; }
    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}