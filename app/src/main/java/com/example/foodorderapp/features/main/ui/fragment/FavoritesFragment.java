package com.example.foodorderapp.features.main.ui.fragment; // Hoặc package của bạn

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.Job;
// Đảm bảo import đúng Adapter và Activity chi tiết
import com.example.foodorderapp.features.main.ui.adapter.FavoriteJobsAdapter;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity; // Thay thế bằng đường dẫn đúng nếu cần

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoriteJobsAdapter.OnFavoriteClickListener {

    private RecyclerView recyclerViewFavorites;
    private FavoriteJobsAdapter adapter; // <<< KHAI BÁO ADAPTER LÀ BIẾN THÀNH VIÊN Ở ĐÂY
    private List<Job> favoriteJobList;
    private LinearLayout emptyStateLayout;
    private EditText etSearch;
    private Button btnExploreNow;
    private Toolbar toolbar;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        favoriteJobList = new ArrayList<>(); // Khởi tạo list ở đây hoặc trong onViewCreated
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ Views
        toolbar = view.findViewById(R.id.toolbar_favorites);
        recyclerViewFavorites = view.findViewById(R.id.recyclerView_favorites);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        etSearch = view.findViewById(R.id.et_search_favorites);
        btnExploreNow = view.findViewById(R.id.btn_explore_now);

        // Setup Toolbar
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        // Setup RecyclerView
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter SAU KHI favoriteJobList đã được khởi tạo
        adapter = new FavoriteJobsAdapter(getContext(), favoriteJobList, this); // <<< KHỞI TẠO ADAPTER Ở ĐÂY
        recyclerViewFavorites.setAdapter(adapter);

        // Setup Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.getFilter().filter(s);
                    updateEmptyStateVisibility(adapter.getItemCount());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        // Setup Button Click
        btnExploreNow.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Navigate to Explore Screen", Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic chuyển màn hình Explore
        });

        // Load dữ liệu ban đầu
        loadFavoriteJobs();
    }

    // --- Menu Handling ---
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.favorites_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_more_favorites) {
            Toast.makeText(getContext(), "More options clicked", Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic cho nút More
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // --- Data Loading ---
    private void loadFavoriteJobs() {
        // TODO: Thay thế bằng logic lấy dữ liệu thật
        favoriteJobList.clear();

        // --- Dữ liệu mẫu (thêm vào nếu cần test) ---

        favoriteJobList.add(new Job("job_1", "Twitter", "Remote UI/UX Designer", "Jakarta-Indonesia", "$50k - $70k",
                "2 days ago", "URL_LOGO_TWITTER", true, "Desc 1...", "Company 1...", 10,
                Arrays.asList("UI/UX", "Remote"), "twitter.com", "Social", "10k+", "SF"));
        favoriteJobList.add(new Job("job_2", "Facebook", "Remote UX Designer", "Surabaya-Indonesia", "$60k - $80k",
                "3 days ago", "URL_LOGO_FACEBOOK", true, "Desc 2...", "Company 2...", 15,
                Arrays.asList("UX", "Remote"), "facebook.com", "Social", "50k+", "MP"));

        // --- Kết thúc dữ liệu mẫu ---

        // Cập nhật adapter và UI - Adapter giờ đã được khởi tạo trong onViewCreated
        if (adapter != null) { // <<< Kiểm tra null vẫn tốt
            adapter.updateData(favoriteJobList);
        }
        updateEmptyStateVisibility(favoriteJobList.size()); // Cập nhật trạng thái trống/có
    }

    // --- UI Update ---
    private void updateEmptyStateVisibility(int itemCount) {
        if (emptyStateLayout == null || recyclerViewFavorites == null) return; // Thêm kiểm tra null cho view

        if (itemCount == 0) {
            recyclerViewFavorites.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewFavorites.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }


    // --- Adapter Click Listener Implementation ---

    @Override
    public void onUnfavoriteClick(Job job, int position) {
        // TODO: Xử lý logic bỏ yêu thích bằng ID
        if (job == null || job.getId() == null || job.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: Invalid job data!", Toast.LENGTH_SHORT).show();
            return;
        }
        String jobId = job.getId();
        Toast.makeText(getContext(), "Unfavorite requested for Job ID: " + jobId, Toast.LENGTH_SHORT).show();

        // --- Logic xóa khỏi nguồn dữ liệu (Ví dụ) ---
        // boolean success = YourDatabaseHelper.getInstance(getContext()).removeFavoriteJob(jobId);
        boolean success = true; // Giả định thành công để test

        if (success) {
            // Load lại hoặc xóa khỏi adapter
            loadFavoriteJobs(); // Cách đơn giản
            // Hoặc:
            // if (adapter != null) {
            //     adapter.removeItem(position);
            //     updateEmptyStateVisibility(adapter.getItemCount());
            // }
        } else {
            Toast.makeText(getContext(), "Failed to unfavorite.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(Job job, int position) {
        if (job == null || job.getId() == null || job.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: Invalid job data!", Toast.LENGTH_SHORT).show();
            return;
        }
        String jobId = job.getId();
        Toast.makeText(getContext(), "Clicked Job ID: " + jobId, Toast.LENGTH_SHORT).show();

        // Mở màn hình chi tiết Job
        Intent intent = new Intent(getContext(), JobDetailActivity.class); // Đảm bảo Activity này tồn tại
        intent.putExtra("JOB_ID", jobId);
        startActivity(intent);
    }
}