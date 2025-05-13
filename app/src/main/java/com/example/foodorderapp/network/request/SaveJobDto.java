package com.example.foodorderapp.network.request;

import com.google.gson.annotations.SerializedName;

public class SaveJobDto {
    @SerializedName("jobId")
    private int jobId;

    public SaveJobDto(int jobId) {
        this.jobId = jobId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
