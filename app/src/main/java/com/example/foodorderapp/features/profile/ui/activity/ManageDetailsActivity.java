package com.example.foodorderapp.features.profile.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.profile.ui.adapter.ManageExperiencesAdapter;
import com.example.foodorderapp.features.profile.ui.adapter.ManageSkillsAdapter;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.ExperiencesApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageDetailsActivity extends AppCompatActivity
        implements ManageExperiencesAdapter.OnExperienceManageClickListener, ManageSkillsAdapter.OnSkillManageClickListener {

    private static final String TAG = "ManageDetailsActivity";

    private Toolbar toolbarManageDetails;
    private RecyclerView rvManageExperiences, rvManageSkills;
    private ProgressBar progressBarManageDetails;

    private ManageExperiencesAdapter experiencesAdapter;
    private ManageSkillsAdapter skillsAdapter;

    private List<Experience> experienceList;
    private List<Skill> skillList;

    private ApiService apiService;
    private String currentAccessToken;

    public static final int EDIT_EXPERIENCE_REQUEST_CODE = 101;
    public static final int EDIT_SKILL_REQUEST_CODE = 102;
    private boolean dataChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_details);

        // Khởi tạo giao diện và API
        initViews();
        setupToolbar();
        initApiService();

        // Khởi tạo danh sách và adapter
        experienceList = new ArrayList<>();
        skillList = new ArrayList<>();
        experiencesAdapter = new ManageExperiencesAdapter(this, experienceList, this);
        rvManageExperiences.setLayoutManager(new LinearLayoutManager(this));
        rvManageExperiences.setAdapter(experiencesAdapter);
        skillsAdapter = new ManageSkillsAdapter(this, skillList, this);
        rvManageSkills.setLayoutManager(new LinearLayoutManager(this));
        rvManageSkills.setAdapter(skillsAdapter);

        // Lấy token xác thực
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);
        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên làm việc hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        // Tải dữ liệu
        fetchAllDetails();
    }

    // Khởi tạo view
    private void initViews() {
        toolbarManageDetails = findViewById(R.id.toolbar_manage_details);
        rvManageExperiences = findViewById(R.id.rv_manage_experiences);
        rvManageSkills = findViewById(R.id.rv_manage_skills);
        progressBarManageDetails = findViewById(R.id.progressBar_manage_details);
    }

    // Cài đặt Toolbar
    private void setupToolbar() {
        setSupportActionBar(toolbarManageDetails);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarManageDetails.setNavigationOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("dataChanged", dataChanged);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    // Xử lý nút back
    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("dataChanged", dataChanged);
        setResult(Activity.RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    // Khởi tạo dịch vụ API
    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    // Hiển thị/ẩn trạng thái tải
    private void showLoading(boolean isLoading) {
        if (progressBarManageDetails != null) {
            progressBarManageDetails.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (!isLoading) {
            if (rvManageExperiences != null) rvManageExperiences.setVisibility(View.VISIBLE);
            if (rvManageSkills != null) rvManageSkills.setVisibility(View.VISIBLE);
        } else {
            if (rvManageExperiences != null) rvManageExperiences.setVisibility(View.GONE);
            if (rvManageSkills != null) rvManageSkills.setVisibility(View.GONE);
        }
    }

    // Tải tất cả dữ liệu
    private void fetchAllDetails() {
        showLoading(true);
        dataChanged = false;
        fetchUserExperiences();
    }

    // Tải danh sách kinh nghiệm
    private void fetchUserExperiences() {
        if (currentAccessToken == null) {
            showLoading(false);
            return;
        }
        Log.d(TAG, "Tải danh sách kinh nghiệm...");
        Call<ExperiencesApiResponse> experiencesCall = apiService.getCurrentUserExperiences("Bearer " + currentAccessToken);
        experiencesCall.enqueue(new Callback<ExperiencesApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExperiencesApiResponse> call, @NonNull Response<ExperiencesApiResponse> response) {
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Experience> fetchedExperiences = response.body().getData();
                        experiencesAdapter.updateExperiences(fetchedExperiences);
                        Log.d(TAG, "Đã tải kinh nghiệm: " + (fetchedExperiences != null ? fetchedExperiences.size() : 0));
                    } else {
                        Log.e(TAG, "Lỗi tải kinh nghiệm. Mã: " + response.code());
                        Toast.makeText(ManageDetailsActivity.this, "Không thể tải kinh nghiệm.", Toast.LENGTH_SHORT).show();
                        experiencesAdapter.updateExperiences(new ArrayList<>());
                    }
                    fetchUserSkills();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExperiencesApiResponse> call, @NonNull Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Lỗi mạng khi tải kinh nghiệm", t);
                    Toast.makeText(ManageDetailsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    experiencesAdapter.updateExperiences(new ArrayList<>());
                    fetchUserSkills();
                }
            }
        });
    }

    // Tải danh sách kỹ năng
    private void fetchUserSkills() {
        if (currentAccessToken == null) {
            showLoading(false);
            return;
        }
        Log.d(TAG, "Tải danh sách kỹ năng...");
        Call<SkillsApiResponse> skillsCall = apiService.getCurrentUserSkills("Bearer " + currentAccessToken);
        skillsCall.enqueue(new Callback<SkillsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SkillsApiResponse> call, @NonNull Response<SkillsApiResponse> response) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Skill> fetchedSkills = response.body().getData();
                        skillsAdapter.updateSkills(fetchedSkills);
                        Log.d(TAG, "Đã tải kỹ năng: " + (fetchedSkills != null ? fetchedSkills.size() : 0));
                    } else {
                        Log.e(TAG, "Lỗi tải kỹ năng. Mã: " + response.code());
                        Toast.makeText(ManageDetailsActivity.this, "Không thể tải kỹ năng.", Toast.LENGTH_SHORT).show();
                        skillsAdapter.updateSkills(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SkillsApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Lỗi mạng khi tải kỹ năng", t);
                    Toast.makeText(ManageDetailsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    skillsAdapter.updateSkills(new ArrayList<>());
                }
            }
        });
    }

    // Điều hướng đến màn hình đăng nhập
    private void navigateToLogin() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Xử lý chỉnh sửa kinh nghiệm
    @Override
    public void onEditExperienceClicked(Experience experience, int position) {
        Log.d(TAG, "Chỉnh sửa kinh nghiệm: " + experience.getTitle());
        Intent intent = new Intent(this, EditExperienceActivity.class);
        intent.putExtra(EditExperienceActivity.EXTRA_EXPERIENCE_ID, experience.getId());
        startActivityForResult(intent, EDIT_EXPERIENCE_REQUEST_CODE);
    }

    // Xử lý xóa kinh nghiệm
    @Override
    public void onDeleteExperienceClicked(Experience experience, final int position) {
        Log.d(TAG, "Xóa kinh nghiệm: " + experience.getTitle());
        new AlertDialog.Builder(this)
                .setTitle("Xóa Kinh Nghiệm")
                .setMessage("Bạn có chắc chắn muốn xóa kinh nghiệm \"" + experience.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (currentAccessToken == null) {
                        Toast.makeText(ManageDetailsActivity.this, "Lỗi xác thực.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showLoading(true);
                    apiService.deleteExperience("Bearer " + currentAccessToken, experience.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            showLoading(false);
                            if (!isFinishing() && !isDestroyed()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(ManageDetailsActivity.this, "Đã xóa: " + experience.getTitle(), Toast.LENGTH_SHORT).show();
                                    experiencesAdapter.removeItem(position);
                                    dataChanged = true;
                                } else {
                                    String errorMessage = "Lỗi khi xóa kinh nghiệm. Mã: " + response.code();
                                    if (response.errorBody() != null) {
                                        try {
                                            errorMessage += " - " + response.errorBody().string();
                                        } catch (IOException e) {
                                            Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                                        }
                                    }
                                    Log.e(TAG, errorMessage);
                                    Toast.makeText(ManageDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showLoading(false);
                            if (!isFinishing() && !isDestroyed()) {
                                Log.e(TAG, "Lỗi mạng khi xóa kinh nghiệm", t);
                                Toast.makeText(ManageDetailsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xử lý click xem chi tiết kinh nghiệm
    @Override
    public void onItemExperienceClicked(Experience experience, int position) {
        Log.d(TAG, "Xem chi tiết kinh nghiệm: " + experience.getTitle());
        Toast.makeText(this, "Xem chi tiết: " + experience.getTitle(), Toast.LENGTH_SHORT).show();
    }

    // Xử lý chỉnh sửa kỹ năng
    @Override
    public void onEditSkillClicked(Skill skill, int position) {
        Log.d(TAG, "Chỉnh sửa kỹ năng: " + skill.getName());
        Intent intent = new Intent(this, EditSkillActivity.class);
        intent.putExtra(EditSkillActivity.EXTRA_SKILL_ID, skill.getId());
        startActivityForResult(intent, EDIT_SKILL_REQUEST_CODE);
    }

    // Xử lý xóa kỹ năng
    @Override
    public void onDeleteSkillClicked(Skill skill, final int position) {
        Log.d(TAG, "Xóa kỹ năng: " + skill.getName());
        new AlertDialog.Builder(this)
                .setTitle("Xóa Kỹ Năng")
                .setMessage("Bạn có chắc chắn muốn xóa kỹ năng \"" + skill.getName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (currentAccessToken == null) {
                        Toast.makeText(ManageDetailsActivity.this, "Lỗi xác thực.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showLoading(true);
                    apiService.deleteSkill("Bearer " + currentAccessToken, skill.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            showLoading(false);
                            if (!isFinishing() && !isDestroyed()) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(ManageDetailsActivity.this, "Đã xóa: " + skill.getName(), Toast.LENGTH_SHORT).show();
                                    skillsAdapter.removeItem(position);
                                    dataChanged = true;
                                } else {
                                    String errorMessage = "Lỗi khi xóa kỹ năng. Mã: " + response.code();
                                    if (response.errorBody() != null) {
                                        try {
                                            errorMessage += " - " + response.errorBody().string();
                                        } catch (IOException e) {
                                            Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                                        }
                                    }
                                    Log.e(TAG, errorMessage);
                                    Toast.makeText(ManageDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showLoading(false);
                            if (!isFinishing() && !isDestroyed()) {
                                Log.e(TAG, "Lỗi mạng khi xóa kỹ năng", t);
                                Toast.makeText(ManageDetailsActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Xử lý kết quả từ activity chỉnh sửa
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            boolean itemWasActuallyChanged = data != null && data.getBooleanExtra("dataChanged", false);
            if (itemWasActuallyChanged) {
                Log.d(TAG, "Nhận kết quả thay đổi từ activity chỉnh sửa (Mã yêu cầu: " + requestCode + ")");
                dataChanged = true;
                fetchAllDetails();
            }
        }
    }
}