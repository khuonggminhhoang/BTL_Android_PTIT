package com.example.foodorderapp.features.main.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.JobCategory;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.features.jobs.ui.activity.SearchActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.CategoryAdapter;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.User;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.request.SaveJobDto;
import com.example.foodorderapp.network.response.JobCategoryResponse;
import com.example.foodorderapp.network.response.PaginatedJobResponse;
import com.example.foodorderapp.network.response.SavedJobsApiResponse; // Thêm import này

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment implements JobAdapter.OnJobInteractionListener {

    private static final String TAG = "HomeFragment";

    private RecyclerView rvCategories;
    private RecyclerView rvJobs;
    private CategoryAdapter categoryAdapter;
    private JobAdapter jobAdapter;
    private List<JobCategory> categoryList;
    private List<Job> jobList;
    private TextView tvHello;
    private TextView etSearch;
    private NestedScrollView nestedScrollView;
    private ProgressBar pbLoadingJobs, pbLoadingMoreJobs, pbLoadingCategories;

    private ApiService apiService;
    private boolean isLoadingJobs = false;
    private boolean isLoadingCategories = false;
    private boolean isLastPage = false;
    private int currentPage = 1;
    private final int PAGE_SIZE = 5;

    private String currentAccessToken;
    private Set<Integer> savedJobIds;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApiService();
        jobList = new ArrayList<>();
        categoryList = new ArrayList<>();
        savedJobIds = new HashSet<>();

        SharedPreferences prefs = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories = view.findViewById(R.id.rvCategories);
        rvJobs = view.findViewById(R.id.rvJobs);
        tvHello = view.findViewById(R.id.tvHello);
        etSearch = view.findViewById(R.id.etSearch);
        nestedScrollView = view.findViewById(R.id.nestedScrollViewHome);
        pbLoadingJobs = view.findViewById(R.id.pbLoadingJobs);
        pbLoadingMoreJobs = view.findViewById(R.id.pbLoadingMoreJobs);
        pbLoadingCategories = view.findViewById(R.id.pbLoadingCategories);

        setupCategoryRecyclerView();
        setupJobRecyclerView();

        setupSearchListener();
        setupScrollListener();

        loadInitialData();
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            }
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

    private void setupCategoryRecyclerView() {
        if (getContext() != null) {
            categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            rvCategories.setLayoutManager(layoutManager);
            rvCategories.setAdapter(categoryAdapter);
            rvCategories.setHasFixedSize(true);
        }
    }

    private void setupJobRecyclerView() {
        if (getContext() != null) {
            jobAdapter = new JobAdapter(requireContext(), jobList, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
            rvJobs.setLayoutManager(layoutManager);
            rvJobs.setAdapter(jobAdapter);
            rvJobs.setNestedScrollingEnabled(false);
        }
    }

    private void setupSearchListener() {
        etSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupScrollListener() {
        if (nestedScrollView == null) return;

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                Log.d(TAG, "Đã cuộn xuống cuối. Trang hiện tại: " + currentPage + ", Đang tải công việc: " + isLoadingJobs + ", Trang cuối: " + isLastPage);
                if (!isLoadingJobs && !isLastPage) {
                    currentPage++;
                    loadJobs(currentPage);
                }
            }
        });
    }

    private void loadInitialData() {
        jobList.clear();
        currentPage = 1;
        isLastPage = false;
        if (jobAdapter != null) {
            jobAdapter.clearJobs();
        }

        if (currentAccessToken != null && !currentAccessToken.isEmpty()) {
            fetchSavedJobsAndThenLoadPageJobs();
        } else {
            Log.w(TAG, "Access token is null. Loading jobs without favorite status.");
            savedJobIds.clear();
            loadJobs(currentPage);
        }
        loadCategories();
    }

    private void fetchSavedJobsAndThenLoadPageJobs() {
        if (apiService == null || currentAccessToken == null) {
            Log.e(TAG, "ApiService or AccessToken is null. Cannot fetch saved jobs.");
            savedJobIds.clear();
            loadJobs(currentPage);
            return;
        }
        isLoadingJobs = true;
        if (pbLoadingJobs != null) pbLoadingJobs.setVisibility(View.VISIBLE);


        Log.d(TAG, "Fetching saved jobs...");
        Call<SavedJobsApiResponse> callSavedJobs = apiService.getSavedJobs("Bearer " + currentAccessToken); // Thay đổi ở đây
        callSavedJobs.enqueue(new Callback<SavedJobsApiResponse>() { // Thay đổi ở đây
            @Override
            public void onResponse(@NonNull Call<SavedJobsApiResponse> call, @NonNull Response<SavedJobsApiResponse> response) { // Thay đổi ở đây
                isLoadingJobs = false;
                if (pbLoadingJobs != null) pbLoadingJobs.setVisibility(View.GONE);

                if (isAdded() && getContext() != null) {
                    savedJobIds.clear();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) { // Kiểm tra isSuccess()
                        List<Job> fetchedSavedJobs = response.body().getData(); // Lấy data từ SavedJobsApiResponse
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
                            Toast.makeText(getContext(), "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
                            navigateToLogin();
                        }
                    }
                    loadJobs(currentPage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SavedJobsApiResponse> call, @NonNull Throwable t) { // Thay đổi ở đây
                isLoadingJobs = false;
                if (pbLoadingJobs != null) pbLoadingJobs.setVisibility(View.GONE);
                if (isAdded() && getContext() != null) {
                    Log.e(TAG, "Network error fetching saved jobs: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Lỗi mạng khi tải công việc đã lưu.", Toast.LENGTH_SHORT).show();
                    savedJobIds.clear();
                    loadJobs(currentPage);
                }
            }
        });
    }


    private void loadJobs(int page) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Không thể tải công việc.");
            if(getContext() != null) Toast.makeText(getContext(), "Lỗi kết nối dịch vụ.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isLoadingJobs && page > 1) {
            return;
        }
        if (isLastPage && page > 1) return;

        isLoadingJobs = true;
        if (page == 1) {
            if (pbLoadingJobs != null) pbLoadingJobs.setVisibility(View.VISIBLE);
            if (pbLoadingMoreJobs != null) pbLoadingMoreJobs.setVisibility(View.GONE);
        } else {
            if (pbLoadingMoreJobs != null) pbLoadingMoreJobs.setVisibility(View.VISIBLE);
        }

        Call<PaginatedJobResponse> call = apiService.getJobsFiltered(
                page,
                PAGE_SIZE,
                "-createdAt",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        call.enqueue(new Callback<PaginatedJobResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedJobResponse> call, @NonNull Response<PaginatedJobResponse> response) {
                isLoadingJobs = false;
                if (pbLoadingJobs != null) pbLoadingJobs.setVisibility(View.GONE);
                if (pbLoadingMoreJobs != null) pbLoadingMoreJobs.setVisibility(View.GONE);

                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        PaginatedJobResponse paginatedResponse = response.body();
                        List<Job> newJobs = paginatedResponse.getData();

                        if (newJobs != null && !newJobs.isEmpty()) {
                            for (Job job : newJobs) {
                                job.setFavorite(savedJobIds.contains(job.getId()));
                            }
                            if (jobAdapter != null) jobAdapter.addJobs(newJobs);
                        }

                        if (paginatedResponse.getMeta() != null) {
                            Log.d(TAG, "Phản hồi Meta API: Tổng số trang=" + paginatedResponse.getMeta().getTotalPages() + ", Trang hiện tại=" + paginatedResponse.getMeta().getCurrentPage());
                            if (paginatedResponse.getMeta().getCurrentPage() >= paginatedResponse.getMeta().getTotalPages()) {
                                isLastPage = true;
                                Log.d(TAG, "Đã đến trang cuối.");
                            }
                        } else {
                            if (newJobs == null || newJobs.isEmpty()) {
                                isLastPage = true;
                            }
                            Log.w(TAG, "Phản hồi meta từ API là null. Giả sử là trang cuối nếu không có công việc mới.");
                        }
                    } else {
                        String errorBodyString = "";
                        if (response.errorBody() != null) {
                            try {
                                errorBodyString = response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc error body cho công việc", e);
                            }
                        }
                        Log.e(TAG, "Lỗi API khi tải công việc: " + response.code() + " - " + response.message() + " | Error Body: " + errorBodyString);
                        Toast.makeText(getContext(), "Không thể tải danh sách công việc. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        if (currentPage > 1) {
                            currentPage--;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedJobResponse> call, @NonNull Throwable t) {
                isLoadingJobs = false;
                if (pbLoadingJobs != null) pbLoadingJobs.setVisibility(View.GONE);
                if (pbLoadingMoreJobs != null) pbLoadingMoreJobs.setVisibility(View.GONE);
                if (isAdded() && getContext() != null) {
                    Log.e(TAG, "Lỗi mạng khi tải công việc: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Lỗi mạng khi tải công việc: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    if (currentPage > 1) {
                        currentPage--;
                    }
                }
            }
        });
    }

    private void loadCategories() {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Không thể tải danh mục.");
            if(getContext() != null) Toast.makeText(getContext(), "Lỗi kết nối dịch vụ.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isLoadingCategories) {
            return;
        }

        isLoadingCategories = true;
        if (pbLoadingCategories != null) pbLoadingCategories.setVisibility(View.VISIBLE);

        Call<JobCategoryResponse> call = apiService.getJobCategories();
        call.enqueue(new Callback<JobCategoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobCategoryResponse> call, @NonNull Response<JobCategoryResponse> response) {
                isLoadingCategories = false;
                if (pbLoadingCategories != null) pbLoadingCategories.setVisibility(View.GONE);

                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<JobCategory> fetchedCategories = response.body().getData();
                        if (fetchedCategories != null) {
                            Log.d(TAG, "Tải danh mục thành công: " + fetchedCategories.size() + " mục");
                            categoryList.clear();
                            categoryList.addAll(fetchedCategories);
                            if (categoryAdapter != null) categoryAdapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Danh sách danh mục tải về là null.");
                            Toast.makeText(getContext(), "Không có dữ liệu danh mục.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorBodyString = "";
                        if (response.errorBody() != null) {
                            try {
                                errorBodyString = response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc error body cho danh mục", e);
                            }
                        }
                        Log.e(TAG, "Lỗi API khi tải danh mục: " + response.code() + " - " + response.message() + " | Error Body: " + errorBodyString);
                        Toast.makeText(getContext(), "Không thể tải danh mục. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobCategoryResponse> call, @NonNull Throwable t) {
                isLoadingCategories = false;
                if (pbLoadingCategories != null) pbLoadingCategories.setVisibility(View.GONE);
                if (isAdded() && getContext() != null) {
                    Log.e(TAG, "Lỗi mạng khi tải danh mục: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Lỗi mạng khi tải danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentAccessToken != null && !currentAccessToken.isEmpty()) {
            fetchSavedJobsAndUpdateVisibleItems();
        }
    }

    private void fetchSavedJobsAndUpdateVisibleItems() {
        if (apiService == null || currentAccessToken == null || jobAdapter == null || jobList.isEmpty()) {
            return;
        }

        apiService.getSavedJobs("Bearer " + currentAccessToken).enqueue(new Callback<SavedJobsApiResponse>() { // Thay đổi ở đây
            @Override
            public void onResponse(@NonNull Call<SavedJobsApiResponse> call, @NonNull Response<SavedJobsApiResponse> response) { // Thay đổi ở đây
                if (isAdded() && getContext() != null && response.isSuccessful() && response.body() != null && response.body().isSuccess()) { // Kiểm tra isSuccess()
                    Set<Integer> newSavedJobIds = new HashSet<>();
                    List<Job> fetchedSavedJobs = response.body().getData(); // Lấy data từ SavedJobsApiResponse
                    if (fetchedSavedJobs != null) {
                        for (Job savedJob : fetchedSavedJobs) {
                            newSavedJobIds.add(savedJob.getId());
                        }
                    }

                    boolean changed = !savedJobIds.equals(newSavedJobIds);
                    if (changed) {
                        savedJobIds = newSavedJobIds;
                        for (int i = 0; i < jobList.size(); i++) {
                            Job job = jobList.get(i);
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
            public void onFailure(@NonNull Call<SavedJobsApiResponse> call, @NonNull Throwable t) { // Thay đổi ở đây
                Log.e(TAG, "Lỗi khi cập nhật danh sách yêu thích lúc resume: " + t.getMessage());
            }
        });
    }


    @Override
    public void onJobClick(Job job, int position) {
        if (getContext() != null && job != null) {
            Intent intent = new Intent(getContext(), JobDetailActivity.class);
            intent.putExtra(JobAdapter.JOB_DETAIL_KEY, job);
            startActivity(intent);
        }
    }

    @Override
    public void onFavoriteToggle(Job job, int position, boolean isNowFavorite) {
        if (getContext() == null || job == null || apiService == null) return;

        if (currentAccessToken == null || currentAccessToken.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng tính năng này.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        if (isNowFavorite) {
            apiService.saveJob("Bearer " + currentAccessToken, new SaveJobDto(job.getId())).enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (isAdded() && getContext() != null) {
                        if (response.isSuccessful()) {
                            job.setFavorite(true);
                            savedJobIds.add(job.getId());
                            if (jobAdapter != null) jobAdapter.updateJobItem(position, job);
                            Toast.makeText(getContext(), "Đã thêm vào yêu thích!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi thêm yêu thích: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            apiService.unsaveJob("Bearer " + currentAccessToken, job.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (isAdded() && getContext() != null) {
                        if (response.isSuccessful()) {
                            job.setFavorite(false);
                            savedJobIds.remove(job.getId());
                            if (jobAdapter != null) jobAdapter.updateJobItem(position, job);
                            Toast.makeText(getContext(), "Đã xóa khỏi yêu thích!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Lỗi khi xóa yêu thích: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
