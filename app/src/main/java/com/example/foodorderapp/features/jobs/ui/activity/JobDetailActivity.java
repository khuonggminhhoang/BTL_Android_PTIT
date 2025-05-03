package com.example.foodorderapp.features.jobs.ui.activity; // Sử dụng package của bạn

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log; // Import Log để debug nếu cần
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

// --- THÊM IMPORT GLIDE ---
import com.bumptech.glide.Glide;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter; // Đảm bảo import đúng
import com.example.foodorderapp.core.model.Job; // Đảm bảo import đúng model Job đã sửa
import com.google.android.material.tabs.TabLayout;

public class JobDetailActivity extends AppCompatActivity {

    private ImageView ivCompanyLogoDetail;
    private TextView tvCompanyNameDetail, tvJobTitleDetail, tvLocationDetail, tvApplicants, tvSalaryDetail;
    private ImageButton btnBack, btnFavoriteDetail;
    private Button btnApply;
    private TabLayout tabLayout;
    private LinearLayout layoutTags;
    private FrameLayout tabContentContainer;
    private LinearLayout layoutDescriptionContent;
    private TextView tvDescription;
    private LinearLayout layoutCompanyContent;
    private TextView tvCompanyDescription, tvWebsite, tvIndustry, tvCompanySize, tvOfficeAddress;

    private Job currentJob; // Sử dụng Job model đã sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        findViews();
        getIntentData();

        if (currentJob != null) {
            populateCommonUi(); // <<< HÀM NÀY ĐÃ ĐƯỢC SỬA
            setupTabLayout();
            setupClickListeners();
        } else {
            Toast.makeText(this, "Không thể tải chi tiết công việc.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void findViews() {
        ivCompanyLogoDetail = findViewById(R.id.ivCompanyLogoDetail);
        tvCompanyNameDetail = findViewById(R.id.tvCompanyNameDetail);
        tvJobTitleDetail = findViewById(R.id.tvJobTitleDetail);
        tvLocationDetail = findViewById(R.id.tvLocationDetail);
        tvApplicants = findViewById(R.id.tvApplicants);
        tvSalaryDetail = findViewById(R.id.tvSalaryDetail);
        btnBack = findViewById(R.id.btnBack);
        btnFavoriteDetail = findViewById(R.id.btnFavoriteDetail);
        btnApply = findViewById(R.id.btnApply);
        tabLayout = findViewById(R.id.tabLayout);
        layoutTags = findViewById(R.id.layoutTags);
        tabContentContainer = findViewById(R.id.tabContentContainer);
        layoutDescriptionContent = findViewById(R.id.layoutDescriptionContent);
        tvDescription = findViewById(R.id.tvDescription);
        layoutCompanyContent = findViewById(R.id.layoutCompanyContent);
        tvCompanyDescription = findViewById(R.id.tvCompanyDescription);
        tvWebsite = findViewById(R.id.tvWebsite);
        tvIndustry = findViewById(R.id.tvIndustry);
        tvCompanySize = findViewById(R.id.tvCompanySize);
        tvOfficeAddress = findViewById(R.id.tvOfficeAddress);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        // Lấy key từ nơi gọi (ví dụ JobAdapter hoặc CategoryJobsActivity)
        String jobDetailKey = JobAdapter.JOB_DETAIL_KEY; // Hoặc key bạn đã dùng
        // String jobIdKey = "JOB_ID"; // Nếu bạn chỉ truyền ID

        if (intent != null) {
            if (intent.hasExtra(jobDetailKey)) {
                currentJob = (Job) intent.getSerializableExtra(jobDetailKey);
            }
            // else if (intent.hasExtra(jobIdKey)) {
            //     String jobId = intent.getStringExtra(jobIdKey);
            //     // TODO: Lấy chi tiết Job từ Database/API dựa trên jobId
            //     // currentJob = YourRepository.getJobById(jobId);
            // }
        }
    }

    private void populateCommonUi() {
        // --- SỬA Ở ĐÂY: Dùng Glide để load logo ---
        String logoUrl = currentJob.getCompanyLogoUrl();
        Glide.with(this)
                .load(logoUrl)
                .placeholder(R.mipmap.ic_launcher) // Thay placeholder phù hợp
                .error(R.mipmap.ic_launcher_round) // Thay ảnh lỗi phù hợp
                // .centerCrop() // Tùy chọn scaleType
                .into(ivCompanyLogoDetail);

        // --- Các phần khác giữ nguyên ---
        tvCompanyNameDetail.setText(currentJob.getCompanyName());
        tvJobTitleDetail.setText(currentJob.getJobTitle());
        tvLocationDetail.setText(currentJob.getLocation());
        tvSalaryDetail.setText(currentJob.getSalary());
        String applicantsText = currentJob.getApplicantCount() + " " + getString(R.string.applicants_label); // Cần định nghĩa string này
        tvApplicants.setText(applicantsText);
        updateFavoriteButton(currentJob.isFavorite());
        displayTags();
    }

    // --- Các phương thức populateDescriptionTab, populateCompanyTab, displayTags, updateFavoriteButton giữ nguyên ---
    private void populateDescriptionTab() {
        tvDescription.setText(currentJob.getDescription());
    }

    private void populateCompanyTab() {
        tvCompanyDescription.setText(currentJob.getCompanyInfo());
        tvWebsite.setText(currentJob.getWebsite());
        // Tự động tạo link cho website
        Linkify.addLinks(tvWebsite, Linkify.WEB_URLS);
        tvIndustry.setText(currentJob.getIndustry());
        tvCompanySize.setText(currentJob.getCompanySize());
        tvOfficeAddress.setText(currentJob.getOfficeAddress());
    }

    private void displayTags() {
        layoutTags.removeAllViews(); // Xóa các tag cũ trước khi thêm mới
        if (currentJob.getTags() != null && !currentJob.getTags().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (String tagText : currentJob.getTags()) {
                // Sử dụng layout item_tag_template.xml bạn đã có
                TextView tagView = (TextView) inflater.inflate(R.layout.item_tag_template, layoutTags, false);
                tagView.setText(tagText);
                // Thêm khoảng cách giữa các tag (tùy chọn)
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.tag_margin)); // Cần định nghĩa dimen này
                tagView.setLayoutParams(params);

                layoutTags.addView(tagView);
            }
        } else {
            layoutTags.setVisibility(View.GONE); // Ẩn layout nếu không có tag
        }
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            btnFavoriteDetail.setImageResource(R.drawable.ic_heart_filled_red);
            btnFavoriteDetail.clearColorFilter(); // Xóa filter vì icon đã có màu đỏ
        } else {
            btnFavoriteDetail.setImageResource(R.drawable.ic_favorite_border); // Icon viền
            // Set màu viền nếu icon gốc là trắng/đen
            btnFavoriteDetail.setColorFilter(ContextCompat.getColor(this, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }


    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFavoriteDetail.setOnClickListener(v -> {
            if (currentJob == null) return;
            boolean isNowFavorite = !currentJob.isFavorite();
            currentJob.setFavorite(isNowFavorite);
            updateFavoriteButton(isNowFavorite);

            // TODO: Lưu trạng thái isNowFavorite và currentJob.getId() vào DB/SharedPreferences
            Toast.makeText(this, isNowFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });

        btnApply.setOnClickListener(v -> {
            if (currentJob == null) return;
            Intent applyIntent = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
            // Truyền object Job (đã implement Serializable)
            applyIntent.putExtra(JobAdapter.JOB_DETAIL_KEY, currentJob);
            startActivity(applyIntent);
        });
    }

    // --- setupTabLayout, showDescriptionContent, showCompanyContent giữ nguyên ---
    private void setupTabLayout() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_description))); // Cần string này
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_company))); // Cần string này

        // Hiển thị tab mặc định (Description)
        showDescriptionContent();
        populateDescriptionTab(); // Populate nội dung cho tab mặc định

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showDescriptionContent();
                    populateDescriptionTab(); // Load lại nội dung khi chọn
                } else if (tab.getPosition() == 1) {
                    showCompanyContent();
                    populateCompanyTab(); // Load lại nội dung khi chọn
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        // Đảm bảo tab đầu tiên được chọn sau khi thêm listener
        // tabLayout.selectTab(tabLayout.getTabAt(0)); // Hoặc để mặc định nếu addTab đã chọn sẵn
    }

    private void showDescriptionContent() {
        layoutDescriptionContent.setVisibility(View.VISIBLE);
        layoutCompanyContent.setVisibility(View.GONE);
    }

    private void showCompanyContent() {
        layoutDescriptionContent.setVisibility(View.GONE);
        layoutCompanyContent.setVisibility(View.VISIBLE);
    }
}