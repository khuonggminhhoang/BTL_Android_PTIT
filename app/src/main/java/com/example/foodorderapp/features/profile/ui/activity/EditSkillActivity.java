package com.example.foodorderapp.features.profile.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.request.CreateSkillRequest;
import com.example.foodorderapp.network.request.UpdateSkillRequest;
import com.example.foodorderapp.network.response.SkillDetailApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditSkillActivity extends AppCompatActivity {

    private static final String TAG = "EditSkillActivity";
    public static final String EXTRA_SKILL_ID = "SKILL_ID";
    public static final String EXTRA_USER_ID = "USER_ID_FOR_NEW_SKILL";

    private Toolbar toolbarEditSkill;
    private TextInputLayout tilSkillName, tilSkillLevel;
    private TextInputEditText etSkillName;
    private AutoCompleteTextView actvSkillLevel;
    private Button btnSaveSkill;
    private ProgressBar progressBarEditSkill;

    private ApiService apiService;
    private String currentAccessToken;
    private int skillIdToEdit = -1;
    private int currentUserId = -1;
    private boolean isEditMode = false;

    private final String[] SKILL_LEVELS_DISPLAY = {"Beginner", "Intermediate", "Advance"};
    private final String[] SKILL_LEVELS_API = {"SKILL_LEVEL.BEGINNER", "SKILL_LEVEL.INTERMEDIATE", "SKILL_LEVEL.ADVANCE"};
    private Map<String, String> displayToApiLevelMap = new HashMap<>();
    private Map<String, String> apiToDisplayLevelMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_skill);

        // Khởi tạo giao diện và API
        initViews();
        setupToolbar();
        initApiService();
        setupSkillLevelDropdown();
        populateLevelMaps();

        // Lấy token xác thực
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);
        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên làm việc hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Xử lý intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SKILL_ID)) {
            isEditMode = true;
            skillIdToEdit = intent.getIntExtra(EXTRA_SKILL_ID, -1);
            if (skillIdToEdit != -1) {
                toolbarEditSkill.setTitle("Chỉnh sửa Kỹ năng");
                btnSaveSkill.setText("Lưu thay đổi");
                fetchSkillDetails(skillIdToEdit);
            } else {
                Toast.makeText(this, "ID kỹ năng không hợp lệ.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (intent != null && intent.hasExtra(EXTRA_USER_ID)) {
            isEditMode = false;
            currentUserId = intent.getIntExtra(EXTRA_USER_ID, -1);
            if (currentUserId == -1) {
                Toast.makeText(this, "ID người dùng không hợp lệ.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbarEditSkill.setTitle("Thêm Kỹ năng mới");
            btnSaveSkill.setText("Thêm Kỹ năng");
        } else {
            Toast.makeText(this, "Thiếu thông tin để thực hiện.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cài đặt sự kiện lưu
        btnSaveSkill.setOnClickListener(v -> saveOrUpdateSkill());
    }

    // Khởi tạo view
    private void initViews() {
        toolbarEditSkill = findViewById(R.id.toolbar_edit_skill);
        tilSkillName = findViewById(R.id.til_skill_name);
        etSkillName = findViewById(R.id.et_skill_name);
        tilSkillLevel = findViewById(R.id.til_skill_level);
        actvSkillLevel = findViewById(R.id.actv_skill_level);
        btnSaveSkill = findViewById(R.id.btn_save_skill);
        progressBarEditSkill = findViewById(R.id.progressBar_edit_skill);
    }

    // Cài đặt Toolbar
    private void setupToolbar() {
        setSupportActionBar(toolbarEditSkill);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarEditSkill.setNavigationOnClickListener(v -> finish());
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

    // Cài đặt dropdown cấp độ kỹ năng
    private void setupSkillLevelDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, SKILL_LEVELS_DISPLAY);
        actvSkillLevel.setAdapter(adapter);
    }

    // Ánh xạ cấp độ hiển thị và API
    private void populateLevelMaps() {
        displayToApiLevelMap.clear();
        apiToDisplayLevelMap.clear();
        for (int i = 0; i < SKILL_LEVELS_DISPLAY.length; i++) {
            displayToApiLevelMap.put(SKILL_LEVELS_DISPLAY[i], SKILL_LEVELS_API[i]);
            apiToDisplayLevelMap.put(SKILL_LEVELS_API[i], SKILL_LEVELS_DISPLAY[i]);
        }
    }

    // Hiển thị/ẩn trạng thái tải
    private void showLoading(boolean isLoading) {
        progressBarEditSkill.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveSkill.setEnabled(!isLoading);
        etSkillName.setEnabled(!isLoading);
        actvSkillLevel.setEnabled(!isLoading);
    }

    // Tải chi tiết kỹ năng
    private void fetchSkillDetails(int skillId) {
        showLoading(true);
        Log.d(TAG, "Tải chi tiết kỹ năng ID: " + skillId);
        apiService.getSkillDetail("Bearer " + currentAccessToken, skillId).enqueue(new Callback<SkillDetailApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SkillDetailApiResponse> call, @NonNull Response<SkillDetailApiResponse> response) {
                showLoading(false);
                if (!isFinishing()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Skill skillToEdit = response.body().getData();
                        if (skillToEdit != null) {
                            populateSkillDataToFields(skillToEdit);
                        } else {
                            Toast.makeText(EditSkillActivity.this, "Không tìm thấy chi tiết kỹ năng.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditSkillActivity.this, "Lỗi tải chi tiết: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi tải chi tiết kỹ năng: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SkillDetailApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing()) {
                    Toast.makeText(EditSkillActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi mạng khi tải chi tiết kỹ năng", t);
                }
            }
        });
    }

    // Điền dữ liệu kỹ năng vào giao diện
    private void populateSkillDataToFields(Skill skill) {
        etSkillName.setText(skill.getName());
        String displayLevel = apiToDisplayLevelMap.get(skill.getLevel());
        if (displayLevel != null) {
            actvSkillLevel.setText(displayLevel, false);
        } else {
            Log.w(TAG, "Không thể ánh xạ cấp độ API: " + skill.getLevel());
        }
    }

    // Lưu hoặc cập nhật kỹ năng
    private void saveOrUpdateSkill() {
        String skillName = etSkillName.getText().toString().trim();
        String selectedDisplayLevel = actvSkillLevel.getText().toString();

        // Xác thực đầu vào
        if (TextUtils.isEmpty(skillName)) {
            tilSkillName.setError("Tên kỹ năng không được để trống");
            return;
        } else {
            tilSkillName.setError(null);
        }
        if (TextUtils.isEmpty(selectedDisplayLevel)) {
            tilSkillLevel.setError("Vui lòng chọn cấp độ");
            return;
        } else {
            tilSkillLevel.setError(null);
        }

        String apiLevel = displayToApiLevelMap.get(selectedDisplayLevel);
        if (apiLevel == null) {
            Toast.makeText(this, "Cấp độ kỹ năng không hợp lệ.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cấp độ hiển thị '" + selectedDisplayLevel + "' không tìm thấy trong ánh xạ.");
            return;
        }

        showLoading(true);

        if (isEditMode && skillIdToEdit != -1) {
            // Cập nhật kỹ năng
            UpdateSkillRequest updateRequest = new UpdateSkillRequest(skillName, apiLevel);
            Log.d(TAG, "Cập nhật kỹ năng ID: " + skillIdToEdit);
            apiService.updateSkill("Bearer " + currentAccessToken, skillIdToEdit, updateRequest).enqueue(new Callback<SkillDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<SkillDetailApiResponse> call, @NonNull Response<SkillDetailApiResponse> response) {
                    showLoading(false);
                    if (!isFinishing()) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditSkillActivity.this, "Kỹ năng đã được cập nhật!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("dataChanged", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                                Log.e(TAG, "Lỗi cập nhật kỹ năng: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditSkillActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SkillDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Lỗi mạng khi cập nhật kỹ năng", t);
                        Toast.makeText(EditSkillActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Thêm kỹ năng mới
            if (currentUserId == -1) {
                Toast.makeText(this, "Lỗi: Không có ID người dùng.", Toast.LENGTH_LONG).show();
                showLoading(false);
                return;
            }
            CreateSkillRequest createRequest = new CreateSkillRequest(currentUserId, skillName, apiLevel);
            Log.d(TAG, "Thêm kỹ năng mới cho User ID: " + currentUserId);
            apiService.createSkill("Bearer " + currentAccessToken, createRequest).enqueue(new Callback<SkillDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<SkillDetailApiResponse> call, @NonNull Response<SkillDetailApiResponse> response) {
                    showLoading(false);
                    if (!isFinishing()) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditSkillActivity.this, "Kỹ năng đã được thêm!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("dataChanged", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                                Log.e(TAG, "Lỗi thêm kỹ năng: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditSkillActivity.this, "Lỗi thêm mới: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SkillDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Lỗi mạng khi thêm kỹ năng", t);
                        Toast.makeText(EditSkillActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}