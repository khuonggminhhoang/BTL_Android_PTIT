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
import com.example.foodorderapp.network.request.CreateSkillRequest; // Import mới
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
    public static final String EXTRA_USER_ID = "USER_ID_FOR_NEW_SKILL"; // Extra mới cho user ID khi thêm mới

    private Toolbar toolbarEditSkill;
    private TextInputLayout tilSkillName, tilSkillLevel;
    private TextInputEditText etSkillName;
    private AutoCompleteTextView actvSkillLevel;
    private Button btnSaveSkill;
    private ProgressBar progressBarEditSkill;

    private ApiService apiService;
    private String currentAccessToken;
    // private Skill currentSkill; // Không cần thiết nếu chỉ fetch khi edit
    private int skillIdToEdit = -1;
    private int currentUserId = -1; // Lưu userId khi thêm mới
    private boolean isEditMode = false;

    private final String[] SKILL_LEVELS_DISPLAY = {"Beginner", "Intermediate", "Advance"};
    private final String[] SKILL_LEVELS_API = {"SKILL_LEVEL.BEGINNER", "SKILL_LEVEL.INTERMEDIATE", "SKILL_LEVEL.ADVANCE"};
    private Map<String, String> displayToApiLevelMap = new HashMap<>();
    private Map<String, String> apiToDisplayLevelMap = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_skill);

        initViews();
        setupToolbar();
        initApiService();
        setupSkillLevelDropdown();
        populateLevelMaps();

        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);

        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SKILL_ID)) { // Chế độ Sửa
            skillIdToEdit = intent.getIntExtra(EXTRA_SKILL_ID, -1);
            if (skillIdToEdit != -1) {
                isEditMode = true;
                toolbarEditSkill.setTitle("Chỉnh sửa Kỹ năng");
                btnSaveSkill.setText("Lưu thay đổi");
                fetchSkillDetails(skillIdToEdit);
            } else {
                Toast.makeText(this, "ID Kỹ năng không hợp lệ để sửa.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (intent != null && intent.hasExtra(EXTRA_USER_ID)) { // Chế độ Thêm mới
            isEditMode = false;
            currentUserId = intent.getIntExtra(EXTRA_USER_ID, -1);
            if (currentUserId == -1) {
                Toast.makeText(this, "ID Người dùng không hợp lệ để thêm kỹ năng.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbarEditSkill.setTitle("Thêm Kỹ năng mới");
            btnSaveSkill.setText("Thêm Kỹ năng");
        } else {
            // Không có ID skill để sửa, cũng không có User ID để thêm mới -> lỗi
            Toast.makeText(this, "Không đủ thông tin để thực hiện.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSaveSkill.setOnClickListener(v -> saveOrUpdateSkill());
    }

    private void initViews() {
        // ... (giữ nguyên)
        toolbarEditSkill = findViewById(R.id.toolbar_edit_skill);
        tilSkillName = findViewById(R.id.til_skill_name);
        etSkillName = findViewById(R.id.et_skill_name);
        tilSkillLevel = findViewById(R.id.til_skill_level);
        actvSkillLevel = findViewById(R.id.actv_skill_level);
        btnSaveSkill = findViewById(R.id.btn_save_skill);
        progressBarEditSkill = findViewById(R.id.progressBar_edit_skill);
    }

    private void setupToolbar() {
        // ... (giữ nguyên)
        setSupportActionBar(toolbarEditSkill);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarEditSkill.setNavigationOnClickListener(v -> finish());
    }

    private void initApiService() {
        // ... (giữ nguyên)
        String baseUrl = Config.BE_URL;
        if (!baseUrl.endsWith("/")) { baseUrl += "/"; }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void populateLevelMaps() {
        // ... (giữ nguyên)
        displayToApiLevelMap.clear();
        apiToDisplayLevelMap.clear();
        for (int i = 0; i < SKILL_LEVELS_DISPLAY.length; i++) {
            displayToApiLevelMap.put(SKILL_LEVELS_DISPLAY[i], SKILL_LEVELS_API[i]);
            apiToDisplayLevelMap.put(SKILL_LEVELS_API[i], SKILL_LEVELS_DISPLAY[i]);
        }
    }

    private void setupSkillLevelDropdown() {
        // ... (giữ nguyên)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, SKILL_LEVELS_DISPLAY);
        actvSkillLevel.setAdapter(adapter);
    }

    private void showLoading(boolean isLoading) {
        // ... (giữ nguyên)
        progressBarEditSkill.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveSkill.setEnabled(!isLoading);
        etSkillName.setEnabled(!isLoading);
        actvSkillLevel.setEnabled(!isLoading);
    }

    private void fetchSkillDetails(int skillId) {
        // ... (giữ nguyên)
        showLoading(true);
        Log.d(TAG, "Fetching details for skill ID: " + skillId);
        apiService.getSkillDetail("Bearer " + currentAccessToken, skillId).enqueue(new Callback<SkillDetailApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SkillDetailApiResponse> call, @NonNull Response<SkillDetailApiResponse> response) {
                showLoading(false);
                if (!isFinishing()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Skill skillToEdit = response.body().getData(); // Không gán cho currentSkill nữa
                        if (skillToEdit != null) {
                            populateSkillDataToFields(skillToEdit);
                        } else {
                            Toast.makeText(EditSkillActivity.this, "Không tìm thấy chi tiết kỹ năng.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditSkillActivity.this, "Lỗi khi tải chi tiết kỹ năng.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching skill details: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SkillDetailApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing()) {
                    Toast.makeText(EditSkillActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Network failure fetching skill details", t);
                }
            }
        });
    }

    private void populateSkillDataToFields(Skill skill) {
        // ... (giữ nguyên)
        etSkillName.setText(skill.getName());
        String displayLevel = apiToDisplayLevelMap.get(skill.getLevel());
        if (displayLevel != null) {
            actvSkillLevel.setText(displayLevel, false);
        } else {
            Log.w(TAG, "Could not map API level to display level: " + skill.getLevel());
        }
    }

    private void saveOrUpdateSkill() {
        String skillName = etSkillName.getText().toString().trim();
        String selectedDisplayLevel = actvSkillLevel.getText().toString();

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
            Toast.makeText(this, "Cấp độ kỹ năng không hợp lệ đã chọn.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Selected display level '" + selectedDisplayLevel + "' not found in displayToApiLevelMap.");
            return;
        }

        showLoading(true);

        if (isEditMode && skillIdToEdit != -1) {
            // CHẾ ĐỘ SỬA: Gọi API PATCH
            UpdateSkillRequest updateRequest = new UpdateSkillRequest(skillName, apiLevel);
            Log.d(TAG, "Updating skill ID: " + skillIdToEdit + " with Name: " + skillName + ", Level: " + apiLevel);
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
                                Log.e(TAG, "Error updating skill: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditSkillActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SkillDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Network failure updating skill", t);
                        Toast.makeText(EditSkillActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // CHẾ ĐỘ THÊM MỚI: Gọi API POST
            if (currentUserId == -1) {
                Toast.makeText(this, "Lỗi: Không có ID người dùng để thêm kỹ năng.", Toast.LENGTH_LONG).show();
                showLoading(false);
                return;
            }
            CreateSkillRequest createRequest = new CreateSkillRequest(currentUserId, skillName, apiLevel);
            Log.d(TAG, "Adding new skill for User ID: " + currentUserId + " - Name: " + skillName + ", Level: " + apiLevel);
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
                                Log.e(TAG, "Error creating skill: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditSkillActivity.this, "Lỗi thêm mới: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SkillDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Network failure creating skill", t);
                        Toast.makeText(EditSkillActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
