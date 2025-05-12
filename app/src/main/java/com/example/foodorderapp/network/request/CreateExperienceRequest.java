package com.example.foodorderapp.network.request;

import com.google.gson.annotations.SerializedName;

public class CreateExperienceRequest {

    @SerializedName("userId")
    private int userId;

    @SerializedName("title")
    private String title;

    @SerializedName("companyName")
    private String companyName;

    @SerializedName("startDate")
    private String startDate; // Định dạng ISO Date String, ví dụ: "YYYY-MM-DD'T'HH:mm:ss.SSSZ'"

    @SerializedName("endDate")
    private String endDate; // Tương tự startDate, có thể null

    @SerializedName("description")
    private String description;

    public CreateExperienceRequest(int userId, String title, String companyName, String startDate, String endDate, String description) {
        this.userId = userId;
        this.title = title;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    // Getters (tùy chọn)
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDescription() { return description; }

    // Setters
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setDescription(String description) { this.description = description; }
}
