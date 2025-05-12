package com.example.foodorderapp.features.jobs.ui.activity;

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
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;

import java.util.ArrayList;
import java.util.List;

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

        LinearLayout layoutJobTypes = view.findViewById(R.id.layout_job_types);
        List<Integer> selectedJobTypeIds = new ArrayList<>();
        List<Button> jobTypeButtons = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JobCategoryApiService apiService = retrofit.create(JobCategoryApiService.class);
        apiService.getJobCategories().enqueue(new Callback<JobCategoryResponse>() {
            @Override
            public void onResponse(Call<JobCategoryResponse> call, Response<JobCategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    layoutJobTypes.removeAllViews();
                    jobTypeButtons.clear();
                    for (com.example.foodorderapp.core.model.JobCategory category : response.body().data) {
                        Button btn = new Button(SearchActivity.this);
                        btn.setText(category.getName());
                        btn.setTag(category.getId());
                        btn.setBackgroundTintList(getColorStateList(R.color.blue_primary));
                        btn.setTextColor(getColorStateList(android.R.color.white));
                        btn.setOnClickListener(v -> {
                            // Bỏ chọn tất cả các button động
                            for (Button b : jobTypeButtons) {
                                b.setSelected(false);
                                b.setBackgroundTintList(getColorStateList(R.color.blue_primary));
                                b.setTextColor(getColorStateList(android.R.color.white));
                            }
                            // Chọn lại nút vừa click
                            btn.setBackgroundTintList(getColorStateList(android.R.color.white));
                            btn.setTextColor(getColorStateList(R.color.blue_primary));
                            btn.setSelected(true);
                        });
                        layoutJobTypes.addView(btn);
                        jobTypeButtons.add(btn);
                    }
                }
            }
            @Override
            public void onFailure(Call<JobCategoryResponse> call, Throwable t) {
                // pass
            }
        });

        EditText edtKeyword = view.findViewById(R.id.edt_filter_keyword);
        EditText edtLocation = view.findViewById(R.id.edt_filter_location);
        edtKeyword.setText(edtSearchJob.getText().toString());
        edtLocation.setText(this.edtLocation.getText().toString());
        TextView tvSalaryValue = view.findViewById(R.id.tv_salary_value);
        SeekBar seekbarSalary = view.findViewById(R.id.seekbar_salary);

        tvSalaryValue.setText("0đ - 10.000.000đ");
        seekbarSalary.setMax(10);
        seekbarSalary.setProgress(1);

        seekbarSalary.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minSalary = 0;
                int step = 10000000;
                int salaryGte = minSalary + (progress * step);
                int salaryLte = salaryGte + step;
                if (salaryLte > 100000000) salaryLte = 100000000;
                tvSalaryValue.setText(String.format("%sđ - %sđ", formatCurrency(salaryGte), formatCurrency(salaryLte)));
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
            
            seekbarSalary.setProgress(1);
            tvSalaryValue.setText("0đ - 10.000.000đ");
            
            // Reset job type buttons
            for (Button btn : jobTypeButtons) {
                btn.setSelected(false);
                btn.setBackgroundTintList(getColorStateList(R.color.blue_primary));
                btn.setTextColor(getColorStateList(android.R.color.white));
            }
        });

        View btnApply = view.findViewById(R.id.btn_apply_filter);
        btnApply.setOnClickListener(v -> {
            selectedJobTypeIds.clear();
            for (Button btn : jobTypeButtons) {
                if (btn.isSelected()) {
                    selectedJobTypeIds.add((Integer) btn.getTag());
                }
            }
            String keyword = edtKeyword.getText().toString();
            String location = edtLocation.getText().toString();

            edtSearchJob.setText(keyword);
            this.edtLocation.setText(location);

            int progress = seekbarSalary.getProgress();
            int minSalary = 0;
            int step = 10000000;
            int salaryGte = minSalary + (progress * step);
            int salaryLte = salaryGte + step;
            if (salaryLte > 100000000) salaryLte = 100000000;

            Integer jobCategoryId = selectedJobTypeIds.isEmpty() ? null : selectedJobTypeIds.get(0);

            searchJobsFromApi(keyword, location, jobCategoryId, salaryGte, salaryLte);

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
        edtSearchJob.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearchJob.getText().toString());
                return true;
            }
            return false;
        });

        edtLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(edtSearchJob.getText().toString());
                return true;
            }
            return false;
        });
    }

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

    private void searchJobsFromApi(String search, String location, Integer jobCategoryId, Integer salaryGte, Integer salaryLte) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.BE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JobApiService apiService = retrofit.create(JobApiService.class);

        List<String> searchFields = new ArrayList<>();
        searchFields.add("title");
        searchFields.add("description");

        Integer pageSize = 20;
        Integer pageNumber = 1;
        String sort = "id, -createdAt";

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

    // Thêm response model cho category
    public static class JobCategoryResponse {
        public boolean success;
        public int statusCode;
        public String message;
        public String error;
        public List<com.example.foodorderapp.core.model.JobCategory> data;
    }

    public interface JobCategoryApiService {
        @GET("/api/v1/job-categories")
        Call<JobCategoryResponse> getJobCategories();
    }

    private String formatCurrency(int amount) {
        if (amount >= 1000000)
            return String.format("%,d", amount).replace(",", ".");
        else
            return String.valueOf(amount);
    }
}