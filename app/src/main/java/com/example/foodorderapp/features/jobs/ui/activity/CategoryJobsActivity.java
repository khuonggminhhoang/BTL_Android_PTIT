package com.example.foodorderapp.features.jobs.ui.activity;

import androidx.annotation.NonNull; // <<< ĐÃ THÊM IMPORT NÀY
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log; // <<< ĐÃ THÊM IMPORT NÀY
import android.view.View; // <<< ĐÃ THÊM IMPORT NÀY
import android.widget.ImageButton;
import android.widget.ProgressBar; // <<< ĐÃ THÊM IMPORT NÀY
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config; // <<< ĐÃ THÊM IMPORT NÀY
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;
import com.example.foodorderapp.features.jobs.ui.adapter.TopCompanyAdapter; // <<< ĐÃ THÊM IMPORT NÀY
import com.example.foodorderapp.network.ApiService; // <<< ĐÃ THÊM IMPORT NÀY
import com.example.foodorderapp.network.response.CompaniesApiResponse; // <<< ĐÃ THÊM IMPORT NÀY


import java.io.IOException; // <<< ĐÃ THÊM IMPORT NÀY
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call; // <<< ĐÃ THÊM IMPORT NÀY
import retrofit2.Callback; // <<< ĐÃ THÊM IMPORT NÀY
import retrofit2.Response; // <<< ĐÃ THÊM IMPORT NÀY
import retrofit2.Retrofit; // <<< ĐÃ THÊM IMPORT NÀY
import retrofit2.converter.gson.GsonConverterFactory; // <<< ĐÃ THÊM IMPORT NÀY

public class CategoryJobsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "CATEGORY_NAME";
    private static final String TAG = "CategoryJobsActivity"; // <<< ĐÃ THÊM TAG

    private TextView tvToolbarTitle;
    private ImageButton btnBack, btnFilter;
    private RecyclerView rvCategoryJobs;
    private JobAdapter jobAdapter;
    private List<Job> allJobsList;
    private List<Job> filteredJobsList;
    private String categoryName;

    // <<< THÊM CÁC BIẾN CHO TOP COMPANIES >>>
    private RecyclerView rvTopCompanies;
    private TopCompanyAdapter topCompanyAdapter;
    private List<Company> topCompanyList;
    private ProgressBar pbLoadingTopCompanies; // ProgressBar cho Top Companies
    private ApiService apiService; // <<< ĐÃ THÊM ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_jobs);

        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(this, "Không nhận được thông tin danh mục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initApiService(); // <<< KHỞI TẠO ApiService
        findViews();
        tvToolbarTitle.setText(categoryName);

        // <<< THAY ĐỔI PHẦN NÀY >>>
        // initAllJobsData(); // Không dùng mock data cho Top Companies nữa
        setupTopCompaniesRecyclerView(); // Thiết lập RecyclerView cho Top Companies
        fetchTopCompanies();          // Gọi API lấy Top Companies

        filterJobsByCategory(); // Phần này giữ nguyên cho Jobs (cần xem xét lại nguồn allJobsList)
        setupJobsRecyclerView();
        setupClickListeners();
    }

    // <<< THÊM PHƯƠNG THỨC KHỞI TẠO ApiService >>>
    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            // Cân nhắc finish() activity hoặc không cho phép thực hiện các hành động API
            // finish();
            return;
        }
        // Đảm bảo URL kết thúc bằng dấu "/"
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // Đặt URL cơ sở
                .addConverterFactory(GsonConverterFactory.create()) // Sử dụng Gson để chuyển đổi JSON
                .build();
        apiService = retrofit.create(ApiService.class); // Tạo instance của ApiService
    }

    private void findViews() {
        tvToolbarTitle = findViewById(R.id.toolbar_title_category);
        btnBack = findViewById(R.id.btnBackCategory);
        btnFilter = findViewById(R.id.btnFilterCategory);
        rvCategoryJobs = findViewById(R.id.rvCategoryJobs);
        // <<< ÁNH XẠ CHO TOP COMPANIES >>>
        rvTopCompanies = findViewById(R.id.rvTopCompanies);
        // Giả sử bạn có ProgressBar với ID này trong activity_category_jobs.xml
        // Nếu chưa có, hãy thêm vào layout:
        // <ProgressBar android:id="@+id/pbLoadingTopCompanies" ... />
        // pbLoadingTopCompanies = findViewById(R.id.pbLoadingTopCompanies); // Bỏ comment nếu đã thêm ProgressBar
    }

    // <<< THÊM PHƯƠNG THỨC THIẾT LẬP RECYCLERVIEW CHO TOP COMPANIES >>>
    private void setupTopCompaniesRecyclerView() {
        topCompanyList = new ArrayList<>();
        topCompanyAdapter = new TopCompanyAdapter(this, topCompanyList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTopCompanies.setLayoutManager(layoutManager);
        rvTopCompanies.setAdapter(topCompanyAdapter);
        rvTopCompanies.setHasFixedSize(true);
    }

    // <<< THÊM PHƯƠNG THỨC GỌI API LẤY TOP COMPANIES >>>
    private void fetchTopCompanies() {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ API.", Toast.LENGTH_SHORT).show();
            return;
        }
        // if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.VISIBLE); // Bỏ comment nếu có ProgressBar
        Log.d(TAG, "Fetching top companies...");

        Call<CompaniesApiResponse> call = apiService.getTopCompanies();
        call.enqueue(new Callback<CompaniesApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompaniesApiResponse> call, @NonNull Response<CompaniesApiResponse> response) {
                // if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.GONE); // Bỏ comment nếu có ProgressBar
                if (isFinishing() || isDestroyed()) return; // Kiểm tra Activity còn tồn tại

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Company> fetchedCompanies = response.body().getData();
                    if (fetchedCompanies != null && !fetchedCompanies.isEmpty()) {
                        Log.d(TAG, "Top companies fetched: " + fetchedCompanies.size());
                        topCompanyAdapter.updateData(fetchedCompanies);
                    } else {
                        Log.d(TAG, "No top companies found or data is null.");
                        // Có thể hiển thị thông báo "Không có công ty hàng đầu"
                        // ví dụ: Toast.makeText(CategoryJobsActivity.this, "Không có công ty hàng đầu.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý lỗi API
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
                // if (pbLoadingTopCompanies != null) pbLoadingTopCompanies.setVisibility(View.GONE); // Bỏ comment nếu có ProgressBar
                if (isFinishing() || isDestroyed()) return; // Kiểm tra Activity còn tồn tại
                Log.e(TAG, "Lỗi mạng khi tải top companies: " + t.getMessage(), t);
                Toast.makeText(CategoryJobsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // HÀM NÀY CẦN SỬA LẠI CÁC LỜI GỌI `new Job(...)` hoặc lấy từ API
    // Hiện tại, nó chỉ tạo dữ liệu mẫu cho danh sách công việc chính,
    // không liên quan trực tiếp đến Top Companies từ API nữa.
    private void initAllJobsData() {
        allJobsList = new ArrayList<>();
        String defaultDescription = "Building new user-facing features...\nAssisting with optimising build pipelines...\nImproving performance...\nAdding analytics...";
        // ... (Phần còn lại của dữ liệu mẫu cho Jobs)

        // Ví dụ:
        Job job1 = new Job();
        job1.setId(1);
        job1.setTitle("Remote UI/UX Designer (Mẫu)");
        job1.setLocation("Jakarta - Indonesia");
        job1.setSalaryMin("500");
        job1.setSalaryMax("1000");
        job1.setSalaryPeriod("MONTH");
        job1.setJobType("REMOTE");
        job1.setTopJob(true); // Giả sử đây là top job trong dữ liệu mẫu
        job1.setStatus("OPEN");
        job1.setDescription(defaultDescription);
        job1.setCreatedAt("1 hours ago");
        Company company1 = new Company();
        company1.setId(101); // ID công ty mẫu
        company1.setName("Twitter (Mẫu)"); // Phân biệt với dữ liệu từ API
        company1.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Logo_of_Twitter.svg/512px-Logo_of_Twitter.svg.png"); // URL logo mẫu
        job1.setCompany(company1);
        allJobsList.add(job1);

        // Thêm các job mẫu khác nếu cần cho rvCategoryJobs
        Job job2 = new Job();
        job2.setId(2);
        job2.setTitle("Android Developer (Mẫu)");
        job2.setLocation("Mountain View, CA");
        // ... các set khác cho job2
        Company company2 = new Company();
        company2.setId(102);
        company2.setName("Google (Mẫu)");
        company2.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/2/2f/Google_2015_logo.svg");
        job2.setCompany(company2);
        allJobsList.add(job2);
    }

    private void filterJobsByCategory() {
        // Nếu allJobsList chưa được khởi tạo từ API hoặc mock data, cần xử lý
        if (allJobsList == null) {
            allJobsList = new ArrayList<>(); // Khởi tạo nếu null
            // Cân nhắc gọi API để lấy allJobsList ở đây nếu chưa có
            initAllJobsData(); // Tạm thời gọi lại để có dữ liệu mẫu cho jobs
        }

        filteredJobsList = new ArrayList<>();
        if (categoryName == null) {
            return;
        }
        // Lọc dựa trên title (ví dụ đơn giản, bạn có thể thay bằng tags hoặc category thực tế)
        // Hoặc nếu categoryName là ID, bạn sẽ lọc theo job.getCategoryId()
        filteredJobsList = allJobsList.stream()
                .filter(job -> job.getTitle() != null && job.getTitle().toLowerCase().contains(categoryName.toLowerCase()))
                .collect(Collectors.toList());

        if (filteredJobsList.isEmpty()) {
            // Toast.makeText(this, "Không tìm thấy công việc nào cho danh mục: " + categoryName, Toast.LENGTH_LONG).show();
            // Hiển thị trạng thái trống cho rvCategoryJobs nếu cần
        }
    }

    private void setupJobsRecyclerView() {
        if (filteredJobsList == null) {
            filteredJobsList = new ArrayList<>();
        }
        jobAdapter = new JobAdapter(this, filteredJobsList); // Đảm bảo JobAdapter đã được cập nhật để dùng Job mới
        rvCategoryJobs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryJobs.setAdapter(jobAdapter);
        rvCategoryJobs.setHasFixedSize(true);
        // Tắt cuộn lồng nếu rvCategoryJobs nằm trong NestedScrollView khác
        // (Trong layout hiện tại, rvCategoryJobs nằm trong NestedScrollView của activity)
        rvCategoryJobs.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Filter chưa được cài đặt", Toast.LENGTH_SHORT).show();
            // TODO: Implement filter bottom sheet or activity
        });
    }
}
