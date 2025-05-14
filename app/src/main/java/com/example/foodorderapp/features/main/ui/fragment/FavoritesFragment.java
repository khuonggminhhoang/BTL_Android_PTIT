package com.example.foodorderapp.features.main.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.main.ui.activity.MainActivity;
import com.example.foodorderapp.features.main.ui.adapter.FavoriteJobsAdapter;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.SavedJobsApiResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FavoritesFragment extends Fragment implements FavoriteJobsAdapter.OnFavoriteClickListener {

    private static final String TAG = "FavoritesFragment";

    private RecyclerView recyclerViewFavorites;
    private FavoriteJobsAdapter adapter;
    private List<Job> favoriteJobList; // This list will be the source of truth for the adapter
    private LinearLayout emptyStateLayout;
    private EditText etSearch;
    private Button btnExploreNow;
    private Toolbar toolbar;
    private ProgressBar progressBarFavorites;

    private ApiService apiService;
    private String currentAccessToken;

    public FavoritesFragment() {
        // Hàm khởi tạo rỗng
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        favoriteJobList = new ArrayList<>();
        initApiService();
        loadAuthToken();
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "Lỗi cấu hình BE_URL");
            if (getContext() != null) Toast.makeText(getContext(), "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
        Log.d(TAG, "Khởi tạo ApiService");
    }

    private void loadAuthToken() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
            currentAccessToken = prefs.getString("accessToken", null);
            Log.d(TAG, currentAccessToken == null ? "Token rỗng" : "Đã tải token");
        } else {
            Log.e(TAG, "Context rỗng, không tải được token");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        toolbar = view.findViewById(R.id.toolbar_favorites);
        recyclerViewFavorites = view.findViewById(R.id.recyclerView_favorites);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        etSearch = view.findViewById(R.id.et_search_favorites);
        btnExploreNow = view.findViewById(R.id.btn_explore_now);
        progressBarFavorites = view.findViewById(R.id.progressBar_favorites);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pass a new ArrayList to the adapter initially, it will be updated in loadFavoriteJobs
        adapter = new FavoriteJobsAdapter(getContext(), new ArrayList<>(favoriteJobList), this);
        recyclerViewFavorites.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) adapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnExploreNow.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                BottomNavigationView bottomNav = mainActivity.findViewById(R.id.bottomNavigationView);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.navigation_home);
                } else {
                    Log.e(TAG, "BottomNavigationView không tìm thấy trong MainActivity.");
                    Toast.makeText(getContext(), "Không thể điều hướng.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Activity không phải là MainActivity.");
                Toast.makeText(getContext(), "Lỗi điều hướng.", Toast.LENGTH_SHORT).show();
            }
        });

        if (currentAccessToken == null && getContext() != null) loadAuthToken();
        loadFavoriteJobs();
    }

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

    private void showLoading(boolean isLoading) {
        if (progressBarFavorites != null) progressBarFavorites.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (recyclerViewFavorites != null) {
            recyclerViewFavorites.setVisibility(isLoading ? View.GONE : (adapter != null && adapter.getItemCount() > 0 ? View.VISIBLE : View.GONE));
        }
        if (emptyStateLayout != null && !isLoading) {
            updateEmptyStateVisibility(adapter != null ? adapter.getItemCount() : 0);
        } else if (emptyStateLayout != null && isLoading) {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void loadFavoriteJobs() {
        if (getContext() == null) { Log.e(TAG, "Context rỗng"); return; }
        if (apiService == null) {
            Log.e(TAG, "ApiService rỗng");
            Toast.makeText(getContext(), "Lỗi dịch vụ.", Toast.LENGTH_SHORT).show();
            showLoading(false); updateEmptyStateVisibility(0); return;
        }
        if (currentAccessToken == null) {
            Log.w(TAG, "Token rỗng");
            Toast.makeText(getContext(), "Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            showLoading(false); updateEmptyStateVisibility(0);
            // navigateToLogin(); // Consider if immediate redirect is desired
            return;
        }

        showLoading(true);
        Log.d(TAG, "Tải công việc yêu thích...");
        Call<SavedJobsApiResponse> call = apiService.getSavedJobs("Bearer " + currentAccessToken);
        call.enqueue(new Callback<SavedJobsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SavedJobsApiResponse> call, @NonNull Response<SavedJobsApiResponse> response) {
                if (getActivity() != null && isAdded()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.d(TAG, "Đã tải công việc yêu thích. Số lượng: " + (response.body().getData() != null ? response.body().getData().size() : 0) );
                        favoriteJobList.clear(); // Clear the main list
                        if (response.body().getData() != null) {
                            favoriteJobList.addAll(response.body().getData()); // Populate the main list
                        }
                        if (adapter != null) {
                            // Update the adapter with a new copy of the list to ensure filtering works correctly
                            adapter.updateData(new ArrayList<>(favoriteJobList));
                        }
                    } else {
                        Log.e(TAG, "Lỗi tải công việc: " + response.code());
                        if (response.code() == 401) {
                            Toast.makeText(getContext(), "Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
                            navigateToLogin();
                        } else {
                            String errorMessage = "Lỗi tải danh sách yêu thích.";
                            try {
                                if (response.errorBody() != null) errorMessage += " Lỗi: " + response.errorBody().string();
                                else errorMessage += " Mã lỗi: " + response.code();
                            } catch (IOException e) { Log.e(TAG, "Lỗi đọc thông báo lỗi", e); }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                        favoriteJobList.clear();
                        if (adapter != null) adapter.updateData(new ArrayList<>(favoriteJobList));
                    }
                    showLoading(false);
                    updateEmptyStateVisibility(favoriteJobList.size());
                } else {
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SavedJobsApiResponse> call, @NonNull Throwable t) {
                if (getActivity() != null && isAdded()) {
                    Log.e(TAG, "Lỗi mạng khi tải công việc", t);
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    favoriteJobList.clear();
                    if (adapter != null) adapter.updateData(new ArrayList<>(favoriteJobList));
                    showLoading(false);
                    updateEmptyStateVisibility(favoriteJobList.size());
                } else {
                    showLoading(false);
                }
            }
        });
    }

    private void updateEmptyStateVisibility(int itemCount) {
        if (emptyStateLayout == null || recyclerViewFavorites == null || progressBarFavorites == null || getContext() == null) return;
        if (progressBarFavorites.getVisibility() == View.VISIBLE) return;
        if (itemCount == 0) {
            recyclerViewFavorites.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewFavorites.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUnfavoriteClick(Job job, final int position) { // Made position final
        if (job == null || getContext() == null) {
            Toast.makeText(getContext(), "Dữ liệu công việc không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (apiService == null || currentAccessToken == null) {
            Toast.makeText(getContext(), "Không thể bỏ yêu thích. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ApiService hoặc AccessToken rỗng khi bỏ yêu thích.");
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Bỏ yêu thích")
                .setMessage("Bạn có chắc muốn bỏ công việc \"" + job.getTitle() + "\" khỏi danh sách yêu thích?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    Log.d(TAG, "Xác nhận bỏ yêu thích Job ID: " + job.getId());
                    // Optional: Show a small loading indicator on the item or a general one
                    // progressBarFavorites.setVisibility(View.VISIBLE); // Or a more subtle indicator

                    Call<Void> call = apiService.unsaveJob("Bearer " + currentAccessToken, job.getId());
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            // progressBarFavorites.setVisibility(View.GONE);
                            if (getActivity() != null && isAdded()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Đã bỏ yêu thích: " + job.getTitle(), Toast.LENGTH_SHORT).show();

                                    // Remove from the main list that backs the adapter's filter
                                    // Find the actual object in favoriteJobList to remove,
                                    // especially if the list passed to adapter.removeItem might be filtered.
                                    Job jobToRemove = null;
                                    for(Job j : favoriteJobList){
                                        if(j.getId() == job.getId()){
                                            jobToRemove = j;
                                            break;
                                        }
                                    }
                                    if(jobToRemove != null) {
                                        favoriteJobList.remove(jobToRemove);
                                    }

                                    // Adapter's removeItem should handle removing from its internal filtered list
                                    // and notifying the changes.
                                    if (adapter != null) {
                                        adapter.removeItem(position); // This position is from the filtered list
                                        updateEmptyStateVisibility(adapter.getItemCount());
                                    }
                                } else {
                                    String errorMessage = "Lỗi khi bỏ yêu thích.";
                                    try {
                                        if (response.errorBody() != null) {
                                            errorMessage += " Lỗi: " + response.errorBody().string();
                                        } else {
                                            errorMessage += " Mã lỗi: " + response.code();
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, "Lỗi đọc thông báo lỗi khi bỏ yêu thích", e);
                                    }
                                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                    Log.e(TAG, "Lỗi API khi bỏ yêu thích: " + errorMessage);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            // progressBarFavorites.setVisibility(View.GONE);
                            if (getActivity() != null && isAdded()) {
                                Toast.makeText(getContext(), "Lỗi mạng khi bỏ yêu thích: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Lỗi mạng khi bỏ yêu thích", t);
                            }
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onItemClick(Job job, int position) {
        if (job == null || getContext() == null) {
            Toast.makeText(getContext(), "Dữ liệu công việc không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Nhấn Job ID: " + job.getId());
        Intent intent = new Intent(getContext(), JobDetailActivity.class);
        intent.putExtra(JobAdapter.JOB_DETAIL_KEY, job);
        startActivity(intent);
    }

    private void navigateToLogin() {
        if (getActivity() != null && isAdded()) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "FavoritesFragment onResume");
        if (currentAccessToken == null && getContext() != null) loadAuthToken();
        // It's good practice to refresh data that might have changed.
        loadFavoriteJobs();
    }
}