package com.example.foodorderapp.activity; // Package của bạn

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.JobAdapter;
import com.example.foodorderapp.model.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors; // Import Stream API để lọc

public class CategoryJobsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "CATEGORY_NAME";

    private TextView tvToolbarTitle;
    private ImageButton btnBack, btnFilter;
    private RecyclerView rvCategoryJobs;
    // private RecyclerView rvTopCompanies; // TODO: Thêm nếu cần
    private JobAdapter jobAdapter;
    private List<Job> allJobsList; // Danh sách chứa TẤT CẢ công việc (tạm thời)
    private List<Job> filteredJobsList; // Danh sách công việc đã lọc theo category
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_jobs);

        // Lấy tên category từ Intent
        categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName == null || categoryName.isEmpty()) {
            Toast.makeText(this, "Không nhận được thông tin danh mục", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có tên category
            return;
        }

        // Ánh xạ Views
        findViews();

        // Đặt tiêu đề Toolbar
        tvToolbarTitle.setText(categoryName);

        // Khởi tạo dữ liệu (Tạm thời tạo lại danh sách đầy đủ ở đây)
        // **LƯU Ý:** Trong ứng dụng thực tế, bạn nên có một nguồn dữ liệu chung (Singleton, Repository,...)
        // thay vì định nghĩa lại danh sách ở nhiều nơi.
        initAllJobsData();

        // Lọc danh sách công việc dựa trên categoryName
        filterJobsByCategory();

        // Thiết lập RecyclerView chính
        setupJobsRecyclerView();

        // Thiết lập RecyclerView Top Companies (Tùy chọn)
        // setupTopCompaniesRecyclerView();

        // Thiết lập sự kiện click
        setupClickListeners();
    }


    private void findViews() {
        tvToolbarTitle = findViewById(R.id.toolbar_title_category);
        btnBack = findViewById(R.id.btnBackCategory);
        btnFilter = findViewById(R.id.btnFilterCategory);
        rvCategoryJobs = findViewById(R.id.rvCategoryJobs);
        // rvTopCompanies = findViewById(R.id.rvTopCompanies); // TODO: Ánh xạ nếu dùng
    }

    // Tạm thời khởi tạo lại danh sách đầy đủ các công việc ở đây
    private void initAllJobsData() {
        allJobsList = new ArrayList<>();
        String defaultDescription = "Building new user-facing features...\nAssisting with optimising build pipelines...\nImproving performance...\nAdding analytics...";
        String twitterCompanyInfo = "Twitter Indonesia is a solution for seafood addicts! We strive to express a positive impression and are committed to producing only good quality without preservatives food products.";
        String googleCompanyInfo = "Google LLC is an American multinational technology company that specializes in Internet-related services and products.";
        String facebookCompanyInfo = "Facebook is an online social media and social networking service owned by American company Meta Platforms.";
        String defaultAddress = "Jl. Muara Baru Ujung Blok T. No. 8 Pergudangan BOSCO , RT.22 / RW.17 , Penjaringan , North Jakarta City , Jakarta 14440";

        // Thêm các công việc giống như trong HomeFragment.initData()
        allJobsList.add(new Job("Twitter", "Remote UI/UX Designer", "Jakarta - Indonesia", "$500 - $1K / Month", "1 hours ago", R.drawable.ic_company_logo_placeholder, true, defaultDescription, twitterCompanyInfo, 300, Arrays.asList("UI/UX", "Remote"), "www.twitter.com", "Socialmedia", "1-50 employee", defaultAddress));
        allJobsList.add(new Job("Google", "Android Developer", "Mountain View, CA", "$8K - $10K / Month", "3 hours ago", R.drawable.ic_company_logo_placeholder, false, "Developing awesome Android applications...", googleCompanyInfo, 150, Arrays.asList("Android", "Fulltime", "Kotlin"), "www.google.com", "Search Engine", "10000+ employee", "1600 Amphitheatre Parkway, Mountain View, CA"));
        allJobsList.add(new Job("Facebook", "Frontend Engineer", "Menlo Park, CA", "$7K - $9K / Month", "5 hours ago", R.drawable.ic_company_logo_placeholder, true, "Building user interfaces with React...", facebookCompanyInfo, 450, Arrays.asList("Frontend", "React", "Remote"), "www.facebook.com", "Social Network", "10000+ employee", "1 Hacker Way, Menlo Park, CA"));
        // Thêm các công việc khác nếu có...
        allJobsList.add(new Job("Shopee", "Backend Engineer", "Singapore", "$6K - $8K / Month", "1 day ago", R.drawable.ic_company_logo_placeholder, false, "Design and implement backend services...", "Shopee is a Singaporean multinational technology company which focuses mainly on e-commerce.", 600, Arrays.asList("Backend", "Fulltime", "Go"), "www.shopee.com", "E-commerce", "10000+ employee", "1 Fusionopolis Place, Singapore"));
        allJobsList.add(new Job("Figma", "Product Designer", "San Francisco, CA", "$7K - $9K / Month", "2 days ago", R.drawable.ic_company_logo_placeholder, true, "Collaborate to define and implement innovative solutions...", "Figma is a collaborative web application for interface design.", 250, Arrays.asList("Product Design", "Fulltime", "Remote"), "www.figma.com", "Design Software", "201-500 employee", "760 Market St, San Francisco, CA"));

    }

    // Lọc danh sách công việc theo categoryName (dựa vào tags)
    private void filterJobsByCategory() {
        filteredJobsList = new ArrayList<>();
        if (allJobsList == null || categoryName == null) {
            return;
        }

        // Dùng Stream API để lọc (yêu cầu API level 24+)
        filteredJobsList = allJobsList.stream()
                .filter(job -> job.getTags() != null &&
                        job.getTags().stream()
                                .anyMatch(tag -> tag.equalsIgnoreCase(categoryName)))
                .collect(Collectors.toList());

        // Hoặc dùng vòng lặp for truyền thống:
        /*
        for (Job job : allJobsList) {
            if (job.getTags() != null) {
                for (String tag : job.getTags()) {
                    if (tag.equalsIgnoreCase(categoryName)) {
                        filteredJobsList.add(job);
                        break; // Thêm công việc và chuyển sang công việc tiếp theo
                    }
                }
            }
        }
        */
    }

    // Thiết lập RecyclerView chính
    private void setupJobsRecyclerView() {
        if (filteredJobsList == null) {
            filteredJobsList = new ArrayList<>(); // Khởi tạo nếu null
        }
        // Tái sử dụng JobAdapter
        jobAdapter = new JobAdapter(this, filteredJobsList);
        rvCategoryJobs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryJobs.setAdapter(jobAdapter);
        rvCategoryJobs.setHasFixedSize(true); // Tối ưu hóa
    }

    // TODO: Thiết lập RecyclerView cho Top Companies nếu cần
    // private void setupTopCompaniesRecyclerView() { ... }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish()); // Đóng Activity hiện tại

        btnFilter.setOnClickListener(v -> {
            // TODO: Xử lý logic khi nhấn nút Filter
            Toast.makeText(this, "Chức năng Filter chưa được cài đặt", Toast.LENGTH_SHORT).show();
        });

        // TODO: Thêm listener cho thanh tìm kiếm, nút See More,...
    }
}
