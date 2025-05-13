package com.example.foodorderapp.features.main.ui.fragment;

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
import com.example.foodorderapp.features.main.ui.adapter.FavoriteJobsAdapter;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment implements FavoriteJobsAdapter.OnFavoriteClickListener {

    private RecyclerView recyclerViewFavorites;
    private FavoriteJobsAdapter adapter;
    private List<Job> favoriteJobList;
    private LinearLayout emptyStateLayout;
    private EditText etSearch;
    private Button btnExploreNow;
    private Toolbar toolbar;

    public FavoritesFragment() {
        // Constructor rỗng bắt buộc
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        favoriteJobList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ view
        toolbar = view.findViewById(R.id.toolbar_favorites);
        recyclerViewFavorites = view.findViewById(R.id.recyclerView_favorites);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        etSearch = view.findViewById(R.id.et_search_favorites);
        btnExploreNow = view.findViewById(R.id.btn_explore_now);

        // Cài đặt Toolbar
        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        // Cài đặt RecyclerView
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FavoriteJobsAdapter(getContext(), favoriteJobList, this);
        recyclerViewFavorites.setAdapter(adapter);

        // Cài đặt tìm kiếm
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

        // Xử lý click nút khám phá
        btnExploreNow.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chuyển đến màn hình khám phá", Toast.LENGTH_SHORT).show();
        });

        // Tải danh sách công việc yêu thích
        loadFavoriteJobs();
    }

    // Xử lý menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.favorites_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_more_favorites) {
            Toast.makeText(getContext(), "Đã chọn tùy chọn thêm", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Tải danh sách công việc yêu thích
    private void loadFavoriteJobs() {
        favoriteJobList.clear();
        // Cập nhật adapter và giao diện
        if (adapter != null) {
            adapter.updateData(favoriteJobList);
        }
        updateEmptyStateVisibility(favoriteJobList.size());
    }

    // Cập nhật trạng thái giao diện
    private void updateEmptyStateVisibility(int itemCount) {
        if (emptyStateLayout == null || recyclerViewFavorites == null) return;

        if (itemCount == 0) {
            recyclerViewFavorites.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewFavorites.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    // Xử lý bỏ yêu thích
    @Override
    public void onUnfavoriteClick(Job job, int position) {
        if (job == null) {
            Toast.makeText(getContext(), "Lỗi: Dữ liệu công việc không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Yêu cầu bỏ yêu thích Job ID: " + job.getId(), Toast.LENGTH_SHORT).show();

        boolean success = true; // Giả định thành công
        if (success) {
            loadFavoriteJobs();
        } else {
            Toast.makeText(getContext(), "Không thể bỏ yêu thích.", Toast.LENGTH_SHORT).show();
        }
    }

    // Xử lý click item
    @Override
    public void onItemClick(Job job, int position) {
        if (job == null) {
            Toast.makeText(getContext(), "Lỗi: Dữ liệu công việc không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getContext(), "Đã chọn Job ID: " + job.getId(), Toast.LENGTH_SHORT).show();

        // Mở màn hình chi tiết công việc
        Intent intent = new Intent(getContext(), JobDetailActivity.class);
        intent.putExtra("JOB_ID", job.getId());
        startActivity(intent);
    }
}