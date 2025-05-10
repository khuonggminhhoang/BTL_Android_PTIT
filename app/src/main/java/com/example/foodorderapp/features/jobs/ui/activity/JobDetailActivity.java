package com.example.foodorderapp.features.jobs.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

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
        String jobDetailKey = JobAdapter.JOB_DETAIL_KEY;
        if (intent != null) {
            if (intent.hasExtra(jobDetailKey)) {
                currentJob = (Job) intent.getSerializableExtra(jobDetailKey);
            }
        }
    }

    private void populateCommonUi() {
        Company company = currentJob.getCompany();
        String logoUrl = company != null ? company.getLogoUrl() : null;
        Glide.with(this)
                .load(logoUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .into(ivCompanyLogoDetail);
        tvCompanyNameDetail.setText(company != null ? company.getName() : "");
        tvJobTitleDetail.setText(currentJob.getTitle());
        tvLocationDetail.setText(currentJob.getLocation());
        String salary = currentJob.getSalaryMin() + " - " + currentJob.getSalaryMax() + " / " + currentJob.getSalaryPeriod();
        tvSalaryDetail.setText(salary);
        updateFavoriteButton(currentJob.isTopJob());
        displayTags();
    }

    private void populateDescriptionTab() {
        tvDescription.setText(currentJob.getDescription());
    }

    private void populateCompanyTab() {
        Company company = currentJob.getCompany();
        tvCompanyDescription.setText(company != null ? company.getDescription() : "");
        tvWebsite.setText(company != null ? company.getWebsite() : "");
        Linkify.addLinks(tvWebsite, Linkify.WEB_URLS);
        tvIndustry.setText(company != null ? company.getIndustry() : "");
        tvCompanySize.setText(company != null ? company.getCompanySize() : "");
        tvOfficeAddress.setText(company != null ? company.getAddress() : "");
    }

    private void displayTags() {
        layoutTags.removeAllViews();
        layoutTags.setVisibility(View.GONE); // Nếu không có tags
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            btnFavoriteDetail.setImageResource(R.drawable.ic_heart_filled_red);
            btnFavoriteDetail.clearColorFilter();
        } else {
            btnFavoriteDetail.setImageResource(R.drawable.ic_favorite_border);
            btnFavoriteDetail.setColorFilter(ContextCompat.getColor(this, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFavoriteDetail.setOnClickListener(v -> {
            if (currentJob == null) return;
            boolean isNowFavorite = !currentJob.isTopJob();
            currentJob.setTopJob(isNowFavorite);
            updateFavoriteButton(isNowFavorite);
            Toast.makeText(this, isNowFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });
        btnApply.setOnClickListener(v -> {
            if (currentJob == null) return;
            Intent applyIntent = new Intent(JobDetailActivity.this, ApplyJobActivity.class);
            applyIntent.putExtra(JobAdapter.JOB_DETAIL_KEY, currentJob);
            startActivity(applyIntent);
        });
    }

    private void setupTabLayout() {
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