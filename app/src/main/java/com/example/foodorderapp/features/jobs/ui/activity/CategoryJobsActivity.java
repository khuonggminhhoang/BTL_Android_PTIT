package com.example.foodorderapp.features.jobs.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Import Nullable
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;


import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;
import com.example.foodorderapp.features.jobs.ui.adapter.TopCompanyAdapter;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.CompaniesApiResponse;
import com.example.foodorderapp.network.response.PaginatedJobResponse;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CategoryJobsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "CATEGORY_ID";
    public static final String EXTRA_CATEGORY_DISPLAY_NAME = "CATEGORY_DISPLAY_NAME";
    private static final String TAG = "CategoryJobsActivity";

    private TextView tvToolbarTitle;
    private ImageButton btnBack, btnFilter;
    private EditText etSearchCategory;
    private RecyclerView rvCategoryJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobListFromApi;
    private String categoryDisplayName;
    private int categoryId = -1;

    private RecyclerView rvTopCompanies;
    private TopCompanyAdapter topCompanyAdapter;
    private List<Company> topCompanyList;
    private ProgressBar pbLoadingTopCompanies, pbLoadingCategoryJobs;
    private ApiService apiService;

    private int currentPage = 1;
    // SỬA LỖI: Thay đổi PAGE_SIZE thành 5 hoặc giá trị nhỏ hơn theo yêu cầu của API
    private final int PAGE_SIZE = 5; // Số lượng công việc mỗi lần tải
    private boolean isLoadingJobs = false;
    private boolean isLastPageJobs = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_jobs);

        categoryId = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);
        categoryDisplayName = getIntent().getStringExtra(EXTRA_CATEGORY_DISPLAY_NAME);

        if (categoryDisplayName == null || categoryDisplayName.isEmpty()) {
            Toast.makeText(this, "Không nhận được thông tin tên danh mục hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initApiService();
        findViews();
        tvToolbarTitle.setText(categoryDisplayName);

        setupTopCompaniesRecyclerView();
        fetchTopCompanies();

        setupJobsRecyclerView();
        fetchJobsForCategory(categoryId, currentPage, null);

        setupClickListeners();
        setupSearchListener();
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
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

    private void findViews() {
        tvToolbarTitle = findViewById(R.id.toolbar_title_category);
        btnBack = findViewById(R.id.btnBackCategory);
        btnFilter = findViewById(R.id.btnFilterCategory);
        etSearchCategory = findViewById(R.id.etSearchCategory);
        rvCategoryJobs = findViewById(R.id.rvCategoryJobs);
        rvTopCompanies = findViewById(R.id.rvTopCompanies);
        pbLoadingTopCompanies = findViewById(R.id.pbLoadingTopCompanies);
        pbLoadingCategoryJobs = findViewById(R.id.pbLoadingCategoryJobs);
    }

    private void setupTopCompaniesRecyclerView() {
        topCompanyList = new ArrayList<>();
        topCompanyAdapter = new TopCompanyAdapter(this, topCompanyList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTopCompanies.setLayoutManager(layoutManager);
        rvTopCompanies.setAdapter(topCompanyAdapter);
        rvTopCompanies.setHasFixedSize(true);
    }

    private void fetchTopCompanies() {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
            if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.GONE);
            return;
        }
        if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.VISIBLE);
        Log.d(TAG, "Fetching top companies...");

        Call<CompaniesApiResponse> call = apiService.getTopCompanies();
        call.enqueue(new Callback<CompaniesApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompaniesApiResponse> call, @NonNull Response<CompaniesApiResponse> response) {
                if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Company> fetchedCompanies = response.body().getData();
                    if (fetchedCompanies != null && !fetchedCompanies.isEmpty()) {
                        Log.d(TAG, "Top companies fetched: " + fetchedCompanies.size());
                        topCompanyAdapter.updateData(fetchedCompanies);
                    } else {
                        Log.d(TAG, "No top companies found or data is null.");
                    }
                } else {
                    String errorMsg = "Lỗi " + response.code() + ": Không thể tải danh sách công ty hàng đầu.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += "\n" + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc errorBody cho top companies", e);
                        }
                    }
                    Log.e(TAG, "API Error (Top Companies): " + errorMsg);
                    Toast.makeText(CategoryJobsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompaniesApiResponse> call, @NonNull Throwable t) {
                if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Lỗi mạng khi tải top companies: " + t.getMessage(), t);
                Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupJobsRecyclerView() {
        jobListFromApi = new ArrayList<>();
        jobAdapter = new JobAdapter(this, jobListFromApi);
        rvCategoryJobs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryJobs.setAdapter(jobAdapter);
        rvCategoryJobs.setNestedScrollingEnabled(false);
    }

    private void fetchJobsForCategory(int categoryIdToFetch, int page, @Nullable String searchQuery) {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
            if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
            isLoadingJobs = false;
            return;
        }
        if (isLoadingJobs) return;

        isLoadingJobs = true;
        if (page == 1 && pbLoadingCategoryJobs != null) {
            pbLoadingCategoryJobs.setVisibility(View.VISIBLE);
            if (jobAdapter != null) jobAdapter.clearJobs();
        }

        Integer categoryIdParam = (categoryIdToFetch <= 0) ? null : categoryIdToFetch;

        Call<PaginatedJobResponse> call = apiService.getJobsFiltered(
                page,
                PAGE_SIZE, // Giá trị này đã được sửa thành 5
                "-createdAt",
                categoryIdParam,
                null,
                searchQuery,
                null,
                null,
                null
        );

        Log.d(TAG, "Fetching jobs for category ID: " + categoryIdParam + ", Page: " + page + ", Search: " + (searchQuery != null ? searchQuery : "N/A") + ", PageSize: " + PAGE_SIZE);

        call.enqueue(new Callback<PaginatedJobResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedJobResponse> call, @NonNull Response<PaginatedJobResponse> response) {
                isLoadingJobs = false;
                if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Job> fetchedJobs = response.body().getData();
                    if (fetchedJobs != null && !fetchedJobs.isEmpty()) {
                        if (page == 1) {
                            jobAdapter.updateJobList(fetchedJobs);
                        } else {
                            jobAdapter.addJobs(fetchedJobs);
                        }
                        Log.d(TAG, "Jobs fetched for category " + categoryIdParam + ": " + fetchedJobs.size());
                        isLastPageJobs = (fetchedJobs.size() < PAGE_SIZE);
                    } else {
                        if (page == 1) {
                            jobAdapter.clearJobs();
                            Toast.makeText(CategoryJobsActivity.this, "Không tìm thấy công việc nào.", Toast.LENGTH_SHORT).show();
                        }
                        isLastPageJobs = true;
                        Log.d(TAG, "No jobs found for category " + categoryIdParam + " on page " + page);
                    }
                } else {
                    String errorMsg = "Lỗi tải công việc. Mã lỗi: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc errorBody cho jobs", e);
                        }
                    }
                    Toast.makeText(CategoryJobsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "API Error (Jobs for category): " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedJobResponse> call, @NonNull Throwable t) {
                isLoadingJobs = false;
                if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Lỗi mạng khi tải công việc cho danh mục: " + t.getMessage(), t);
                Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Filter chưa được cài đặt", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearchListener() {
        etSearchCategory.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchQuery = etSearchCategory.getText().toString().trim();
                currentPage = 1;
                isLastPageJobs = false;
                fetchJobsForCategory(categoryId, currentPage, searchQuery.isEmpty() ? null : searchQuery);
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }
}
