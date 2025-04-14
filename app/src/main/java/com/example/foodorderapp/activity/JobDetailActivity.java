package com.example.foodorderapp.activity; // Sử dụng package của bạn

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.JobAdapter;
import com.example.foodorderapp.model.Job;
import com.google.android.material.tabs.TabLayout;

public class JobDetailActivity extends AppCompatActivity {

    // ... (Các biến thành viên khác giữ nguyên) ...
    private ImageView ivCompanyLogoDetail;
    private TextView tvCompanyNameDetail, tvJobTitleDetail, tvLocationDetail, tvApplicants, tvSalaryDetail;
    private ImageButton btnBack, btnFavoriteDetail;
    private Button btnApply; // Nút "Ứng tuyển ngay"
    private TabLayout tabLayout;
    private LinearLayout layoutTags;
    private FrameLayout tabContentContainer;
    private LinearLayout layoutDescriptionContent;
    private TextView tvDescription;
    private LinearLayout layoutCompanyContent;
    private TextView tvCompanyDescription, tvWebsite, tvIndustry, tvCompanySize, tvOfficeAddress;

    private Job currentJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);

        findViews();
        getIntentData();

        if (currentJob != null) {
            populateCommonUi();
            setupTabLayout();
            setupClickListeners(); // Gọi hàm thiết lập listener
        } else {
            Toast.makeText(this, "Không thể tải chi tiết công việc.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void findViews() {
        // ... (Ánh xạ các view khác giữ nguyên) ...
        ivCompanyLogoDetail = findViewById(R.id.ivCompanyLogoDetail);
        tvCompanyNameDetail = findViewById(R.id.tvCompanyNameDetail);
        tvJobTitleDetail = findViewById(R.id.tvJobTitleDetail);
        tvLocationDetail = findViewById(R.id.tvLocationDetail);
        tvApplicants = findViewById(R.id.tvApplicants);
        tvSalaryDetail = findViewById(R.id.tvSalaryDetail);
        btnBack = findViewById(R.id.btnBack);
        btnFavoriteDetail = findViewById(R.id.btnFavoriteDetail);
        btnApply = findViewById(R.id.btnApply); // Ánh xạ nút Apply
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
        if (intent != null && intent.hasExtra(JobAdapter.JOB_DETAIL_KEY)) {
            currentJob = (Job) intent.getSerializableExtra(JobAdapter.JOB_DETAIL_KEY);
        }
    }

    private void populateCommonUi() {
        // ... (Code populate giữ nguyên) ...
        ivCompanyLogoDetail.setImageResource(currentJob.getCompanyLogoResId());
        tvCompanyNameDetail.setText(currentJob.getCompanyName());
        tvJobTitleDetail.setText(currentJob.getJobTitle());
        tvLocationDetail.setText(currentJob.getLocation());
        tvSalaryDetail.setText(currentJob.getSalary());
        String applicantsText = currentJob.getApplicantCount() + " " + getString(R.string.applicants_label);
        tvApplicants.setText(applicantsText);
        updateFavoriteButton(currentJob.isFavorite());
        displayTags();
    }

    private void populateDescriptionTab() {
        tvDescription.setText(currentJob.getDescription());
    }

    private void populateCompanyTab() {
        tvCompanyDescription.setText(currentJob.getCompanyInfo());
        tvWebsite.setText(currentJob.getWebsite());
        Linkify.addLinks(tvWebsite, Linkify.WEB_URLS);
        tvIndustry.setText(currentJob.getIndustry());
        tvCompanySize.setText(currentJob.getCompanySize());
        tvOfficeAddress.setText(currentJob.getOfficeAddress());
    }


    private void displayTags() {
        // ... (Code displayTags giữ nguyên) ...
        layoutTags.removeAllViews();
        if (currentJob.getTags() != null && !currentJob.getTags().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (String tagText : currentJob.getTags()) {
                TextView tagView = (TextView) inflater.inflate(R.layout.item_tag_template, layoutTags, false);
                tagView.setText(tagText);
                layoutTags.addView(tagView);
            }
        }
    }

    private void updateFavoriteButton(boolean isFavorite) {
        // ... (Code updateFavoriteButton giữ nguyên) ...
        if (isFavorite) {
            btnFavoriteDetail.setImageResource(R.drawable.ic_heart_filled);
            btnFavoriteDetail.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            btnFavoriteDetail.setImageResource(R.drawable.ic_favorite_border);
            btnFavoriteDetail.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFavoriteDetail.setOnClickListener(v -> {
            // ... (Logic nút favorite giữ nguyên) ...
            boolean isCurrentlyFavorite = currentJob.isFavorite();
            boolean isNowFavorite = !isCurrentlyFavorite;
            updateFavoriteButton(isNowFavorite);
            currentJob.setFavorite(isNowFavorite);
            // TODO: Lưu trạng thái isNowFavorite vào DB/SharedPreferences
            Toast.makeText(this, isNowFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });

        // --- Cập nhật Listener cho nút Apply ---
        btnApply.setOnClickListener(v -> {
            // Tạo Intent để mở ApplyJobActivity
            Intent applyIntent = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
            // Đặt đối tượng Job hiện tại vào Intent extra
            applyIntent.putExtra(JobAdapter.JOB_DETAIL_KEY, currentJob); // Dùng lại key từ JobAdapter
            // Khởi chạy ApplyJobActivity
            startActivity(applyIntent);
        });
    }

    private void setupTabLayout() {
        // ... (Code setupTabLayout giữ nguyên) ...
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_description)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_company)));
        showDescriptionContent();
        populateDescriptionTab();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showDescriptionContent();
                    populateDescriptionTab();
                } else if (tab.getPosition() == 1) {
                    showCompanyContent();
                    populateCompanyTab();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
        tabLayout.selectTab(tabLayout.getTabAt(0));
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
