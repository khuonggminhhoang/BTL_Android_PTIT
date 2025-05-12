package com.example.foodorderapp.features.profile.ui.activity;

import android.app.Activity;
import android.app.AlertDialog; // Đổi sang androidx.appcompat.app.AlertDialog
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.core.model.User;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.ExperiencesApiResponse;
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResumeMyInfoActivity extends AppCompatActivity {

    private static final String TAG = "ResumeMyInfoActivity";

    private Toolbar toolbarResumeInfo;
    private ImageView ivEditAboutMeResume, ivAddWorkExperienceResume, ivAddSkillResume;
    private TextView tvToolbarTitleResumeInfo, tvCvFileNameResume, tvCvUploadInfoResume, tvEditResumeLinkResume;
    private TextInputEditText etAboutMeResumeContent;
    private TextInputLayout tilAboutMeResumeContent;

    private LinearLayout llWorkExperienceItemsContainerResume, llSkillsItemsContainerResume;
    private Button btnDetailExperienceSkillsResume;
    private ProgressBar progressBarResumeInfo;

    private ApiService apiService;
    private User currentUser;
    private String currentAccessToken;

    private ActivityResultLauncher<Intent> manageDetailsLauncher;
    private ActivityResultLauncher<Intent> editSkillLauncher;
    private ActivityResultLauncher<Intent> editExperienceLauncher;


    private boolean isAboutMeEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_my_info);

        initViews();
        initApiService();
        setupToolbar();

        manageDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("dataChanged", false)) {
                            Log.d(TAG, "Data changed in ManageDetailsActivity, refreshing profile...");
                            fetchUserProfileData();
                        }
                    }
                });

        // Launcher này sẽ được dùng cho cả Thêm mới và Sửa Skill
        editSkillLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("dataChanged", false)) {
                            Log.d(TAG, "Data changed via EditSkillActivity, refreshing profile...");
                            fetchUserProfileData();
                        }
                    }
                });

        // Launcher này sẽ được dùng cho cả Thêm mới và Sửa Experience
        editExperienceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("dataChanged", false)) {
                            Log.d(TAG, "Data changed via EditExperienceActivity, refreshing profile...");
                            fetchUserProfileData();
                        }
                    }
                });

        setupClickListeners();
        fetchUserProfileData();
    }

    private void initViews() {
        toolbarResumeInfo = findViewById(R.id.toolbar_resume_info);
        tvToolbarTitleResumeInfo = findViewById(R.id.tv_toolbar_title_resume_info);
        tvCvFileNameResume = findViewById(R.id.tv_cv_file_name_resume);
        tvCvUploadInfoResume = findViewById(R.id.tv_cv_upload_info_resume);
        tvEditResumeLinkResume = findViewById(R.id.tv_edit_resume_link_resume);
        ivEditAboutMeResume = findViewById(R.id.iv_edit_about_me_resume);
        etAboutMeResumeContent = findViewById(R.id.et_about_me_resume_content);
        tilAboutMeResumeContent = findViewById(R.id.til_about_me_resume_content);
        ivAddWorkExperienceResume = findViewById(R.id.iv_add_work_experience_resume);
        llWorkExperienceItemsContainerResume = findViewById(R.id.ll_work_experience_items_container_resume);
        ivAddSkillResume = findViewById(R.id.iv_add_skill_resume);
        llSkillsItemsContainerResume = findViewById(R.id.ll_skills_items_container_resume);
        btnDetailExperienceSkillsResume = findViewById(R.id.btn_detail_experience_skills_resume);
        progressBarResumeInfo = findViewById(R.id.progressBar_resume_info);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarResumeInfo);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbarResumeInfo.setNavigationOnClickListener(v -> {
            if (isAboutMeEditing) {
                showDiscardChangesDialog();
            } else {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isAboutMeEditing) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this) // Sử dụng androidx.appcompat.app.AlertDialog
                .setTitle("Hủy bỏ thay đổi?")
                .setMessage("Bạn có thay đổi chưa lưu. Bạn có muốn hủy bỏ không?")
                .setPositiveButton("Hủy bỏ", (dialog, which) -> {
                    isAboutMeEditing = false;
                    setAboutMeEditState(false);
                    if (currentUser != null && currentUser.getAboutMe() != null) {
                        etAboutMeResumeContent.setText(currentUser.getAboutMe());
                    } else {
                        etAboutMeResumeContent.setText("");
                    }
                    finish();
                })
                .setNegativeButton("Tiếp tục sửa", null)
                .show();
    }


    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void setAboutMeEditState(boolean editing) {
        isAboutMeEditing = editing;
        etAboutMeResumeContent.setEnabled(editing);
        etAboutMeResumeContent.setFocusable(editing);
        etAboutMeResumeContent.setFocusableInTouchMode(editing);
        etAboutMeResumeContent.setCursorVisible(editing);
        etAboutMeResumeContent.setLongClickable(editing);

        if (editing) {
            ivEditAboutMeResume.setImageResource(R.drawable.ic_save);
            etAboutMeResumeContent.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etAboutMeResumeContent, InputMethodManager.SHOW_IMPLICIT);
            }
            tilAboutMeResumeContent.setBoxStrokeWidth(getResources().getDimensionPixelSize(R.dimen.dp_1));
            etAboutMeResumeContent.setBackgroundResource(R.drawable.bg_edittext_editing);
        } else {
            ivEditAboutMeResume.setImageResource(R.drawable.ic_edit);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etAboutMeResumeContent.getWindowToken(), 0);
            }
            tilAboutMeResumeContent.setBoxStrokeWidth(0);
            etAboutMeResumeContent.setBackgroundResource(R.drawable.bg_container_card);
        }
    }


    private void setupClickListeners() {
        tvEditResumeLinkResume.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng sửa/tải lên CV chưa được cài đặt", Toast.LENGTH_SHORT).show();
        });

        ivEditAboutMeResume.setOnClickListener(v -> {
            if (isAboutMeEditing) {
                saveAboutMeChanges();
            } else {
                setAboutMeEditState(true);
            }
        });

        // KHI NHẤN NÚT + ĐỂ THÊM KINH NGHIỆM
        ivAddWorkExperienceResume.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getId() != 0) { // Kiểm tra ID hợp lệ
                Log.d(TAG, "Add Work Experience button clicked. User ID: " + currentUser.getId());
                Intent intent = new Intent(ResumeMyInfoActivity.this, EditExperienceActivity.class);
                intent.putExtra(EditExperienceActivity.EXTRA_USER_ID, currentUser.getId());
                // Không truyền EXTRA_EXPERIENCE_ID để EditExperienceActivity biết đây là chế độ thêm mới
                editExperienceLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Không thể tải ID người dùng để thêm kinh nghiệm.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cannot add experience, currentUser is null or ID is invalid.");
            }
        });

        // KHI NHẤN NÚT + ĐỂ THÊM KỸ NĂNG
        ivAddSkillResume.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getId() != 0) { // Kiểm tra ID hợp lệ
                Log.d(TAG, "Add Skill button clicked. User ID: " + currentUser.getId());
                Intent intent = new Intent(ResumeMyInfoActivity.this, EditSkillActivity.class);
                intent.putExtra(EditSkillActivity.EXTRA_USER_ID, currentUser.getId());
                // Không truyền EXTRA_SKILL_ID để EditSkillActivity biết đây là chế độ thêm mới
                editSkillLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Không thể tải ID người dùng để thêm kỹ năng.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cannot add skill, currentUser is null or ID is invalid.");
            }
        });

        btnDetailExperienceSkillsResume.setOnClickListener(v -> {
            if (isAboutMeEditing) {
                new AlertDialog.Builder(this)
                        .setTitle("Lưu thay đổi?")
                        .setMessage("Bạn có thay đổi chưa lưu cho phần 'Giới thiệu'. Bạn có muốn lưu trước khi tiếp tục không?")
                        .setPositiveButton("Lưu và Tiếp tục", (dialog, which) -> saveAboutMeChangesAndProceed())
                        .setNegativeButton("Bỏ qua và Tiếp tục", (dialog, which) -> launchManageDetailsActivity())
                        .setNeutralButton("Hủy", null)
                        .show();
            } else {
                launchManageDetailsActivity();
            }
        });
    }

    private void saveAboutMeChangesAndProceed() {
        saveAboutMeChanges(true);
    }

    private void launchManageDetailsActivity() {
        Log.d(TAG, "Launching ManageDetailsActivity for result.");
        Intent intent = new Intent(ResumeMyInfoActivity.this, ManageDetailsActivity.class);
        manageDetailsLauncher.launch(intent);
    }


    private void saveAboutMeChanges() {
        saveAboutMeChanges(false);
    }

    private void saveAboutMeChanges(final boolean proceedAfterSave) {
        String newAboutMeText = etAboutMeResumeContent.getText().toString().trim();

        if (currentUser != null && currentUser.getAboutMe() != null && currentUser.getAboutMe().equals(newAboutMeText)) {
            Toast.makeText(this, "Không có thay đổi nào để lưu.", Toast.LENGTH_SHORT).show();
            setAboutMeEditState(false);
            if (proceedAfterSave) launchManageDetailsActivity();
            return;
        }

        showLoading(true);
        ivEditAboutMeResume.setEnabled(false);

        Map<String, RequestBody> fields = new HashMap<>();
        // Đảm bảo key "aboutMe" khớp với API PATCH /profile/me nếu backend yêu cầu camelCase
        fields.put("aboutMe", RequestBody.create(MediaType.parse("text/plain"), newAboutMeText));

        Log.d(TAG, "Updating aboutMe to: " + newAboutMeText);
        apiService.updateMyProfile("Bearer " + currentAccessToken, fields, null).enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                showLoading(false);
                ivEditAboutMeResume.setEnabled(true);
                if (!isFinishing()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ResumeMyInfoActivity.this, "Giới thiệu đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        if (currentUser != null) {
                            currentUser.setAboutMe(newAboutMeText);
                        }
                        setAboutMeEditState(false);
                        if (proceedAfterSave) {
                            launchManageDetailsActivity();
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                            Log.e(TAG, "Error updating aboutMe: " + response.code() + " - " + errorBody);
                            Toast.makeText(ResumeMyInfoActivity.this, "Lỗi cập nhật Giới thiệu: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                ivEditAboutMeResume.setEnabled(true);
                if (!isFinishing()) {
                    Log.e(TAG, "Network failure updating aboutMe", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showLoading(boolean isLoading) {
        if (progressBarResumeInfo != null) {
            progressBarResumeInfo.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchUserProfileData() {
        showLoading(true);
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);

        if (currentAccessToken == null) {
            showLoading(false);
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        Log.d(TAG, "Fetching user profile (/profile/me)...");
        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + currentAccessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        currentUser = response.body().getData();
                        if (currentUser != null) {
                            Log.d(TAG, "User Profile Fetched. User ID: " + currentUser.getId() + ", AboutMe: '" + currentUser.getAboutMe() + "'. Fetching skills...");
                            populateUIBasicInfo(currentUser);
                            fetchUserSkills();
                        } else {
                            showLoading(false);
                            Log.e(TAG, "Fetched user data is null from API response data (profile/me).");
                            Toast.makeText(ResumeMyInfoActivity.this, "Failed to get user data (profile data is null).", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        handleApiError(response, prefs);
                    }
                } else {
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to fetch user profile (onFailure)", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Network error fetching profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchUserSkills() {
        if (currentAccessToken == null) { showLoading(false); Log.e(TAG, "Access token is null, cannot fetch skills."); return; }
        Log.d(TAG, "Fetching user skills (/skills)...");
        Call<SkillsApiResponse> skillsCall = apiService.getCurrentUserSkills("Bearer " + currentAccessToken);
        skillsCall.enqueue(new Callback<SkillsApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<SkillsApiResponse> call, @NonNull Response<SkillsApiResponse> response) {
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Skill> skills = response.body().getData();
                        Log.d(TAG, "Successfully fetched skills: " + (skills != null ? skills.size() : "null list") + " items. Fetching experiences...");
                        if (currentUser != null && skills != null) {
                            currentUser.setSkills(skills);
                        }
                        populateSkills(skills);
                    } else {
                        Log.e(TAG, "Failed to fetch skills. Code: " + response.code());
                        Toast.makeText(ResumeMyInfoActivity.this, "Failed to load skills.", Toast.LENGTH_SHORT).show();
                        populateSkills(new ArrayList<>());
                    }
                    fetchUserExperiences();
                } else {
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SkillsApiResponse> call, @NonNull Throwable t) {
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to fetch skills (onFailure)", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Network error fetching skills: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    populateSkills(new ArrayList<>());
                    fetchUserExperiences();
                } else {
                    showLoading(false);
                }
            }
        });
    }

    private void fetchUserExperiences() {
        if (currentAccessToken == null) { showLoading(false); Log.e(TAG, "Access token is null, cannot fetch experiences."); return; }
        Log.d(TAG, "Fetching user experiences (/experiences)...");
        Call<ExperiencesApiResponse> experiencesCall = apiService.getCurrentUserExperiences("Bearer " + currentAccessToken);
        experiencesCall.enqueue(new Callback<ExperiencesApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExperiencesApiResponse> call, @NonNull Response<ExperiencesApiResponse> response) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<Experience> experiences = response.body().getData();
                        Log.d(TAG, "Successfully fetched experiences: " + (experiences != null ? experiences.size() : "null list") + " items.");
                        if (currentUser != null && experiences != null) {
                            currentUser.setExperiences(experiences);
                        }
                        populateWorkExperience(experiences);
                    } else {
                        Log.e(TAG, "Failed to fetch experiences. Code: " + response.code());
                        Toast.makeText(ResumeMyInfoActivity.this, "Failed to load work experiences.", Toast.LENGTH_SHORT).show();
                        populateWorkExperience(new ArrayList<>());
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ExperiencesApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to fetch experiences (onFailure)", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Network error fetching experiences: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    populateWorkExperience(new ArrayList<>());
                }
            }
        });
    }


    private void populateUIBasicInfo(User user) {
        if (user == null) {
            Log.w(TAG, "User object is null in populateUIBasicInfo.");
            tvCvFileNameResume.setText("N/A");
            tvCvUploadInfoResume.setText("Last updated: N/A");
            etAboutMeResumeContent.setText("No information provided.");
            populateWorkExperience(new ArrayList<>());
            populateSkills(new ArrayList<>());
            setAboutMeEditState(false);
            return;
        }
        String resumeName = "CV_Portfolio.pdf";
        String resumeUrlString = user.getResumeUrl();
        if (resumeUrlString != null && !resumeUrlString.isEmpty()) {
            if (resumeUrlString.startsWith("http://") || resumeUrlString.startsWith("https://")) {
                try {
                    URL parsedUrl = new URL(resumeUrlString);
                    String path = parsedUrl.getPath();
                    if (path != null && !path.isEmpty() && path.contains("/")) {
                        resumeName = path.substring(path.lastIndexOf('/') + 1);
                    }
                    if (resumeName.isEmpty() && !resumeUrlString.isEmpty()) {
                        resumeName = resumeUrlString;
                    }
                } catch (MalformedURLException e) {
                    Log.w(TAG, "Resume URL is not a valid URL, treating as filename: " + resumeUrlString, e);
                    resumeName = resumeUrlString;
                }
            } else {
                resumeName = resumeUrlString;
            }
        } else if (user.getName() != null && !user.getName().isEmpty()) {
            resumeName = user.getName().replace(" ", "_") + "_CV.pdf";
        }
        tvCvFileNameResume.setText(resumeName.isEmpty() ? "CV_Portfolio.pdf" : resumeName);
        String lastUpdatedText = "N/A";
        if (user.getUpdatedAt() != null) {
            lastUpdatedText = formatDateString(user.getUpdatedAt(), "dd MMM yy, HH:mm");
        }
        tvCvUploadInfoResume.setText("Last updated: " + lastUpdatedText);
        etAboutMeResumeContent.setText(user.getAboutMe() != null && !user.getAboutMe().isEmpty() ? user.getAboutMe() : "No information provided.");
        setAboutMeEditState(false);
    }

    private void populateWorkExperience(List<Experience> experiences) {
        if (llWorkExperienceItemsContainerResume == null) return;
        llWorkExperienceItemsContainerResume.removeAllViews();
        if (experiences == null || experiences.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView noData = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, llWorkExperienceItemsContainerResume, false);
            noData.setText("No work experience added yet.");
            noData.setPadding(0, (int) (16 * getResources().getDisplayMetrics().density), 0, (int) (16 * getResources().getDisplayMetrics().density));
            noData.setGravity(View.TEXT_ALIGNMENT_CENTER);
            llWorkExperienceItemsContainerResume.addView(noData);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Experience exp : experiences) {
            if (exp == null) continue;
            View itemView = inflater.inflate(R.layout.item_work_experience, llWorkExperienceItemsContainerResume, false);
            ImageView ivCompanyIcon = itemView.findViewById(R.id.iv_company_icon_exp_item);
            TextView tvJobTitle = itemView.findViewById(R.id.tv_job_title_exp_item);
            TextView tvCompanyNameExp = itemView.findViewById(R.id.tv_company_name_exp_item);
            TextView tvDurationExp = itemView.findViewById(R.id.tv_duration_exp_item);
            ivCompanyIcon.setImageResource(R.drawable.ic_building);
            tvJobTitle.setText(exp.getTitle() != null ? exp.getTitle() : "N/A");
            tvCompanyNameExp.setText(exp.getCompanyName() != null ? exp.getCompanyName() : "N/A");
            String startDateFormatted = formatDateString(exp.getStartDate(), "MMM yy");
            String endDateFormatted;
            if (exp.getEndDate() == null || exp.getEndDate().isEmpty()) {
                endDateFormatted = "Hiện tại";
            } else {
                endDateFormatted = formatDateString(exp.getEndDate(), "MMM yy");
            }
            tvDurationExp.setText(String.format("%s - %s", startDateFormatted, endDateFormatted));
            llWorkExperienceItemsContainerResume.addView(itemView);
        }
    }

    private void populateSkills(List<Skill> skills) {
        if (llSkillsItemsContainerResume == null) return;
        llSkillsItemsContainerResume.removeAllViews();
        if (skills == null || skills.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            TextView noData = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, llSkillsItemsContainerResume, false);
            noData.setText("No skills added yet.");
            noData.setPadding(0, (int) (16 * getResources().getDisplayMetrics().density), 0, (int) (16 * getResources().getDisplayMetrics().density));
            noData.setGravity(View.TEXT_ALIGNMENT_CENTER);
            llSkillsItemsContainerResume.addView(noData);
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Skill skill : skills) {
            if (skill == null) continue;
            View itemView = inflater.inflate(R.layout.item_skill, llSkillsItemsContainerResume, false);
            TextView tvSkillNameLevel = itemView.findViewById(R.id.tv_skill_name_level_item);
            String skillName = skill.getName() != null ? skill.getName() : "N/A";
            String rawLevel = skill.getLevel();
            String displayLevel = rawLevel;
            if (rawLevel != null) {
                if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.BEGINNER")) { displayLevel = "Beginner"; }
                else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.INTERMEDIATE")) { displayLevel = "Intermediate"; }
                else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.ADVANCE")) { displayLevel = "Advance"; }
                else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.EXPERT")) { displayLevel = "Expert"; } // Giữ lại nếu API có thể trả về
            } else { displayLevel = "N/A"; }
            tvSkillNameLevel.setText(String.format("%s - %s", skillName, displayLevel));
            llSkillsItemsContainerResume.addView(itemView);
        }
    }

    private String formatDateString(String dateString, String outputFormatPattern) {
        if (dateString == null || dateString.isEmpty()) return "N/A";
        try {
            SimpleDateFormat sdfFromApi;
            if (dateString.contains("T") && dateString.contains("Z")) {
                if (dateString.contains(".")) { sdfFromApi = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US); }
                else { sdfFromApi = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US); }
                sdfFromApi.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sdfFromApi = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            } else {
                Log.w(TAG, "Unrecognized date format for: " + dateString);
                SimpleDateFormat sdfAttempt = new SimpleDateFormat(outputFormatPattern, Locale.getDefault());
                Date parsedOriginalDate;
                try { parsedOriginalDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString); }
                catch (ParseException innerPe) { return dateString; }
                return sdfAttempt.format(parsedOriginalDate);
            }
            Calendar cal = Calendar.getInstance(); cal.setTime(sdfFromApi.parse(dateString));
            SimpleDateFormat sdfOutput = new SimpleDateFormat(outputFormatPattern, Locale.getDefault());
            return sdfOutput.format(cal.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date string: '" + dateString + "'", e); return dateString;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Error formatting date string (illegal argument): '" + dateString + "'", iae); return dateString;
        }
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs) {
        if (isFinishing() || isDestroyed()) return;
        if (response.code() == 401) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            if (prefs != null) { SharedPreferences.Editor editor = prefs.edit(); editor.clear().apply(); }
            navigateToLogin();
        } else {
            String errorMsg = "Error: " + response.code();
            if (response.errorBody() != null) {
                try {
                    String errorBodyString = response.errorBody().string();
                    if (errorBodyString.contains("\"message\"") || errorBodyString.contains("\"error\"")) {
                        try {
                            org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                            errorMsg += " - " + errorJson.optString("message", errorJson.optString("error", "Unknown server error"));
                        } catch (org.json.JSONException e) { errorMsg += " - (Failed to parse error details as JSON)"; Log.w(TAG, "Error body was not valid JSON: " + errorBodyString); }
                    } else { errorMsg += " - " + errorBodyString; }
                } catch (IOException e) { Log.e(TAG, "Error parsing error body for API", e); errorMsg += " - (Error reading server response)"; }
            } else if (response.message() != null && !response.message().isEmpty()){ errorMsg += " - " + response.message(); }
            else { errorMsg += " - (Unknown error, no message in response)"; }
            Log.e(TAG, "API Call Error: " + errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void navigateToLogin() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

