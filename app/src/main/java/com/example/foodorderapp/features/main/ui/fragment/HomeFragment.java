package com.example.foodorderapp.features.main.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config; // Import Config
import com.example.foodorderapp.core.model.JobCategory;
import com.example.foodorderapp.features.jobs.ui.activity.SearchActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.CategoryAdapter;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.network.ApiService; // Import ApiService
import com.example.foodorderapp.network.response.PaginatedJobResponse; // Import PaginatedJobResponse

import java.io.IOException; // Import IOException
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private RecyclerView rvCategories;
    private RecyclerView rvJobs;
    private CategoryAdapter categoryAdapter;
    private JobAdapter jobAdapter;
    private List<JobCategory> categoryList;
    private List<Job> jobList; // Danh sách công việc sẽ được cập nhật bởi API
    private TextView tvHello;
    private TextView etSearch;
    private NestedScrollView nestedScrollView; // Thêm NestedScrollView
    private ProgressBar pbLoadingJobs, pbLoadingMoreJobs; // ProgressBar cho tải ban đầu và tải thêm

    private ApiService apiService; // Đối tượng ApiService
    private boolean isLoading = false; // Cờ kiểm soát trạng thái tải dữ liệu
    private boolean isLastPage = false; // Cờ kiểm soát đã tải hết trang chưa
    private int currentPage = 1; // Trang hiện tại, bắt đầu từ 1
    private final int PAGE_SIZE = 5; // Số lượng item mỗi trang, backend giới hạn là 5

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApiService(); // Khởi tạo ApiService
        jobList = new ArrayList<>(); // Khởi tạo jobList
        categoryList = new ArrayList<>(); // Khởi tạo categoryList
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        nestedScrollView = view.findViewById(R.id.nestedScrollViewHome); // Ánh xạ NestedScrollView
        pbLoadingJobs = view.findViewById(R.id.pbLoadingJobs); // ProgressBar tải ban đầu
        pbLoadingMoreJobs = view.findViewById(R.id.pbLoadingMoreJobs); // ProgressBar tải thêm

        // Setup RecyclerViews
        setupCategoryRecyclerView();
        setupJobRecyclerView();

        // Setup Listeners
        setupSearchListener();
        setupScrollListener(); // Setup listener cho NestedScrollView

        // Tải dữ liệu ban đầu
        loadInitialData();
    }

    private void initApiService() {
        // Khởi tạo Retrofit và ApiService
        // Đảm bảo Config.BE_URL đã được định nghĩa chính xác
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void setupCategoryRecyclerView() {
        // Tạm thời giữ nguyên phần category, bạn có thể thay đổi để gọi API sau
        // categoryList = new ArrayList<>(); // Đã khởi tạo ở onCreate
        // Thêm dữ liệu mẫu hoặc gọi API cho categories
        // Ví dụ:
        // categoryList.add(new JobCategory(1, "Remote", "url_icon_remote", null, null, null, null));
        // categoryList.add(new JobCategory(2, "Full-time", "url_icon_fulltime", null, null, null, null));

        if (getContext() != null) {
            categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            rvCategories.setLayoutManager(layoutManager);
            rvCategories.setAdapter(categoryAdapter);
            rvCategories.setHasFixedSize(true);
            // TODO: Gọi API để lấy danh sách categories và cập nhật adapter
            // loadCategories();
        }
    }

    private void setupJobRecyclerView() {
        if (getContext() != null) {
            jobAdapter = new JobAdapter(requireContext(), jobList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
            rvJobs.setLayoutManager(layoutManager);
            rvJobs.setAdapter(jobAdapter);
            rvJobs.setNestedScrollingEnabled(false); // Quan trọng khi RecyclerView nằm trong NestedScrollView
        }
    }

    private void setupSearchListener() {
        etSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                // intent.putExtra("search", etSearch.getText().toString()); // Không cần thiết nếu etSearch là TextView
                startActivity(intent);
            }
        });
    }

    private void setupScrollListener() {
        if (nestedScrollView == null) return;

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Kiểm tra nếu scroll đến cuối cùng của NestedScrollView
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                Log.d(TAG, "Scrolled to bottom. CurrentPage: " + currentPage + ", IsLoading: " + isLoading + ", IsLastPage: " + isLastPage);
                if (!isLoading && !isLastPage) {
                    currentPage++;
                    loadJobs(currentPage);
                }
            }
        });
    }

    private void loadInitialData() {
        jobList.clear(); // Xóa dữ liệu cũ trước khi tải mới
        currentPage = 1;
        isLastPage = false;
        if (jobAdapter != null) {
            jobAdapter.clearJobs(); // Xóa item trong adapter
        }
        loadJobs(currentPage);
        // loadCategories(); // Bạn có thể thêm hàm load categories nếu cần
    }

    private void loadJobs(int page) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Cannot load jobs.");
            if(getContext() != null) Toast.makeText(getContext(), "Lỗi kết nối dịch vụ.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isLoading || isLastPage) {
            return; // Không tải nếu đang tải hoặc đã hết trang
        }

        isLoading = true;
        if (page == 1) {
            pbLoadingJobs.setVisibility(View.VISIBLE); // Hiển thị ProgressBar cho lần tải đầu
            pbLoadingMoreJobs.setVisibility(View.GONE);
        } else {
            pbLoadingMoreJobs.setVisibility(View.VISIBLE); // Hiển thị ProgressBar cho tải thêm
        }

        // SỬA THAM SỐ SORT Ở ĐÂY
        // Backend NestJS của bạn (với TransformSort decorator) có vẻ mong đợi "-createdAt" cho DESC
        Call<PaginatedJobResponse> call = apiService.getJobsPaginated(page, PAGE_SIZE, "-createdAt");

        call.enqueue(new Callback<PaginatedJobResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedJobResponse> call, @NonNull Response<PaginatedJobResponse> response) {
                isLoading = false;
                pbLoadingJobs.setVisibility(View.GONE);
                pbLoadingMoreJobs.setVisibility(View.GONE);

                if (isAdded() && getContext() != null) { // Kiểm tra fragment còn attached
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        PaginatedJobResponse paginatedResponse = response.body();
                        List<Job> newJobs = paginatedResponse.getData();

                        if (newJobs != null && !newJobs.isEmpty()) {
                            jobAdapter.addJobs(newJobs);
                        }

                        if (paginatedResponse.getMeta() != null) {
                            Log.d(TAG, "API Response Meta: TotalPages=" + paginatedResponse.getMeta().getTotalPages() + ", CurrentPage=" + paginatedResponse.getMeta().getCurrentPage());
                            if (paginatedResponse.getMeta().getCurrentPage() >= paginatedResponse.getMeta().getTotalPages()) {
                                isLastPage = true;
                                Log.d(TAG, "Reached last page.");
                            }
                            // Kiểm tra nếu pageSize từ API khác với client (dù không nên xảy ra nếu backend cố định)
                            if (paginatedResponse.getMeta().getPageSize() > 0 && paginatedResponse.getMeta().getPageSize() < PAGE_SIZE) {
                                Log.w(TAG, "API pageSize (" + paginatedResponse.getMeta().getPageSize() + ") is smaller than client PAGE_SIZE (" + PAGE_SIZE + "). This might lead to issues.");
                            }
                        } else {
                            // Nếu không có meta, giả sử là trang cuối nếu không có dữ liệu trả về
                            if (newJobs == null || newJobs.isEmpty()) {
                                isLastPage = true;
                            }
                            Log.w(TAG, "API response meta is null. Assuming last page if no new jobs.");
                        }
                    } else {
                        // Xử lý lỗi API
                        String errorBodyString = "";
                        if (response.errorBody() != null) {
                            try {
                                errorBodyString = response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Error reading error body", e);
                            }
                        }
                        Log.e(TAG, "API Error: " + response.code() + " - " + response.message() + " | Error Body: " + errorBodyString);
                        Toast.makeText(getContext(), "Không thể tải danh sách công việc. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                        // Nếu lỗi ở trang > 1, có thể giảm currentPage để thử lại sau
                        if (currentPage > 1) {
                            currentPage--;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaginatedJobResponse> call, @NonNull Throwable t) {
                isLoading = false;
                pbLoadingJobs.setVisibility(View.GONE);
                pbLoadingMoreJobs.setVisibility(View.GONE);
                if (isAdded() && getContext() != null) { // Kiểm tra fragment còn attached
                    Log.e(TAG, "Network Failure: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    if (currentPage > 1) {
                        currentPage--;
                    }
                }
            }
        });
    }

    // (Tùy chọn) Hàm tải danh mục nếu bạn cũng muốn lấy từ API
    // private void loadCategories() { ... }

    @Override
    public void onResume() {
        super.onResume();
        // Cân nhắc có nên tải lại dữ liệu khi fragment resume hay không
        // Ví dụ: nếu có thay đổi từ màn hình khác, bạn có thể muốn làm mới
        // loadInitialData(); // Bỏ comment nếu muốn tải lại mỗi khi quay lại fragment
    }
}
