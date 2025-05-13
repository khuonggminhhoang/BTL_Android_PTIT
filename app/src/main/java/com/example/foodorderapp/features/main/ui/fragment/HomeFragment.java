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
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.JobCategory;
import com.example.foodorderapp.features.jobs.ui.activity.SearchActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.CategoryAdapter;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.JobCategoryResponse; // Import mới
import com.example.foodorderapp.network.response.PaginatedJobResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment"; // Tag để logging

    // Views
    private RecyclerView rvCategories; // RecyclerView cho danh mục
    private RecyclerView rvJobs; // RecyclerView cho công việc
    private CategoryAdapter categoryAdapter; // Adapter cho danh mục
    private JobAdapter jobAdapter; // Adapter cho công việc
    private List<JobCategory> categoryList; // Danh sách dữ liệu danh mục
    private List<Job> jobList; // Danh sách dữ liệu công việc
    private TextView tvHello; // TextView chào mừng
    private TextView etSearch; // TextView hoạt động như một EditText để tìm kiếm
    private NestedScrollView nestedScrollView; // Để cuộn nội dung
    private ProgressBar pbLoadingJobs, pbLoadingMoreJobs, pbLoadingCategories; // ProgressBars cho các trạng thái tải

    // API và Pagination
    private ApiService apiService; // Đối tượng dịch vụ API
    private boolean isLoadingJobs = false; // Cờ theo dõi trạng thái tải công việc
    private boolean isLoadingCategories = false; // Cờ theo dõi trạng thái tải danh mục
    private boolean isLastPage = false; // Cờ cho biết đã đến trang cuối cùng của danh sách công việc chưa
    private int currentPage = 1; // Trang hiện tại của danh sách công việc
    private final int PAGE_SIZE = 5; // Số lượng công việc trên mỗi trang

    public HomeFragment() {
        // Constructor rỗng là bắt buộc cho Fragment
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initApiService(); // Khởi tạo dịch vụ API
        jobList = new ArrayList<>(); // Khởi tạo danh sách công việc
        categoryList = new ArrayList<>(); // Khởi tạo danh sách danh mục
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

        // Ánh xạ các view từ layout
        rvCategories = view.findViewById(R.id.rvCategories);
        rvJobs = view.findViewById(R.id.rvJobs);
        tvHello = view.findViewById(R.id.tvHello);
        etSearch = view.findViewById(R.id.etSearch);
        nestedScrollView = view.findViewById(R.id.nestedScrollViewHome);
        pbLoadingJobs = view.findViewById(R.id.pbLoadingJobs);
        pbLoadingMoreJobs = view.findViewById(R.id.pbLoadingMoreJobs);
        pbLoadingCategories = view.findViewById(R.id.pbLoadingCategories); // Ánh xạ ProgressBar cho danh mục

        // Thiết lập RecyclerViews
        setupCategoryRecyclerView();
        setupJobRecyclerView();

        // Thiết lập Listeners
        setupSearchListener();
        setupScrollListener(); // Thiết lập listener cho NestedScrollView để tải thêm khi cuộn xuống cuối

        // Tải dữ liệu ban đầu
        loadInitialData();
    }

    // Khởi tạo Retrofit và ApiService
    private void initApiService() {
        String baseUrl = Config.BE_URL; // Lấy URL cơ sở từ Config
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!"); // Ghi log lỗi nếu URL không được cấu hình
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            }
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

    // Thiết lập RecyclerView cho danh mục
    private void setupCategoryRecyclerView() {
        if (getContext() != null) {
            categoryAdapter = new CategoryAdapter(requireContext(), categoryList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false); // Layout ngang
            rvCategories.setLayoutManager(layoutManager);
            rvCategories.setAdapter(categoryAdapter);
            rvCategories.setHasFixedSize(true); // Tối ưu hóa hiệu suất nếu kích thước item không đổi
        }
    }

    // Thiết lập RecyclerView cho công việc
    private void setupJobRecyclerView() {
        if (getContext() != null) {
            jobAdapter = new JobAdapter(requireContext(), jobList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false); // Layout dọc
            rvJobs.setLayoutManager(layoutManager);
            rvJobs.setAdapter(jobAdapter);
            rvJobs.setNestedScrollingEnabled(false); // Vô hiệu hóa cuộn lồng vì đã có NestedScrollView
        }
    }

    // Thiết lập listener cho ô tìm kiếm
    private void setupSearchListener() {
        etSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), SearchActivity.class); // Chuyển đến SearchActivity
                startActivity(intent);
            }
        });
    }

    // Thiết lập listener cho NestedScrollView để phát hiện khi cuộn đến cuối
    private void setupScrollListener() {
        if (nestedScrollView == null) return;

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Kiểm tra nếu đã cuộn đến cuối cùng của NestedScrollView
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                Log.d(TAG, "Đã cuộn xuống cuối. Trang hiện tại: " + currentPage + ", Đang tải công việc: " + isLoadingJobs + ", Trang cuối: " + isLastPage);
                // Nếu không đang tải và chưa phải trang cuối, tải thêm công việc
                if (!isLoadingJobs && !isLastPage) {
                    currentPage++; // Tăng số trang hiện tại
                    loadJobs(currentPage); // Tải công việc cho trang mới
                }
            }
        });
    }

    // Tải dữ liệu ban đầu (công việc và danh mục)
    private void loadInitialData() {
        jobList.clear(); // Xóa danh sách công việc cũ
        currentPage = 1; // Reset về trang đầu tiên
        isLastPage = false; // Reset cờ trang cuối
        if (jobAdapter != null) {
            jobAdapter.clearJobs(); // Xóa các mục trong adapter công việc
        }
        loadJobs(currentPage); // Tải công việc cho trang đầu tiên
        loadCategories(); // Tải danh sách danh mục
    }

    // Tải danh sách công việc từ API cho một trang cụ thể
    private void loadJobs(int page) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Không thể tải công việc.");
            if(getContext() != null) Toast.makeText(getContext(), "Lỗi kết nối dịch vụ.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Không tải nếu đang tải hoặc đã ở trang cuối
        if (isLoadingJobs || isLastPage) {
            return;
        }

        isLoadingJobs = true; // Đặt cờ đang tải công việc
        // Hiển thị ProgressBar tương ứng
        if (page == 1) {
            pbLoadingJobs.setVisibility(View.VISIBLE);
            pbLoadingMoreJobs.setVisibility(View.GONE);
        } else {
            pbLoadingMoreJobs.setVisibility(View.VISIBLE);
        }

        // Gọi API để lấy danh sách công việc đã phân trang
        Call<PaginatedJobResponse> call = apiService.getJobsPaginated(page, PAGE_SIZE, "-createdAt"); // Sắp xếp theo ngày tạo giảm dần

        call.enqueue(new Callback<PaginatedJobResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaginatedJobResponse> call, @NonNull Response<PaginatedJobResponse> response) {
                isLoadingJobs = false; // Reset cờ đang tải công việc
                // Ẩn ProgressBar
                pbLoadingJobs.setVisibility(View.GONE);
                pbLoadingMoreJobs.setVisibility(View.GONE);

                if (isAdded() && getContext() != null) { // Kiểm tra fragment còn được gắn vào Activity
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        PaginatedJobResponse paginatedResponse = response.body();
                        List<Job> newJobs = paginatedResponse.getData(); // Lấy danh sách công việc từ phản hồi

                        if (newJobs != null && !newJobs.isEmpty()) {
                            jobAdapter.addJobs(newJobs); // Thêm công việc mới vào adapter
                        }

                        // Kiểm tra thông tin phân trang từ meta
                        if (paginatedResponse.getMeta() != null) {
                            Log.d(TAG, "Phản hồi Meta API: Tổng số trang=" + paginatedResponse.getMeta().getTotalPages() + ", Trang hiện tại=" + paginatedResponse.getMeta().getCurrentPage());
                            // Nếu trang hiện tại lớn hơn hoặc bằng tổng số trang, đặt cờ trang cuối
                            if (paginatedResponse.getMeta().getCurrentPage() >= paginatedResponse.getMeta().getTotalPages()) {
                                isLastPage = true;
                                Log.d(TAG, "Đã đến trang cuối.");
                            }
                        } else {
                            // Nếu không có meta, giả sử là trang cuối nếu không có dữ liệu mới
                            if (newJobs == null || newJobs.isEmpty()) {
                                isLastPage = true;
                            }
                            Log.w(TAG, "Phản hồi meta từ API là null. Giả sử là trang cuối nếu không có công việc mới.");
                        }
                    } else {
                        // Xử lý lỗi API
                        String errorBodyString = "";
                        if (response.errorBody() != null) {
                            try {
                                errorBodyString = response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc error body cho công việc", e);
                            }
                        }
                        Log.e(TAG, "Lỗi API khi tải công việc: " + response.code() + " - " + response.message() + " | Error Body: " + errorBodyString);
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
                isLoadingJobs = false; // Reset cờ đang tải công việc
                // Ẩn ProgressBar
                pbLoadingJobs.setVisibility(View.GONE);
                pbLoadingMoreJobs.setVisibility(View.GONE);
                if (isAdded() && getContext() != null) { // Kiểm tra fragment còn được gắn vào Activity
                    Log.e(TAG, "Lỗi mạng khi tải công việc: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Lỗi mạng khi tải công việc: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    // Nếu lỗi ở trang > 1, có thể giảm currentPage để thử lại sau
                    if (currentPage > 1) {
                        currentPage--;
                    }
                }
            }
        });
    }

    // --- HÀM MỚI ĐỂ TẢI DANH MỤC ---
    private void loadCategories() {
        if (apiService == null) {
            Log.e(TAG, "ApiService is null. Không thể tải danh mục.");
            if(getContext() != null) Toast.makeText(getContext(), "Lỗi kết nối dịch vụ.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Không tải nếu đang tải
        if (isLoadingCategories) {
            return;
        }

        isLoadingCategories = true; // Đặt cờ đang tải danh mục
        pbLoadingCategories.setVisibility(View.VISIBLE); // Hiển thị ProgressBar

        // Gọi API để lấy danh sách danh mục
        Call<JobCategoryResponse> call = apiService.getJobCategories();
        call.enqueue(new Callback<JobCategoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobCategoryResponse> call, @NonNull Response<JobCategoryResponse> response) {
                isLoadingCategories = false; // Reset cờ đang tải danh mục
                pbLoadingCategories.setVisibility(View.GONE); // Ẩn ProgressBar

                if (isAdded() && getContext() != null) { // Kiểm tra fragment còn được gắn vào Activity
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<JobCategory> fetchedCategories = response.body().getData(); // Lấy danh sách danh mục từ phản hồi
                        if (fetchedCategories != null) {
                            Log.d(TAG, "Tải danh mục thành công: " + fetchedCategories.size() + " mục");
                            categoryList.clear(); // Xóa danh sách cũ
                            categoryList.addAll(fetchedCategories); // Thêm danh mục mới
                            categoryAdapter.notifyDataSetChanged(); // Cập nhật adapter
                        } else {
                            Log.w(TAG, "Danh sách danh mục tải về là null.");
                            Toast.makeText(getContext(), "Không có dữ liệu danh mục.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Xử lý lỗi API
                        String errorBodyString = "";
                        if (response.errorBody() != null) {
                            try {
                                errorBodyString = response.errorBody().string();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc error body cho danh mục", e);
                            }
                        }
                        Log.e(TAG, "Lỗi API khi tải danh mục: " + response.code() + " - " + response.message() + " | Error Body: " + errorBodyString);
                        Toast.makeText(getContext(), "Không thể tải danh mục. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<JobCategoryResponse> call, @NonNull Throwable t) {
                isLoadingCategories = false; // Reset cờ đang tải danh mục
                pbLoadingCategories.setVisibility(View.GONE); // Ẩn ProgressBar
                if (isAdded() && getContext() != null) { // Kiểm tra fragment còn được gắn vào Activity
                    Log.e(TAG, "Lỗi mạng khi tải danh mục: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Lỗi mạng khi tải danh mục: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cân nhắc việc có nên tải lại dữ liệu khi fragment resume hay không.
        // Hiện tại, chỉ tải lại khi fragment được tạo lần đầu.
        // Nếu muốn tải lại mỗi khi quay lại, hãy gọi loadInitialData() ở đây.
        // loadInitialData();
    }
}
