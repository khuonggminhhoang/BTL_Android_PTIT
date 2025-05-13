package com.example.foodorderapp.network.response;

import com.example.foodorderapp.core.model.Job;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedJobResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("statusCode")
    private int statusCode;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<Job> data;

    @SerializedName("meta")
    private Meta meta;

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

    public List<Job> getData() {
        return data;
    }

    public Meta getMeta() {
        return meta;
    }

    // Lớp Meta lồng nhau để chứa thông tin phân trang
    public static class Meta {
        @SerializedName("totalItems")
        private int totalItems;

        @SerializedName("currentPage")
        private int currentPage;

        @SerializedName("pageSize")
        private int pageSize; // Thêm pageSize nếu API trả về

        @SerializedName("totalPages")
        private int totalPages;

        // Getters
        public int getTotalItems() {
            return totalItems;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getTotalPages() {
            return totalPages;
        }
    }
}
