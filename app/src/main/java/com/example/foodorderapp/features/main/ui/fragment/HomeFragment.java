package com.example.foodorderapp.features.main.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.JobCategory;
import com.example.foodorderapp.features.jobs.ui.activity.SearchActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.CategoryAdapter;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategories;
    private RecyclerView rvJobs;
    private CategoryAdapter categoryAdapter;
    private JobAdapter jobAdapter;
    private List<JobCategory> categoryList;
    private List<Job> jobList;
    private TextView tvHello;
    private TextView etSearch;

    public HomeFragment() {
        // Constructor rỗng
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        rvCategories = view.findViewById(R.id.rvCategories);
        rvJobs = view.findViewById(R.id.rvJobs);
        tvHello = view.findViewById(R.id.tvHello);
        etSearch = view.findViewById(R.id.etSearch);

        // Khởi tạo dữ liệu và giao diện
        initData();
        setupCategoryRecyclerView();
        setupJobRecyclerView();

        // Xử lý click tìm kiếm
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SearchActivity.class);
            intent.putExtra("search", etSearch.getText().toString());
            startActivity(intent);
        });
    }

    // Khởi tạo dữ liệu mẫu
    private void initData() {
        categoryList = new ArrayList<>();
        jobList = new ArrayList<>();
        String defaultDescription = "Building new user-facing features...\nAssisting with optimising build pipelines...\nImproving performance...\nAdding analytics...";

        // Công việc mẫu 1
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
        Company company1 = new Company();
        company1.setName("Twitter");
        company1.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Logo_of_Twitter.svg/512px-Logo_of_Twitter.svg.png");
        job1.setCompany(company1);
        job1.setUsers(new ArrayList<>());
        jobList.add(job1);

        // Công việc mẫu 2
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
        Company company2 = new Company();
        company2.setName("Google");
        company2.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/2/2f/Google_2015_logo.svg");
        job2.setCompany(company2);
        job2.setUsers(new ArrayList<>());
        jobList.add(job2);

        // Công việc mẫu 3
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
        Company company3 = new Company();
        company3.setName("Facebook");
        company3.setLogoUrl("https://upload.wikimedia.org/wikipedia/commons/0/05/Facebook_Logo_%282019%29.png");
        job3.setCompany(company3);
        job3.setUsers(new ArrayList<>());
        jobList.add(job3);
    }

    // Cài đặt RecyclerView danh mục
    private void setupCategoryRecyclerView() {
        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setHasFixedSize(true);
    }

    // Cài đặt RecyclerView công việc
    private void setupJobRecyclerView() {
        if (jobList == null) {
            jobList = new ArrayList<>();
        }
        jobAdapter = new JobAdapter(requireContext(), jobList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rvJobs.setLayoutManager(layoutManager);
        rvJobs.setAdapter(jobAdapter);
        rvJobs.setHasFixedSize(true);
    }
}