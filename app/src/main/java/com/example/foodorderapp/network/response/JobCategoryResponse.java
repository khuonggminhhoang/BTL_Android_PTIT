package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.JobCategory;
import com.google.gson.annotations.SerializedName;

import java.util.List;

// Lớp này đại diện cho cấu trúc phản hồi JSON từ API khi lấy danh sách các danh mục công việc
public class JobCategoryResponse {

    @SerializedName("success")
    private boolean success; // Cho biết yêu cầu API có thành công hay không

    @SerializedName("statusCode")
    private int statusCode; // Mã trạng thái HTTP của phản hồi

    @SerializedName("message")
    private String message; // Thông báo từ API (nếu có)

    @SerializedName("error") // Trường lỗi từ API (nếu có)
    private String error;

    @SerializedName("data")
    private List<JobCategory> data; // Danh sách các đối tượng JobCategory

    @SerializedName("meta") // Trường meta từ API (nếu có, thường chứa thông tin phân trang)
    private Object meta; // Sử dụng Object nếu cấu trúc meta không xác định hoặc không cần thiết cho việc phân tích cú pháp cụ thể

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

    public List<JobCategory> getData() {
        return data;
    }

    public Object getMeta() {
        return meta;
    }

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

    public void setError(String error) {
        this.error = error;
    }

    public void setData(List<JobCategory> data) {
        this.data = data;
    }

    public void setMeta(Object meta) {
        this.meta = meta;
    }
}
