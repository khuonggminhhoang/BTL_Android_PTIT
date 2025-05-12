package com.example.foodorderapp.core.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Experience implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    // SỬA Ở ĐÂY: Khớp với key "companyName" từ JSON API
    @SerializedName("companyName")
    private String companyName;

    // SỬA Ở ĐÂY: Khớp với key "startDate" từ JSON API
    @SerializedName("startDate")
    private String startDate;

    // SỬA Ở ĐÂY: Khớp với key "endDate" từ JSON API
    @SerializedName("endDate")
    private String endDate;

    @SerializedName("description")
    private String description;

    // Các trường như userId, createdAt, updatedAt có thể được giữ lại nếu API trả về
    // và bạn cần dùng chúng, hoặc bỏ đi nếu không cần thiết trong model này.
    // Ví dụ, nếu JSON API trả về "userId":
    // @SerializedName("userId")
    // private int userId;


    public Experience() {}

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDescription() { return description; }

    // Setters (Nếu bạn cần tạo đối tượng Experience ở client hoặc cho testing)
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setDescription(String description) { this.description = description; }
}
