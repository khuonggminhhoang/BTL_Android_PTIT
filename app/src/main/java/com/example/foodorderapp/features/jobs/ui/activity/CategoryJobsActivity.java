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
import java.util.Arrays; // <<< THÊM IMPORT NÀY
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
    private ImageButton btnBack;
    // private ImageButton btnFilter; // Bỏ btnFilter nếu không dùng
    private EditText etSearchCategory;
    private RecyclerView rvCategoryJobs;
    private JobAdapter jobAdapter;
    // private List<Job> jobListFromApi; // Không cần thiết nếu jobAdapter quản lý list
    private String categoryDisplayName;
    private int categoryId = -1;

    private RecyclerView rvTopCompanies;
    private TopCompanyAdapter topCompanyAdapter;
    // private List<Company> topCompanyList; // Không cần thiết nếu topCompanyAdapter quản lý list
    private ProgressBar pbLoadingTopCompanies, pbLoadingCategoryJobs;
    private ApiService apiService;

    private int currentPage = 1;
    private final int PAGE_SIZE = 5;
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
        // Tải lần đầu không có searchQuery, chỉ theo categoryId
        fetchJobsForCategory(categoryId, currentPage, null);

        setupClickListeners();
        setupSearchListener();
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            // Cân nhắc finish() activity nếu không có URL
            // finish();
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
        // btnFilter = findViewById(R.id.btnFilterCategory); // Bỏ nếu không dùng
        etSearchCategory = findViewById(R.id.etSearchCategory);
        rvCategoryJobs = findViewById(R.id.rvCategoryJobs);
        rvTopCompanies = findViewById(R.id.rvTopCompanies);
        pbLoadingTopCompanies = findViewById(R.id.pbLoadingTopCompanies);
        pbLoadingCategoryJobs = findViewById(R.id.pbLoadingCategoryJobs);
    }

    private void setupTopCompaniesRecyclerView() {
        // Khởi tạo list rỗng cho adapter
        topCompanyAdapter = new TopCompanyAdapter(this, new ArrayList<>());
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
                        // Có thể hiển thị thông báo "Không có công ty hàng đầu" nếu cần
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
        // Khởi tạo list rỗng cho adapter
        jobAdapter = new JobAdapter(this, new ArrayList<>());
        rvCategoryJobs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryJobs.setAdapter(jobAdapter);
        rvCategoryJobs.setNestedScrollingEnabled(false); // Quan trọng khi RecyclerView nằm trong NestedScrollView
    }

    private void fetchJobsForCategory(int categoryIdToFetch, int page, @Nullable String searchQuery) {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
            if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
            isLoadingJobs = false;
            return;
        }
        if (isLoadingJobs && page > 1) return; // Chỉ cho phép một yêu cầu tải thêm tại một thời điểm

        isLoadingJobs = true;
        if (page == 1) { // Nếu là trang đầu tiên (tải mới hoặc tìm kiếm mới)
            if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.VISIBLE);
            if (jobAdapter != null) jobAdapter.clearJobs(); // Xóa dữ liệu cũ
            isLastPageJobs = false; // Reset cờ trang cuối
        } else { // Nếu là tải thêm (load more)
            // Có thể thêm ProgressBar ở cuối RecyclerView nếu muốn
        }

        Integer categoryIdParam = (categoryIdToFetch <= 0) ? null : categoryIdToFetch;
        List<String> searchFields = null;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchFields = Arrays.asList("title", "description"); // Tìm kiếm trong tiêu đề và mô tả
        }


        Call<PaginatedJobResponse> call = apiService.getJobsFiltered(
                page,
                PAGE_SIZE,
                "-createdAt", // Sắp xếp theo ngày tạo mới nhất
                categoryIdParam,
                null, // location - không lọc theo location ở màn này
                searchQuery, // Từ khóa tìm kiếm (tên công việc)
                null, // salaryGte
                null, // salaryLte
                null,  // isTopJob
                searchFields // <<< THÊM searchFields
        );

        Log.d(TAG, "Fetching jobs for category ID: " + categoryIdParam + ", Page: " + page + ", Search: " + (searchQuery != null ? searchQuery : "N/A") + ", PageSize: " + PAGE_SIZE + ", SearchFields: " + (searchFields != null ? searchFields.toString() : "N/A"));

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
                        // Cập nhật isLastPageJobs dựa trên số lượng item trả về
                        isLastPageJobs = fetchedJobs.size() < PAGE_SIZE;
                        if (isLastPageJobs && page > 1) {
                            Toast.makeText(CategoryJobsActivity.this, "Đã tải hết công việc.", Toast.LENGTH_SHORT).show();
                        }
                    } else { // Không có công việc nào được tìm thấy
                        if (page == 1) { // Nếu là trang đầu tiên và không có kết quả
                            jobAdapter.clearJobs(); // Đảm bảo RecyclerView trống
                            Toast.makeText(CategoryJobsActivity.this, "Không tìm thấy công việc nào.", Toast.LENGTH_SHORT).show();
                        }
                        isLastPageJobs = true; // Đánh dấu là trang cuối
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
                    if (page > 1) currentPage--; // Giảm trang hiện tại nếu tải thêm thất bại
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedJobResponse> call, @NonNull Throwable t) {
                isLoadingJobs = false;
                if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Lỗi mạng khi tải công việc cho danh mục: " + t.getMessage(), t);
                Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (page > 1) currentPage--; // Giảm trang hiện tại nếu tải thêm thất bại
            }
        });
    }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        // btnFilter.setOnClickListener(v -> { // Bỏ nếu không dùng
        //     Toast.makeText(this, "Chức năng Filter chưa được cài đặt", Toast.LENGTH_SHORT).show();
        // });
    }

    private void setupSearchListener() {
        etSearchCategory.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchQuery = etSearchCategory.getText().toString().trim();
                currentPage = 1; // Reset về trang đầu tiên khi tìm kiếm mới
                // isLastPageJobs đã được reset trong fetchJobsForCategory khi page = 1
                fetchJobsForCategory(categoryId, currentPage, searchQuery.isEmpty() ? null : searchQuery);

                // Ẩn bàn phím
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                etSearchCategory.clearFocus(); // Bỏ focus khỏi EditText
                return true;
            }
            return false;
        });
    }

    // TODO: Cân nhắc thêm logic tải thêm khi cuộn xuống cuối RecyclerView cho rvCategoryJobs
    // nếu danh sách công việc có thể rất dài.
}
