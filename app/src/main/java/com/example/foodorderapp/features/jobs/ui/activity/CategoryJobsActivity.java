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

        // --- SỬA CÁC LỜI GỌI CONSTRUCTOR Ở ĐÂY ---
        allJobsList.add(new Job("job_twitter_1", "Twitter", "Remote UI/UX Designer", "Jakarta - Indonesia", "$500 - $1K / Month", "1 hours ago", "", true, defaultDescription, twitterCompanyInfo, 300, Arrays.asList("UI/UX", "Remote"), "www.twitter.com", "Socialmedia", "1-50 employee", defaultAddress));
        allJobsList.add(new Job("job_google_1", "Google", "Android Developer", "Mountain View, CA", "$8K - $10K / Month", "3 hours ago", null, false, "Developing awesome Android applications...", googleCompanyInfo, 150, Arrays.asList("Android", "Fulltime", "Kotlin"), "www.google.com", "Search Engine", "10000+ employee", "1600 Amphitheatre Parkway, Mountain View, CA"));
        allJobsList.add(new Job("job_facebook_1", "Facebook", "Frontend Engineer", "Menlo Park, CA", "$7K - $9K / Month", "5 hours ago", "URL_LOGO_FB", true, "Building user interfaces with React...", facebookCompanyInfo, 450, Arrays.asList("Frontend", "React", "Remote"), "www.facebook.com", "Social Network", "10000+ employee", "1 Hacker Way, Menlo Park, CA"));
        allJobsList.add(new Job("job_shopee_1", "Shopee", "Backend Engineer", "Singapore", "$6K - $8K / Month", "1 day ago", "", false, "Design and implement backend services...", "Shopee is a Singaporean multinational technology company which focuses mainly on e-commerce.", 600, Arrays.asList("Backend", "Fulltime", "Go"), "www.shopee.com", "E-commerce", "10000+ employee", "1 Fusionopolis Place, Singapore"));
        allJobsList.add(new Job("job_figma_1", "Figma", "Product Designer", "San Francisco, CA", "$7K - $9K / Month", "2 days ago", null, true, "Collaborate to define and implement innovative solutions...", "Figma is a collaborative web application for interface design.", 250, Arrays.asList("Product Design", "Fulltime", "Remote", "UI/UX"), "www.figma.com", "Design Software", "201-500 employee", "760 Market St, San Francisco, CA"));
        // Thêm các công việc khác nếu cần, đảm bảo đúng constructor
    }

    private void filterJobsByCategory() {
        filteredJobsList = new ArrayList<>();
        if (allJobsList == null || categoryName == null) {
            return;
        }

        // Lọc dựa trên tags (không phân biệt chữ hoa/thường)
        filteredJobsList = allJobsList.stream()
                .filter(job -> job.getTags() != null &&
                        job.getTags().stream()
                                .anyMatch(tag -> tag.equalsIgnoreCase(categoryName)))
                .collect(Collectors.toList());

        // Hiển thị thông báo nếu không có job nào thuộc category này
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