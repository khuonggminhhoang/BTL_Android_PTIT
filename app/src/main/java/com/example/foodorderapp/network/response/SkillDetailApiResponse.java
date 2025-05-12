package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Skill; // Đảm bảo import đúng model Skill
import com.google.gson.annotations.SerializedName;

public class SkillDetailApiResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("error") // Thêm trường error nếu API của bạn có trả về
    private String error;

    @SerializedName("data")
    private Skill data; // Dữ liệu là một đối tượng Skill duy nhất

    @SerializedName("meta") // Thêm trường meta nếu API của bạn có trả về
    private Object meta; // Kiểu Object vì chúng ta không biết rõ cấu trúc của meta, hoặc bạn có thể tạo lớp Meta nếu cần

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

    public String getError() {
        return error;
    }

    public Skill getData() {
        return data;
    }

    public Object getMeta() {
        return meta;
    }

    // Setters (Tùy chọn, thường không cần cho lớp response)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setData(Skill data) {
        this.data = data;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }
}

