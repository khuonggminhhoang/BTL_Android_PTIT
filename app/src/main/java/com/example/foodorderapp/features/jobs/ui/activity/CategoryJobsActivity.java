package com.example.foodorderapp.features.jobs.ui.activity; // Package của bạn

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter; // Đảm bảo import đúng adapter
import com.example.foodorderapp.core.model.Job; // Đảm bảo import đúng model Job đã sửa
import com.example.foodorderapp.core.model.Company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryJobsActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_NAME = "CATEGORY_NAME";

    private TextView tvToolbarTitle;
    private ImageButton btnBack, btnFilter;
    private RecyclerView rvCategoryJobs;
    private JobAdapter jobAdapter;
    private List<Job> allJobsList; // Nguồn dữ liệu gốc (NÊN lấy từ một nơi chung)
    private List<Job> filteredJobsList; // Danh sách hiển thị sau khi lọc
    private String categoryName;

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

        findViews();
        tvToolbarTitle.setText(categoryName);

        // **QUAN TRỌNG:** Thay vì tạo dữ liệu ở đây, hãy xem xét có một
        // nguồn dữ liệu chung (ví dụ: một lớp Repository hoặc Singleton)
        // để lấy danh sách `allJobsList`. Dưới đây chỉ là khởi tạo mẫu.
        initAllJobsData(); // <<< SỬA CÁC LỜI GỌI CONSTRUCTOR TRONG HÀM NÀY

        filterJobsByCategory();
        setupJobsRecyclerView();
        setupClickListeners();
    }


    private void findViews() {
        tvToolbarTitle = findViewById(R.id.toolbar_title_category);
        btnBack = findViewById(R.id.btnBackCategory);
        btnFilter = findViewById(R.id.btnFilterCategory);
        rvCategoryJobs = findViewById(R.id.rvCategoryJobs);
    }

    // HÀM NÀY CẦN SỬA LẠI CÁC LỜI GỌI `new Job(...)`
    private void initAllJobsData() {
        allJobsList = new ArrayList<>();
        String defaultDescription = "Building new user-facing features...\nAssisting with optimising build pipelines...\nImproving performance...\nAdding analytics...";
        String twitterCompanyInfo = "Twitter Indonesia is a solution for seafood addicts! We strive to express a positive impression and are committed to producing only good quality without preservatives food products.";
        String googleCompanyInfo = "Google LLC is an American multinational technology company that specializes in Internet-related services and products.";
        String facebookCompanyInfo = "Facebook is an online social media and social networking service owned by American company Meta Platforms.";
        String defaultAddress = "Jl. Muara Baru Ujung Blok T. No. 8 Pergudangan BOSCO , RT.22 / RW.17 , Penjaringan , North Jakarta City , Jakarta 14440";

        // Sử dụng setter để tạo Job mẫu
        Job job1 = new Job();
        job1.setId(1);
        job1.setTitle("Remote UI/UX Designer");
        job1.setLocation("Jakarta - Indonesia");
        job1.setSalaryMin("500");
        job1.setSalaryMax("1000");
        job1.setSalaryPeriod("MONTH");
        job1.setJobType("REMOTE");
        job1.setTopJob(true);
        job1.setStatus("OPEN");
        job1.setDescription(defaultDescription);
        job1.setCreatedAt("1 hours ago");
        job1.setUpdatedAt("1 hours ago");
        job1.setDeletedAt(null);
        Company company1 = new Company();
        company1.setName("Twitter");
        company1.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Logo_of_Twitter.svg/512px-Logo_of_Twitter.svg.png");
        job1.setCompany(company1);
        job1.setUsers(new ArrayList<>());
        allJobsList.add(job1);

        Job job2 = new Job();
        job2.setId(2);
        job2.setTitle("Android Developer");
        job2.setLocation("Mountain View, CA");
        job2.setSalaryMin("8000");
        job2.setSalaryMax("10000");
        job2.setSalaryPeriod("MONTH");
        job2.setJobType("FULL_TIME");
        job2.setTopJob(false);
        job2.setStatus("OPEN");
        job2.setDescription("Developing awesome Android applications...");
        job2.setCreatedAt("3 hours ago");
        job2.setUpdatedAt("3 hours ago");
        job2.setDeletedAt(null);
        Company company2 = new Company();
        company2.setName("Google");
        company2.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/2/2f/Google_2015_logo.svg");
        job2.setCompany(company2);
        job2.setUsers(new ArrayList<>());
        allJobsList.add(job2);

        Job job3 = new Job();
        job3.setId(3);
        job3.setTitle("Frontend Engineer");
        job3.setLocation("Menlo Park, CA");
        job3.setSalaryMin("7000");
        job3.setSalaryMax("9000");
        job3.setSalaryPeriod("MONTH");
        job3.setJobType("FULL_TIME");
        job3.setTopJob(true);
        job3.setStatus("OPEN");
        job3.setDescription("Building user interfaces with React...");
        job3.setCreatedAt("5 hours ago");
        job3.setUpdatedAt("5 hours ago");
        job3.setDeletedAt(null);
        Company company3 = new Company();
        company3.setName("Facebook");
        company3.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/0/05/Facebook_Logo_%282019%29.png");
        job3.setCompany(company3);
        job3.setUsers(new ArrayList<>());
        allJobsList.add(job3);

        Job job4 = new Job();
        job4.setId(4);
        job4.setTitle("Backend Engineer");
        job4.setLocation("Singapore");
        job4.setSalaryMin("6000");
        job4.setSalaryMax("8000");
        job4.setSalaryPeriod("MONTH");
        job4.setJobType("FULL_TIME");
        job4.setTopJob(false);
        job4.setStatus("OPEN");
        job4.setDescription("Design and implement backend services...");
        job4.setCreatedAt("1 day ago");
        job4.setUpdatedAt("1 day ago");
        job4.setDeletedAt(null);
        Company company4 = new Company();
        company4.setName("Shopee");
        company4.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/6/6b/Shopee_logo.svg");
        job4.setCompany(company4);
        job4.setUsers(new ArrayList<>());
        allJobsList.add(job4);

        Job job5 = new Job();
        job5.setId(5);
        job5.setTitle("Product Designer");
        job5.setLocation("San Francisco, CA");
        job5.setSalaryMin("7000");
        job5.setSalaryMax("9000");
        job5.setSalaryPeriod("MONTH");
        job5.setJobType("FULL_TIME");
        job5.setTopJob(true);
        job5.setStatus("OPEN");
        job5.setDescription("Collaborate to define and implement innovative solutions...");
        job5.setCreatedAt("2 days ago");
        job5.setUpdatedAt("2 days ago");
        job5.setDeletedAt(null);
        Company company5 = new Company();
        company5.setName("Figma");
        company5.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/3/33/Figma-logo.svg");
        job5.setCompany(company5);
        job5.setUsers(new ArrayList<>());
        allJobsList.add(job5);
    }

    private void filterJobsByCategory() {
        filteredJobsList = new ArrayList<>();
        if (allJobsList == null || categoryName == null) {
            return;
        }
        // Lọc dựa trên title (ví dụ đơn giản, bạn có thể thay bằng tags hoặc category thực tế)
        filteredJobsList = allJobsList.stream()
                .filter(job -> job.getTitle() != null && job.getTitle().toLowerCase().contains(categoryName.toLowerCase()))
                .collect(Collectors.toList());
        if (filteredJobsList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy công việc nào cho danh mục: " + categoryName, Toast.LENGTH_LONG).show();
        }
    }

    private void setupJobsRecyclerView() {
        if (filteredJobsList == null) {
            filteredJobsList = new ArrayList<>();
        }
        // Truyền danh sách đã lọc vào adapter
        jobAdapter = new JobAdapter(this, filteredJobsList); // Đảm bảo JobAdapter đã được cập nhật để dùng Job mới
        rvCategoryJobs.setLayoutManager(new LinearLayoutManager(this));
        rvCategoryJobs.setAdapter(jobAdapter);
        rvCategoryJobs.setHasFixedSize(true);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilter.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Filter chưa được cài đặt", Toast.LENGTH_SHORT).show();
        });

        // Listener cho item click (nếu JobAdapter của bạn có định nghĩa interface listener)
        /*
        if (jobAdapter != null) {
            jobAdapter.setOnItemClickListener(new JobAdapter.OnItemClickListener() { // Giả sử tên interface là OnItemClickListener
                @Override
                public void onItemClick(Job job) {
                    // Mở màn hình chi tiết công việc
                    Intent intent = new Intent(CategoryJobsActivity.this, JobDetailActivity.class);
                    intent.putExtra("JOB_ID", job.getId()); // Truyền ID
                    startActivity(intent);
                }

                @Override
                public void onFavoriteClick(Job job, int position) {
                     // Xử lý favorite/unfavorite ở đây
                     boolean isNowFavorite = !job.isFavorite();
                     job.setFavorite(isNowFavorite);
                     jobAdapter.notifyItemChanged(position); // Cập nhật lại item view
                     // TODO: Lưu trạng thái isNowFavorite vào DB/SharedPreferences
                      Toast.makeText(CategoryJobsActivity.this, isNowFavorite ? "Đã thêm yêu thích" : "Đã xóa yêu thích", Toast.LENGTH_SHORT).show();
                }
            });
        }
        */
    }
}