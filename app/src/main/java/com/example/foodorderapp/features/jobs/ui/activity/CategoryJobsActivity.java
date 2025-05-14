package com.example.foodorderapp.features.jobs.ui.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.foodorderapp.core.model.User;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;
import com.example.foodorderapp.features.jobs.ui.adapter.TopCompanyAdapter;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.request.SaveJobDto;
import com.example.foodorderapp.network.response.CompaniesApiResponse;
import com.example.foodorderapp.network.response.PaginatedJobResponse;
import com.example.foodorderapp.network.response.SavedJobsApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CategoryJobsActivity extends AppCompatActivity implements JobAdapter.OnJobInteractionListener {

    public static final String EXTRA_CATEGORY_ID = "CATEGORY_ID";
    public static final String EXTRA_CATEGORY_DISPLAY_NAME = "CATEGORY_DISPLAY_NAME";
    private static final String TAG = "CategoryJobsActivity";

    private TextView tvToolbarTitle;
    private ImageButton btnBack;
    private EditText etSearchCategory;
    private RecyclerView rvCategoryJobs;
    private JobAdapter jobAdapter;
    private String categoryDisplayName;
    private int categoryId = -1;

    private RecyclerView rvTopCompanies;
    private TopCompanyAdapter topCompanyAdapter;
    private ProgressBar pbLoadingTopCompanies, pbLoadingCategoryJobs;
    private ApiService apiService;

    private int currentPage = 1;
    private final int PAGE_SIZE = 5;
    private boolean isLoadingJobs = false;
    private boolean isLastPageJobs = false;

    private String currentAccessToken;
    private Set<Integer> savedJobIds;
    private List<Job> currentJobList;


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

        savedJobIds = new HashSet<>();
        currentJobList = new ArrayList<>();

        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);

        setupTopCompaniesRecyclerView();
        fetchTopCompanies();

        setupJobsRecyclerView();
        loadInitialJobs();

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
        etSearchCategory = findViewById(R.id.etSearchCategory);
        rvCategoryJobs = findViewById(R.id.rvCategoryJobs);
        rvTopCompanies = findViewById(R.id.rvTopCompanies);
        pbLoadingTopCompanies = findViewById(R.id.pbLoadingTopCompanies);
        pbLoadingCategoryJobs = findViewById(R.id.pbLoadingCategoryJobs);
    }

    private void setupTopCompaniesRecyclerView() {
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
        jobAdapter = new JobAdapter(this, currentJobList, this);
        rvCategoryJobs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryJobs.setAdapter(jobAdapter);
        rvCategoryJobs.setNestedScrollingEnabled(false);
    }

    private void loadInitialJobs() {
        currentJobList.clear();
        currentPage = 1;
        isLastPageJobs = false;
        jobAdapter.clearJobs();

        if (currentAccessToken != null && !currentAccessToken.isEmpty()) {
            fetchSavedJobsAndThenLoadPageJobs(etSearchCategory.getText().toString().trim());
        } else {
            Log.w(TAG, "Access token is null. Loading jobs without favorite status.");
            savedJobIds.clear();
            fetchJobsForCategory(categoryId, currentPage, etSearchCategory.getText().toString().trim());
        }
    }

    private void fetchSavedJobsAndThenLoadPageJobs(@Nullable String searchQuery) {
        if (apiService == null || currentAccessToken == null) {
            Log.e(TAG, "ApiService or AccessToken is null. Cannot fetch saved jobs.");
            savedJobIds.clear();
            fetchJobsForCategory(categoryId, currentPage, searchQuery);
            return;
        }
        isLoadingJobs = true;
        if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.VISIBLE);

        Log.d(TAG, "Fetching saved jobs...");
        Call<SavedJobsApiResponse> callSavedJobs = apiService.getSavedJobs("Bearer " + currentAccessToken);
        callSavedJobs.enqueue(new Callback<SavedJobsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SavedJobsApiResponse> call, @NonNull Response<SavedJobsApiResponse> response) {
                isLoadingJobs = false; // Sẽ set lại khi fetchJobsForCategory bắt đầu
                if (pbLoadingCategoryJobs != null && currentPage == 1) pbLoadingCategoryJobs.setVisibility(View.GONE);

                if (isFinishing() || isDestroyed()) return;

                savedJobIds.clear();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Job> fetchedSavedJobs = response.body().getData();
                    if (fetchedSavedJobs != null) {
                        Log.d(TAG, "Saved jobs fetched successfully: " + fetchedSavedJobs.size() + " items.");
                        for (Job savedJob : fetchedSavedJobs) {
                            savedJobIds.add(savedJob.getId());
                        }
                    } else {
                        Log.w(TAG, "Fetched saved jobs list is null from response data.");
                    }
                } else {
                    Log.e(TAG, "Failed to fetch saved jobs. Code: " + response.code());
                    if (response.code() == 401) {
                        Toast.makeText(CategoryJobsActivity.this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    }
                }
                fetchJobsForCategory(categoryId, currentPage, searchQuery);
            }

            @Override
            public void onFailure(@NonNull Call<SavedJobsApiResponse> call, @NonNull Throwable t) {
                isLoadingJobs = false;
                if (pbLoadingCategoryJobs != null && currentPage == 1) pbLoadingCategoryJobs.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;

                Log.e(TAG, "Network error fetching saved jobs: " + t.getMessage(), t);
                Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng khi tải công việc đã lưu.", Toast.LENGTH_SHORT).show();
                savedJobIds.clear();
                fetchJobsForCategory(categoryId, currentPage, searchQuery);
            }
        });
    }


    private void fetchJobsForCategory(int categoryIdToFetch, int page, @Nullable String searchQuery) {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
            if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
            isLoadingJobs = false;
            return;
        }
        if (isLoadingJobs && page > 1) return;
        if (isLastPageJobs && page > 1) return;


        isLoadingJobs = true;
        if (page == 1) {
            if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.VISIBLE);
            // jobAdapter.clearJobs(); // Không clear ở đây nữa vì currentJobList đã được clear trong loadInitialJobs
            // isLastPageJobs = false;
        } else {
            // Có thể thêm ProgressBar ở cuối RecyclerView nếu muốn
        }

        Integer categoryIdParam = (categoryIdToFetch <= 0) ? null : categoryIdToFetch;
        List<String> searchFields = null;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            searchFields = Arrays.asList("title", "description");
        }

        Call<PaginatedJobResponse> call = apiService.getJobsFiltered(
                page,
                PAGE_SIZE,
                "-createdAt",
                categoryIdParam,
                null,
                searchQuery,
                null,
                null,
                null,
                searchFields
        );

        Log.d(TAG, "Fetching jobs for category ID: " + categoryIdParam + ", Page: " + page + ", Search: " + (searchQuery != null ? searchQuery : "N/A"));

        call.enqueue(new Callback<PaginatedJobResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedJobResponse> call, @NonNull Response<PaginatedJobResponse> response) {
                isLoadingJobs = false;
                if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Job> fetchedJobs = response.body().getData();
                    if (fetchedJobs != null && !fetchedJobs.isEmpty()) {
                        for (Job job : fetchedJobs) {
                            job.setFavorite(savedJobIds.contains(job.getId()));
                        }
                        if (page == 1) {
                            currentJobList.clear(); // Xóa dữ liệu cũ trước khi thêm mới
                            currentJobList.addAll(fetchedJobs);
                            jobAdapter.updateJobList(new ArrayList<>(currentJobList)); // Tạo list mới để adapter nhận diện thay đổi
                        } else {
                            int oldSize = currentJobList.size();
                            currentJobList.addAll(fetchedJobs);
                            jobAdapter.addJobs(fetchedJobs); // addJobs sẽ notifyItemRangeInserted
                        }
                        Log.d(TAG, "Jobs fetched for category " + categoryIdParam + ": " + fetchedJobs.size());
                        isLastPageJobs = fetchedJobs.size() < PAGE_SIZE;
                        if (isLastPageJobs && page > 1) {
                            Toast.makeText(CategoryJobsActivity.this, "Đã tải hết công việc.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (page == 1) {
                            currentJobList.clear();
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
                    if (page > 1) currentPage--;
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedJobResponse> call, @NonNull Throwable t) {
                isLoadingJobs = false;
                if (pbLoadingCategoryJobs != null) pbLoadingCategoryJobs.setVisibility(View.GONE);
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Lỗi mạng khi tải công việc cho danh mục: " + t.getMessage(), t);
                Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                if (page > 1) currentPage--;
            }
        });
    }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearchListener() {
        etSearchCategory.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String searchQuery = etSearchCategory.getText().toString().trim();
                currentPage = 1;
                isLastPageJobs = false; // Reset cờ trang cuối khi tìm kiếm mới
                currentJobList.clear(); // Xóa danh sách hiện tại
                jobAdapter.clearJobs(); // Thông báo cho adapter
                fetchSavedJobsAndThenLoadPageJobs(searchQuery.isEmpty() ? null : searchQuery);

                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                etSearchCategory.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentAccessToken != null && !currentAccessToken.isEmpty()) {
            fetchSavedJobsAndUpdateVisibleItems();
        }
    }

    private void fetchSavedJobsAndUpdateVisibleItems() {
        if (apiService == null || currentAccessToken == null || jobAdapter == null || currentJobList.isEmpty()) {
            return;
        }

        apiService.getSavedJobs("Bearer " + currentAccessToken).enqueue(new Callback<SavedJobsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SavedJobsApiResponse> call, @NonNull Response<SavedJobsApiResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Set<Integer> newSavedJobIds = new HashSet<>();
                    List<Job> fetchedSavedJobs = response.body().getData();
                    if (fetchedSavedJobs != null) {
                        for (Job savedJob : fetchedSavedJobs) {
                            newSavedJobIds.add(savedJob.getId());
                        }
                    }

                    boolean changed = !savedJobIds.equals(newSavedJobIds);
                    if (changed) {
                        savedJobIds = newSavedJobIds;
                        for (int i = 0; i < currentJobList.size(); i++) {
                            Job job = currentJobList.get(i);
                            boolean oldFavoriteStatus = job.isFavorite();
                            boolean newFavoriteStatus = savedJobIds.contains(job.getId());
                            if (oldFavoriteStatus != newFavoriteStatus) {
                                job.setFavorite(newFavoriteStatus);
                                jobAdapter.notifyItemChanged(i);
                            }
                        }
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<SavedJobsApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi khi cập nhật danh sách yêu thích lúc resume: " + t.getMessage());
            }
        });
    }

    @Override
    public void onJobClick(Job job, int position) {
        if (job != null) {
            Intent intent = new Intent(this, JobDetailActivity.class);
            intent.putExtra(JobAdapter.JOB_DETAIL_KEY, job);
            startActivity(intent);
        }
    }

    @Override
    public void onFavoriteToggle(Job job, int position, boolean isNowFavorite) {
        if (job == null || apiService == null) return;

        if (currentAccessToken == null || currentAccessToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        if (isNowFavorite) {
            apiService.saveJob("Bearer " + currentAccessToken, new SaveJobDto(job.getId())).enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful()) {
                        job.setFavorite(true);
                        savedJobIds.add(job.getId());
                        jobAdapter.updateJobItem(position, job);
                        Toast.makeText(CategoryJobsActivity.this, "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CategoryJobsActivity.this, "Lỗi khi thêm yêu thích: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.unsaveJob("Bearer " + currentAccessToken, job.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (isFinishing() || isDestroyed()) return;
                    if (response.isSuccessful()) {
                        job.setFavorite(false);
                        savedJobIds.remove(job.getId());
                        jobAdapter.updateJobItem(position, job);
                        Toast.makeText(CategoryJobsActivity.this, "Đã xóa khỏi yêu thích!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CategoryJobsActivity.this, "Lỗi khi xóa yêu thích: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
