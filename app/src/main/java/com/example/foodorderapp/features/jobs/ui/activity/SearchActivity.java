package com.example.foodorderapp.features.jobs.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Retrofit imports
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class SearchActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton btnBack;
    private EditText edtSearchJob;
    private EditText edtLocation;
    private RecyclerView rvRecommended;
    private TextView txtNoResults;
    private ImageView imgNoResults;
    private JobAdapter recommendedJobAdapter;
    private List<Job> recommendedJobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViews();
        setupToolbar();
        setupRecommendedJobs();
        setupListeners();
        edtSearchJob.requestFocus();
        edtSearchJob.postDelayed(() -> {
            edtSearchJob.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(edtSearchJob, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);
    }

    private void findViews() {
        toolbar = findViewById(R.id.search_toolbar);
        btnBack = findViewById(R.id.search_btn_back);
        edtSearchJob = findViewById(R.id.search_edt_job);
        edtLocation = findViewById(R.id.search_edt_location);
        rvRecommended = findViewById(R.id.search_rv_recommended);
        txtNoResults = findViewById(R.id.search_txt_no_results);
        imgNoResults = findViewById(R.id.search_img_no_results);
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_filter) {
                // TODO: Hiển thị dialog hoặc activity lọc
                Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupRecommendedJobs() {
        recommendedJobList = new ArrayList<>();
        recommendedJobAdapter = new JobAdapter(this, recommendedJobList); // Reuse JobAdapter
        rvRecommended.setLayoutManager(new LinearLayoutManager(this));
        rvRecommended.setAdapter(recommendedJobAdapter);
    }

    private void setupListeners() {
        // Xử lý khi nhấn nút search trên bàn phím
        edtSearchJob.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearchJob.getText().toString());
                return true;
            }
            return false;
        });

        // Xử lý khi nhấn nút search trên bàn phím của ô location
        edtLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearchJob.getText().toString());
                return true;
            }
            return false;
        });
    }

    // Retrofit API interface
    public interface JobApiService {
        @GET("/api/v1/jobs")
        Call<JobSearchResponse> searchJobs(
            @Query("search") String search,
            @Query("searchFields") List<String> searchFields,
            @Query("pageSize") Integer pageSize,
            @Query("pageNumber") Integer pageNumber,
            @Query("sort") String sort,
            @Query("location") String location,
            @Query("jobCategoryId") Integer jobCategoryId,
            @Query("salaryGte") Integer salaryGte,
            @Query("salaryLte") Integer salaryLte
        );
    }

    // Gọi API tìm kiếm việc làm
    private void searchJobsFromApi(String search, String location) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JobApiService apiService = retrofit.create(JobApiService.class);

        List<String> searchFields = new ArrayList<>();
        searchFields.add("title");
        searchFields.add("description");

        // Các trường optional, bạn có thể lấy từ UI hoặc truyền null nếu không dùng
        Integer pageSize = null; // ví dụ: 20
        Integer pageNumber = null; // ví dụ: 1
        String sort = null; // ví dụ: "id, -createdAt"
        Integer jobCategoryId = null;
        Integer salaryGte = null;
        Integer salaryLte = null;
        // location đã có từ tham số

        Call<JobSearchResponse> call = apiService.searchJobs(
            search,
            searchFields,
            pageSize,
            pageNumber,
            sort,
            location,
            jobCategoryId,
            salaryGte,
            salaryLte
        );

        call.enqueue(new Callback<JobSearchResponse>() {
            @Override
            public void onResponse(Call<JobSearchResponse> call, Response<JobSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    updateSearchResults(response.body().data);
                } else {
                    updateSearchResults(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<JobSearchResponse> call, Throwable t) {
                updateSearchResults(new ArrayList<>());
            }
        });
    }

    private void performSearch(String query) {
        if (query != null && !query.trim().isEmpty()) {
            String location = edtLocation.getText().toString();
            searchJobsFromApi(query, location);
        } else {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSearchResults(List<Job> results) {
        if (results == null || results.isEmpty()) {
            rvRecommended.setVisibility(View.GONE);
            txtNoResults.setVisibility(View.VISIBLE);
            txtNoResults.setText("Không tìm thấy kết quả");
            if (imgNoResults != null) imgNoResults.setVisibility(View.VISIBLE);
        } else {
            rvRecommended.setVisibility(View.VISIBLE);
            txtNoResults.setVisibility(View.GONE);
            if (imgNoResults != null) imgNoResults.setVisibility(View.GONE);
            recommendedJobList.clear();
            recommendedJobList.addAll(results);
            recommendedJobAdapter.notifyDataSetChanged();
        }
    }

    // Model cho response
    public static class JobSearchResponse {
        public boolean success;
        public int statusCode;
        public String message;
        public String error;
        public List<Job> data;
        public Meta meta;

        public static class Meta {
            public int totalItems;
            public Integer totalPages;
        }
    }
}