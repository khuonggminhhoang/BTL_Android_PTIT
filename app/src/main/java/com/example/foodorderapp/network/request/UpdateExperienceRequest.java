package com.example.foodorderapp.network.request; // Hoặc package request của bạn

import com.google.gson.annotations.SerializedName;

public class UpdateExperienceRequest {

    @SerializedName("title")
    private String title;

    @SerializedName("companyName")
    private String companyName;

    @SerializedName("startDate")
    private String startDate; // Định dạng ISO Date String, ví dụ: "YYYY-MM-DD" hoặc "YYYY-MM-DDTHH:mm:ss.SSSZ"

    @SerializedName("endDate")
    private String endDate; // Tương tự startDate, có thể null

    @SerializedName("description")
    private String description;

    // Constructor có thể hữu ích
    public UpdateExperienceRequest(String title, String companyName, String startDate, String endDate, String description) {
        this.title = title;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    // Getters (tùy chọn)
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDescription() { return description; }

    // Setters (quan trọng)
    public void setTitle(String title) { this.title = title; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setDescription(String description) { this.description = description; }
}
