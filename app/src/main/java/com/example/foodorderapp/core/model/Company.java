package com.example.foodorderapp.core.model;

import java.io.Serializable;

public class Company implements Serializable {
    private int id;
    private String name;
    private String logoUrl;
    private String description;
    private String website;
    private String industry;
    private String companySize;
    private String address;
    private boolean isTopCompany;
    private String createdAt;
    private String updatedAt;
    private String deletedAt;

    public Company() {}

    public Company(int id, String name, String logoUrl, String description, String website, String industry, String companySize, String address, boolean isTopCompany, String createdAt, String updatedAt, String deletedAt) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
        this.website = website;
        this.industry = industry;
        this.companySize = companySize;
        this.address = address;
        this.isTopCompany = isTopCompany;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public boolean isTopCompany() { return isTopCompany; }
    public void setTopCompany(boolean topCompany) { isTopCompany = topCompany; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getDeletedAt() { return deletedAt; }
    public void setDeletedAt(String deletedAt) { this.deletedAt = deletedAt; }
}