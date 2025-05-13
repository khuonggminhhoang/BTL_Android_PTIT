package com.example.foodorderapp.features.jobs.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.JobDetailResponse;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JobDetailActivity extends AppCompatActivity {

    private static final String TAG = "JobDetailActivity";

    private ImageView ivCompanyLogoDetail;
    private TextView tvCompanyNameDetail, tvJobTitleDetail, tvLocationDetail, tvApplicants, tvSalaryDetail;
    private ImageButton btnBack;
    // btnFavoriteDetail đã được xóa
    private Button btnApply;
    private TabLayout tabLayout;
    private LinearLayout layoutTags;
    private FrameLayout tabContentContainer;
    private LinearLayout layoutDescriptionContent;
    private TextView tvDescription;
    private LinearLayout layoutCompanyContent;
    private TextView tvCompanyDescription, tvWebsite, tvIndustry, tvCompanySize, tvOfficeAddress;
    private ProgressBar progressBarJobDetail;

    private ApiService apiService;
    private Job currentJob;
    private int jobIdToFetch = -1;
    private String currentAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        findViews();
        initApiService();
        setupToolbar();

        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);

        getIntentData();

        if (jobIdToFetch != -1) {
            fetchJobDetails(jobIdToFetch);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin công việc.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void findViews() {
        ivCompanyLogoDetail = findViewById(R.id.ivCompanyLogoDetail);
        tvCompanyNameDetail = findViewById(R.id.tvCompanyNameDetail);
        tvJobTitleDetail = findViewById(R.id.tvJobTitleDetail);
        tvLocationDetail = findViewById(R.id.tvLocationDetail);
        tvApplicants = findViewById(R.id.tvApplicants);
        tvSalaryDetail = findViewById(R.id.tvSalaryDetail);
        btnBack = findViewById(R.id.btnBack);
        // btnFavoriteDetail đã được xóa khỏi đây
        btnApply = findViewById(R.id.btnApply);
        tabLayout = findViewById(R.id.tabLayout);
        layoutTags = findViewById(R.id.layoutTags);
        tabContentContainer = findViewById(R.id.tabContentContainer);
        layoutDescriptionContent = findViewById(R.id.layoutDescriptionContent);
        tvDescription = findViewById(R.id.tvDescription);
        layoutCompanyContent = findViewById(R.id.layoutCompanyContent);
        tvCompanyDescription = findViewById(R.id.tvCompanyDescription);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvIndustry = findViewById(R.id.tvIndustry);
        tvCompanySize = findViewById(R.id.tvCompanySize);
        tvOfficeAddress = findViewById(R.id.tvOfficeAddress);
        progressBarJobDetail = findViewById(R.id.progressBarJobDetail);
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(JobAdapter.JOB_DETAIL_KEY)) {
            Job jobFromIntent = (Job) intent.getSerializableExtra(JobAdapter.JOB_DETAIL_KEY);
            if (jobFromIntent != null) {
                jobIdToFetch = jobFromIntent.getId();
            }
        } else if (intent != null && intent.hasExtra("JOB_ID")) {
            jobIdToFetch = intent.getIntExtra("JOB_ID", -1);
        }
    }

    private void showLoading(boolean isLoading) {
        if (progressBarJobDetail != null) {
            progressBarJobDetail.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        View contentLayout = findViewById(R.id.scrollViewJobDetail);
        if (contentLayout != null) {
            contentLayout.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
        if (btnApply != null) {
            btnApply.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }


    private void fetchJobDetails(int jobId) {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);

        Call<JobDetailResponse> call = apiService.getJobDetail(jobId);
        call.enqueue(new Callback<JobDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobDetailResponse> call, @NonNull Response<JobDetailResponse> response) {
                showLoading(false);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentJob = response.body().getData();
                    if (currentJob != null) {
                        populateCommonUi();
                        setupTabLayout();
                        setupClickListeners();
                    } else {
                        Toast.makeText(JobDetailActivity.this, "Không tìm thấy chi tiết công việc.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    String errorMsg = "Lỗi " + response.code() + ": Không thể tải chi tiết công việc.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += "\n" + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc errorBody", e);
                        }
                    }
                    Log.e(TAG, "API Error: " + errorMsg);
                    Toast.makeText(JobDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobDetailResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Lỗi mạng khi tải chi tiết công việc: " + t.getMessage(), t);
                Toast.makeText(JobDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String formatSalary(String salaryMinStr, String salaryMaxStr, String salaryPeriodStr) {
        if (TextUtils.isEmpty(salaryMinStr) && TextUtils.isEmpty(salaryMaxStr)) {
            return "Thỏa thuận";
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedMin = "N/A";
        String formattedMax = "N/A";

        try {
            if (!TextUtils.isEmpty(salaryMinStr)) {
                double minSalary = Double.parseDouble(salaryMinStr);
                formattedMin = currencyFormat.format(minSalary);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Không thể định dạng salaryMin: " + salaryMinStr, e);
            formattedMin = salaryMinStr;
        }

        try {
            if (!TextUtils.isEmpty(salaryMaxStr)) {
                double maxSalary = Double.parseDouble(salaryMaxStr);
                formattedMax = currencyFormat.format(maxSalary);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Không thể định dạng salaryMax: " + salaryMaxStr, e);
            formattedMax = salaryMaxStr;
        }

        String salaryRange;
        if (!TextUtils.isEmpty(salaryMinStr) && !TextUtils.isEmpty(salaryMaxStr)) {
            salaryRange = formattedMin + " - " + formattedMax;
        } else if (!TextUtils.isEmpty(salaryMinStr)) {
            salaryRange = "Từ " + formattedMin;
        } else {
            salaryRange = "Đến " + formattedMax;
        }

        String periodDisplay = "";
        if ("MONTH".equalsIgnoreCase(salaryPeriodStr)) {
            periodDisplay = "/ Tháng";
        } else if ("YEAR".equalsIgnoreCase(salaryPeriodStr)) {
            periodDisplay = "/ Năm";
        } else if ("HOUR".equalsIgnoreCase(salaryPeriodStr)) {
            periodDisplay = "/ Giờ";
        }

        return salaryRange + periodDisplay;
    }


    private void populateCommonUi() {
        if (currentJob == null) return;

        Company company = currentJob.getCompany();
        String logoUrl = (company != null && company.getLogoUrl() != null) ? company.getLogoUrl() : "";
        if (!logoUrl.isEmpty() && !logoUrl.toLowerCase().startsWith("http")) {
            String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
            logoUrl = imageBaseUrl + (logoUrl.startsWith("/") ? "" : "/") + logoUrl;
        }

        Glide.with(this)
                .load(logoUrl.isEmpty() ? R.drawable.ic_company_logo_placeholder : logoUrl)
                .placeholder(R.drawable.ic_company_logo_placeholder)
                .error(R.drawable.ic_company_logo_placeholder)
                .into(ivCompanyLogoDetail);

        tvCompanyNameDetail.setText(company != null ? company.getName() : "Không rõ");
        tvJobTitleDetail.setText(currentJob.getTitle());
        tvLocationDetail.setText(currentJob.getLocation());

        String salaryText = formatSalary(currentJob.getSalaryMin(), currentJob.getSalaryMax(), currentJob.getSalaryPeriod());
        tvSalaryDetail.setText(salaryText);

        tvApplicants.setText("- Ứng viên");

        // updateFavoriteButton(currentJob.isFavorite()); // Đã xóa dòng này
        displayTags();
    }

    private void populateDescriptionTab() {
        if (currentJob != null && tvDescription != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvDescription.setText(Html.fromHtml(currentJob.getDescription(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                tvDescription.setText(Html.fromHtml(currentJob.getDescription()));
            }
        }
    }

    private void populateCompanyTab() {
        if (currentJob == null || currentJob.getCompany() == null) {
            if (layoutCompanyContent != null) layoutCompanyContent.setVisibility(View.GONE);
            return;
        }
        Company company = currentJob.getCompany();
        tvCompanyDescription.setText(company.getDescription() != null ? company.getDescription() : "Không có mô tả.");
        tvWebsite.setText(company.getWebsite() != null ? company.getWebsite() : "Chưa cập nhật");
        Linkify.addLinks(tvWebsite, Linkify.WEB_URLS);
        tvIndustry.setText(company.getIndustry() != null ? company.getIndustry() : "Chưa cập nhật");
        tvCompanySize.setText(company.getCompanySize() != null ? company.getCompanySize() : "Chưa cập nhật");
        tvOfficeAddress.setText(company.getAddress() != null ? company.getAddress() : "Chưa cập nhật");
    }

    private void displayTags() {
        if (currentJob == null || layoutTags == null) return;
        layoutTags.removeAllViews();

        List<String> tags = new ArrayList<>();
        if (currentJob.getJobType() != null) {
            tags.add(formatJobType(currentJob.getJobType()));
        }

        if (tags.isEmpty()) {
            layoutTags.setVisibility(View.GONE);
            return;
        }

        layoutTags.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String tagText : tags) {
            TextView tagView = (TextView) inflater.inflate(R.layout.item_tag_template, layoutTags, false);
            tagView.setText(tagText);
            layoutTags.addView(tagView);
        }
    }

    private String formatJobType(String jobTypeApi) {
        if (jobTypeApi == null) return "";
        switch (jobTypeApi.toUpperCase()) {
            case "FULL_TIME": return "Toàn thời gian";
            case "PART_TIME": return "Bán thời gian";
            case "CONTRACT": return "Hợp đồng";
            case "INTERNSHIP": return "Thực tập";
            case "FREELANCE": return "Freelance";
            default: return jobTypeApi;
        }
    }

    // Phương thức updateFavoriteButton đã được xóa

    private void setupClickListeners() {
        if (currentJob == null) return;

        // Logic cho btnFavoriteDetail đã được xóa

        btnApply.setOnClickListener(v -> {
            if (currentAccessToken == null || currentAccessToken.isEmpty()) {
                Toast.makeText(this, "Vui lòng đăng nhập để ứng tuyển.", Toast.LENGTH_SHORT).show();
                // Cân nhắc điều hướng đến LoginActivity nếu cần
                // Intent loginIntent = new Intent(JobDetailActivity.this, LoginActivity.class);
                // startActivity(loginIntent);
                return;
            }
            Intent applyIntent = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
            applyIntent.putExtra(JobAdapter.JOB_DETAIL_KEY, currentJob);
            startActivity(applyIntent);
        });
    }

    private void setupTabLayout() {
        if (currentJob == null || tabLayout == null) return;

        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_description)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_company)));

        showDescriptionContent();
        populateDescriptionTab();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showDescriptionContent();
                    populateDescriptionTab();
                } else if (tab.getPosition() == 1) {
                    showCompanyContent();
                    populateCompanyTab();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void showDescriptionContent() {
        if (layoutDescriptionContent != null) layoutDescriptionContent.setVisibility(View.VISIBLE);
        if (layoutCompanyContent != null) layoutCompanyContent.setVisibility(View.GONE);
    }

    private void showCompanyContent() {
        if (layoutDescriptionContent != null) layoutDescriptionContent.setVisibility(View.GONE);
        if (layoutCompanyContent != null) layoutCompanyContent.setVisibility(View.VISIBLE);
    }
}
