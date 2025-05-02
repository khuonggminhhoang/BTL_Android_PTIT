package com.example.foodorderapp.features.jobs.ui.activity;

import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.features.jobs.ui.adapter.SearchHistoryAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.features.jobs.ui.model.SearchHistory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchHistoryAdapter.OnSearchHistoryClickListener {

    private Toolbar toolbar;
    private ImageButton btnBack, btnFilter;
    private EditText edtSearchJob;
    private TextView txtLocation;
    private RecyclerView rvHistory, rvRecommended;
    private SearchHistoryAdapter historyAdapter;
    private JobAdapter recommendedJobAdapter; // Tái sử dụng JobAdapter
    private List<SearchHistory> searchHistoryList;
    private List<Job> recommendedJobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findViews();
        setupToolbar();
        setupSearchHistory();
        setupRecommendedJobs();
        setupListeners();
    }

    private void findViews() {
        toolbar = findViewById(R.id.search_toolbar);
        btnBack = findViewById(R.id.search_btn_back);
        btnFilter = findViewById(R.id.search_btn_filter);
        edtSearchJob = findViewById(R.id.search_edt_job);
        txtLocation = findViewById(R.id.search_txt_location);
        rvHistory = findViewById(R.id.search_rv_history);
        rvRecommended = findViewById(R.id.search_rv_recommended);
    }

    private void setupToolbar() {
        // Không cần setSupportActionBar nếu không dùng menu của toolbar
        btnBack.setOnClickListener(v -> finish());
        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Filter clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement filter functionality
        });
    }

    private void setupSearchHistory() {
        searchHistoryList = new ArrayList<>();
        // TODO: Load search history from SharedPreferences or Database
        // --- Dummy Data ---
        searchHistoryList.add(new SearchHistory("UI/UX Designer", 120));
        searchHistoryList.add(new SearchHistory("Front End Developer", 90));
        searchHistoryList.add(new SearchHistory("Graphic Designer", 10));
        // --- End Dummy Data ---

        historyAdapter = new SearchHistoryAdapter(this, searchHistoryList, this); // Truyền this (Activity) làm listener
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupRecommendedJobs() {
        recommendedJobList = new ArrayList<>();
        // TODO: Load recommended jobs based on user profile/history or default
        // --- Dummy Data (Lấy từ dữ liệu mẫu bạn đã có) ---
        String defaultDescription = "Building new user-facing features...";
        String twitterCompanyInfo = "Twitter Indonesia is a solution...";
        String defaultAddress = "Jl. Muara Baru Ujung Blok T...";
        recommendedJobList.add(new Job("Slack", "Remote Front End Developer", "Bandung-Indonesia", "$15K - $30K / Month", "2 hours ago", R.drawable.ic_company_logo_placeholder, false, defaultDescription, "Slack company info...", 50, Arrays.asList("Frontend", "Remote"), "www.slack.com", "Collaboration", "501-1000 employee", "Bandung"));
        recommendedJobList.add(new Job("Facebook", "Remote UX Designer", "Surabaya-Indonesia", "$10K - $25K / Month", "1 day ago", R.drawable.ic_company_logo_placeholder, true, defaultDescription, twitterCompanyInfo, 150, Arrays.asList("UI/UX", "Remote", "Design"), "www.facebook.com", "Social Network", "10000+ employee", defaultAddress));
        // --- End Dummy Data ---

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

        // Xử lý khi nhấn vào ô Location
        txtLocation.setOnClickListener(v -> {
            Toast.makeText(this, "Select location clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement location selection (e.g., open map, use current location)
        });
    }

    // Xử lý khi click vào item trong lịch sử tìm kiếm
    @Override
    public void onHistoryItemClick(SearchHistory item) {
        edtSearchJob.setText(item.getTerm());
        performSearch(item.getTerm());
    }

    // Hàm thực hiện tìm kiếm (hiện tại chỉ hiển thị Toast)
    private void performSearch(String query) {
        if (query != null && !query.trim().isEmpty()) {
            Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
            // TODO: Implement actual search logic (e.g., start SearchResultsActivity with query)
            // Ví dụ:
            // Intent intent = new Intent(this, SearchResultsActivity.class);
            // intent.putExtra("SEARCH_QUERY", query);
            // startActivity(intent);
        } else {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
        }
        // Có thể ẩn bàn phím ở đây nếu cần
    }
}