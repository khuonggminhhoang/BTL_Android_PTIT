package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Company;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CompaniesApiResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Company> data;

    // Thêm trường meta nếu API của bạn có trả về (ví dụ: thông tin phân trang)
    // @SerializedName("meta")
    // private Object meta;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public List<Company> getData() {
        return data;
    }

    // public Object getMeta() {
    //     return meta;
    // }

    // Setters (Tùy chọn, thường không cần thiết cho các lớp phản hồi)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(List<Company> data) {
        this.data = data;
    }

    // public void setMeta(Object meta) {
    //    this.meta = meta;
    // }
}
