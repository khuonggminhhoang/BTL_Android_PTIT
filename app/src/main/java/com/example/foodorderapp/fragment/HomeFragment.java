package com.example.foodorderapp.fragment; // Package fragment mới

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // Import Fragment
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView nếu cần thay đổi text động

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.CategoryAdapter;
import com.example.foodorderapp.adapter.JobAdapter;
import com.example.foodorderapp.model.Category;
import com.example.foodorderapp.model.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Fragment cho màn hình chính (Trang chủ)
public class HomeFragment extends Fragment {

    // Các biến thành viên được chuyển từ MainActivity vào đây
    private RecyclerView rvCategories;
    private RecyclerView rvJobs;
    private CategoryAdapter categoryAdapter;
    private JobAdapter jobAdapter;
    private List<Category> categoryList;
    private List<Job> jobList;
    // Các view khác nếu cần tương tác (ví dụ: TextView chào mừng)
    private TextView tvHello;

    // Constructor mặc định (bắt buộc cho Fragment)
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout cho fragment này
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View sau khi layout đã được inflate
        // Sử dụng view.findViewById() thay vì chỉ findViewById()
        rvCategories = view.findViewById(R.id.rvCategories);
        rvJobs = view.findViewById(R.id.rvJobs);
        tvHello = view.findViewById(R.id.tvHello); // Ví dụ ánh xạ TextView

        // Cập nhật text chào mừng (ví dụ)
        // tvHello.setText(getString(R.string.hello_user, "Tên User")); // Lấy tên user thực tế nếu có

        // Khởi tạo dữ liệu mẫu (logic giống hệt trong MainActivity cũ)
        initData();

        // Thiết lập RecyclerView cho Danh mục
        setupCategoryRecyclerView();

        // Thiết lập RecyclerView cho Công việc
        setupJobRecyclerView();
    }

    // Khởi tạo dữ liệu mẫu (fix cứng) - Giống hệt MainActivity cũ (đã sửa)
    private void initData() {
        // Dữ liệu mẫu cho Danh mục
        categoryList = new ArrayList<>();
        categoryList.add(new Category("Remote", R.drawable.ic_remote));
        categoryList.add(new Category("Freelance", R.drawable.ic_freelance));
        categoryList.add(new Category("Fulltime", R.drawable.ic_fulltime));
        categoryList.add(new Category("Internship", R.drawable.ic_internship));

        // Dữ liệu mẫu cho Công việc - Sử dụng constructor 15 tham số
        jobList = new ArrayList<>();
        String defaultDescription = "Building new user-facing features...\nAssisting with optimising build pipelines...\nImproving performance...\nAdding analytics...";
        String twitterCompanyInfo = "Twitter Indonesia is a solution for seafood addicts! We strive to express a positive impression and are committed to producing only good quality without preservatives food products.";
        String googleCompanyInfo = "Google LLC is an American multinational technology company that specializes in Internet-related services and products.";
        String facebookCompanyInfo = "Facebook is an online social media and social networking service owned by American company Meta Platforms.";
        String defaultAddress = "Jl. Muara Baru Ujung Blok T. No. 8 Pergudangan BOSCO , RT.22 / RW.17 , Penjaringan , North Jakarta City , Jakarta 14440";

        jobList.add(new Job("Twitter", "Remote UI/UX Designer", "Jakarta - Indonesia", "$500 - $1K / Month", "1 hours ago", R.drawable.ic_company_logo_placeholder, true, defaultDescription, twitterCompanyInfo, 300, Arrays.asList("UI/UX", "Remote"), "www.twitter.com", "Socialmedia", "1-50 employee", defaultAddress));
        jobList.add(new Job("Google", "Android Developer", "Mountain View, CA", "$8K - $10K / Month", "3 hours ago", R.drawable.ic_company_logo_placeholder, false, "Developing awesome Android applications...", googleCompanyInfo, 150, Arrays.asList("Android", "Fulltime", "Kotlin"), "www.google.com", "Search Engine", "10000+ employee", "1600 Amphitheatre Parkway, Mountain View, CA"));
        jobList.add(new Job("Facebook", "Frontend Engineer", "Menlo Park, CA", "$7K - $9K / Month", "5 hours ago", R.drawable.ic_company_logo_placeholder, true, "Building user interfaces with React...", facebookCompanyInfo, 450, Arrays.asList("Frontend", "React", "Remote"), "www.facebook.com", "Social Network", "10000+ employee", "1 Hacker Way, Menlo Park, CA"));
    }

    // Thiết lập RecyclerView cho Danh mục
    private void setupCategoryRecyclerView() {
        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }
        // Sử dụng requireContext() để lấy Context an toàn trong Fragment
        categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
        // Đảm bảo layout manager được đặt đúng chiều ngang
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(layoutManager);
        rvCategories.setAdapter(categoryAdapter);
        rvCategories.setHasFixedSize(true);
    }

    // Thiết lập RecyclerView cho Công việc
    private void setupJobRecyclerView() {
        if (jobList == null) {
            jobList = new ArrayList<>();
        }
        jobAdapter = new JobAdapter(requireContext(), jobList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rvJobs.setLayoutManager(layoutManager);
        rvJobs.setAdapter(jobAdapter);
        // NestedScrolling không cần thiết vì RecyclerView này nằm trong NestedScrollView của fragment_home.xml
        // rvJobs.setNestedScrollingEnabled(false);
        rvJobs.setHasFixedSize(true);
    }

    // TODO: Thêm logic khác cho HomeFragment nếu cần (ví dụ: xử lý click tìm kiếm,...)
}

