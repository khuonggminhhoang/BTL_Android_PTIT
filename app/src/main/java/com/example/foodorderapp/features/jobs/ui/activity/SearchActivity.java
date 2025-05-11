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
import android.widget.SeekBar;
import android.widget.Button;

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

import com.google.android.material.bottomsheet.BottomSheetDialog;

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
                showFilterBottomSheet();
                return true;
            }
            return false;
        });
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_filter, null);
        bottomSheetDialog.setContentView(view);

        // Get references to views
        EditText edtKeyword = view.findViewById(R.id.edt_filter_keyword);
        EditText edtLocation = view.findViewById(R.id.edt_filter_location);
        TextView tvSalaryValue = view.findViewById(R.id.tv_salary_value);
        SeekBar seekbarSalary = view.findViewById(R.id.seekbar_salary);
        Button btnRemote = view.findViewById(R.id.btn_job_type_remote);
        Button btnFreelance = view.findViewById(R.id.btn_job_type_freelance);
        Button btnFulltime = view.findViewById(R.id.btn_job_type_fulltime);
        Button btnIntern = view.findViewById(R.id.btn_job_type_intern);

        tvSalaryValue.setText("500000đ-1000000đ");
        seekbarSalary.setProgress(50);
        seekbarSalary.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minSalary = 0;
                int maxSalary = 100000000;
                int range = maxSalary - minSalary;
                int currentMin = minSalary + (progress * range / 100);
                int currentMax = currentMin + 500000;
                tvSalaryValue.setText(currentMin + "đ - " + currentMax + "đ");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        View tvClearAll = view.findViewById(R.id.tv_clear_all);
        tvClearAll.setOnClickListener(v -> {
            edtKeyword.setText("");
            edtLocation.setText("");
            
            seekbarSalary.setProgress(50);
            tvSalaryValue.setText("500000-1000000");
            
            btnRemote.setBackgroundTintList(getColorStateList(R.color.blue_primary));
            btnFreelance.setBackgroundTintList(getColorStateList(R.color.blue_primary));
            btnFulltime.setBackgroundTintList(getColorStateList(R.color.blue_primary));
            btnIntern.setBackgroundTintList(getColorStateList(R.color.blue_primary));
            
            btnRemote.setTextColor(getColorStateList(android.R.color.white));
            btnFreelance.setTextColor(getColorStateList(android.R.color.white));
            btnFulltime.setTextColor(getColorStateList(android.R.color.white));
            btnIntern.setTextColor(getColorStateList(android.R.color.white));
        });

        // Handle job type button clicks
        View.OnClickListener jobTypeClickListener = v -> {
            Button clickedButton = (Button) v;
            if (clickedButton.getCurrentTextColor() == getColor(android.R.color.white)) {
                // If selected, deselect it
                clickedButton.setBackgroundTintList(getColorStateList(R.color.blue_primary));
                clickedButton.setTextColor(getColorStateList(android.R.color.white));
            } else {
                // If not selected, select it
                clickedButton.setBackgroundTintList(getColorStateList(android.R.color.white));
                clickedButton.setTextColor(getColorStateList(R.color.blue_primary));
            }
        };

        btnRemote.setOnClickListener(jobTypeClickListener);
        btnFreelance.setOnClickListener(jobTypeClickListener);
        btnFulltime.setOnClickListener(jobTypeClickListener);
        btnIntern.setOnClickListener(jobTypeClickListener);

        // Handle apply filter button
        View btnApply = view.findViewById(R.id.btn_apply_filter);
        btnApply.setOnClickListener(v -> {
            // TODO: Get filter data and pass back to SearchActivity
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
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

        Integer pageSize = null;
        Integer pageNumber = null;
        String sort = null;
        Integer jobCategoryId = null;
        Integer salaryGte = null;
        Integer salaryLte = null;

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