package com.example.foodorderapp.features.profile.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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
import com.google.gson.Gson; // THÊM IMPORT NÀY ĐỂ LOG RESPONSE BODY

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import okhttp3.MultipartBody;
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
    private TextView tvToolbarTitleResumeInfo, tvCvFileNameResume, tvCvUploadInfoResume;

    private CardView cardCvContainer;
    private TextView tvUploadChangeCvLink;
    private ImageView ivDeleteCv;

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
    private ActivityResultLauncher<Intent> pickPdfLauncher;


    private boolean isAboutMeEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_my_info);

        initViews();
        initApiService();
        setupToolbar();
        setupLaunchers();
        setupClickListeners();
        fetchUserProfileData();
    }

    private void initViews() {
        toolbarResumeInfo = findViewById(R.id.toolbar_resume_info);
        tvToolbarTitleResumeInfo = findViewById(R.id.tv_toolbar_title_resume_info);

        cardCvContainer = findViewById(R.id.card_cv_container);
        tvCvFileNameResume = findViewById(R.id.tv_cv_file_name_resume);
        tvCvUploadInfoResume = findViewById(R.id.tv_cv_upload_info_resume);
        tvUploadChangeCvLink = findViewById(R.id.tv_upload_change_cv_link);
        ivDeleteCv = findViewById(R.id.iv_delete_cv);

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

    private void setupLaunchers() {
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

        pickPdfLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedPdfUri = result.getData().getData();
                        if (selectedPdfUri != null) {
                            Log.d(TAG, "PDF selected: " + selectedPdfUri.toString());
                            uploadCvFile(selectedPdfUri);
                        } else {
                            Toast.makeText(this, "Không thể lấy URI của tệp PDF.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "PDF selection cancelled or failed.");
                    }
                }
        );
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
                Intent resultIntent = new Intent();
                resultIntent.putExtra("profileDataChanged", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isAboutMeEditing) {
            showDiscardChangesDialog();
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("profileDataChanged", true);
            setResult(Activity.RESULT_OK, resultIntent);
            super.onBackPressed();
        }
    }

    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
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
            if (imm != null && getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
            tilAboutMeResumeContent.setBoxStrokeWidth(0);
            etAboutMeResumeContent.setBackgroundResource(R.drawable.bg_container_card);
        }
    }


    private void setupClickListeners() {
        tvUploadChangeCvLink.setOnClickListener(v -> openPdfPicker());
        cardCvContainer.setOnClickListener(v -> viewCv());
        ivDeleteCv.setOnClickListener(v -> confirmDeleteCv());

        ivEditAboutMeResume.setOnClickListener(v -> {
            if (isAboutMeEditing) {
                saveAboutMeChanges();
            } else {
                setAboutMeEditState(true);
            }
        });

        ivAddWorkExperienceResume.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getId() != 0) {
                Log.d(TAG, "Add Work Experience button clicked. User ID: " + currentUser.getId());
                Intent intent = new Intent(ResumeMyInfoActivity.this, EditExperienceActivity.class);
                intent.putExtra(EditExperienceActivity.EXTRA_USER_ID, currentUser.getId());
                editExperienceLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Không thể tải ID người dùng để thêm kinh nghiệm.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cannot add experience, currentUser is null or ID is invalid.");
            }
        });

        ivAddSkillResume.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getId() != 0) {
                Log.d(TAG, "Add Skill button clicked. User ID: " + currentUser.getId());
                Intent intent = new Intent(ResumeMyInfoActivity.this, EditSkillActivity.class);
                intent.putExtra(EditSkillActivity.EXTRA_USER_ID, currentUser.getId());
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

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            pickPdfLauncher.launch(Intent.createChooser(intent, "Chọn tệp PDF CV"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng quản lý tệp.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("Range")
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        if (uri == null) return "default_cv.pdf";
        ContentResolver resolver = getContentResolver();
        if (resolver == null) return "default_cv.pdf";

        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name from content URI: " + uri.toString(), e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            if (fileName != null) {
                int cut = fileName.lastIndexOf('/');
                if (cut != -1) {
                    fileName = fileName.substring(cut + 1);
                }
            }
        }
        return (fileName == null || fileName.isEmpty()) ? "cv_upload.pdf" : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }


    private void uploadCvFile(Uri pdfUri) {
        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        showLoading(true);

        String fileName = getFileNameFromUri(pdfUri);
        ContentResolver resolver = getContentResolver();
        if (resolver == null) {
            Toast.makeText(this, "Không thể truy cập tệp.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        RequestBody requestFile;
        File tempFile = null; // Khai báo ở đây để có thể xóa trong finally
        try {
            InputStream inputStream = resolver.openInputStream(pdfUri);
            if (inputStream == null) {
                throw new FileNotFoundException("InputStream is null for URI: " + pdfUri);
            }
            tempFile = new File(getCacheDir(), "temp_cv_upload_" + System.currentTimeMillis() + ".pdf");
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while((read = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            String mimeType = resolver.getType(pdfUri);
            if (mimeType == null) {
                mimeType = "application/pdf"; // Fallback MIME type
            }
            requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);

        } catch (IOException e) {
            Log.e(TAG, "Error creating RequestBody from Uri", e);
            Toast.makeText(this, "Lỗi khi đọc tệp PDF.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            if (tempFile != null && tempFile.exists()) { // Xóa file tạm nếu có lỗi
                tempFile.delete();
            }
            return;
        }

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", fileName, requestFile);
        final File finalTempFile = tempFile; // Để truy cập trong inner class

        apiService.uploadPortfolio("Bearer " + currentAccessToken, body).enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                showLoading(false);
                if (finalTempFile != null && finalTempFile.exists()) { // Xóa file tạm sau khi upload
                    finalTempFile.delete();
                }
                // ----- LOGGING CHI TIẾT -----
                Log.d(TAG, "Upload_onResponse: Code = " + response.code() + ", isSuccessful = " + response.isSuccessful());
                if (response.body() != null) {
                    Log.d(TAG, "Upload_onResponse: Body as JSON = " + new Gson().toJson(response.body()));
                    Log.d(TAG, "Upload_onResponse: Body Success = " + response.body().isSuccess() + ", Message = " + response.body().getMessage());
                    if (response.body().getData() != null) {
                        Log.d(TAG, "Upload_onResponse: ResumeUrl from response.body().getData() = " + response.body().getData().getResumeUrl());
                    } else {
                        Log.d(TAG, "Upload_onResponse: response.body().getData() is NULL");
                    }
                } else {
                    Log.d(TAG, "Upload_onResponse: response.body() is NULL");
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Upload_onResponse: ErrorBody = " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
                // ----- KẾT THÚC LOGGING -----

                if (!isFinishing()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ResumeMyInfoActivity.this, "CV đã được tải lên!", Toast.LENGTH_SHORT).show();
                        currentUser = response.body().getData();
                        if (currentUser != null) {
                            Log.d(TAG, "Upload_Success_UI_Update: currentUser.getResumeUrl() = " + currentUser.getResumeUrl());
                            populateCvSection(currentUser);
                        } else {
                            Log.d(TAG, "Upload_Success_UI_Update: currentUser is null after response.body().getData(). Fetching profile again.");
                            fetchUserProfileData();
                        }
                    } else {
                        // Giữ nguyên phần xử lý lỗi đã có trước đó
                        String errorMessage = "Lỗi tải lên CV.";
                        if (response.body() != null && response.body().getMessage() != null && !response.body().getMessage().equals("OK")) { // API có thể trả về message "OK" dù statusCode không phải 2xx
                            errorMessage += " (" + response.body().getMessage() + ")";
                        } else if (response.errorBody() != null) {
                            try {
                                String errorStr = response.errorBody().string();
                                errorMessage += " Code: " + response.code() + " - " + errorStr;
                                Log.e(TAG, "Upload CV Error Body: " + errorStr);
                            } catch (IOException e) { Log.e(TAG, "Error reading error body for CV upload", e); errorMessage += " Code: " + response.code();}
                        } else {
                            errorMessage += " Code: " + response.code();
                        }
                        Toast.makeText(ResumeMyInfoActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Upload CV failed: " + errorMessage);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (finalTempFile != null && finalTempFile.exists()) { // Xóa file tạm
                    finalTempFile.delete();
                }
                if (!isFinishing()) {
                    Log.e(TAG, "Lỗi mạng khi tải lên CV", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void viewCv() {
        if (currentUser == null || TextUtils.isEmpty(currentUser.getResumeUrl())) {
            Toast.makeText(this, "Chưa có CV nào được tải lên để xem.", Toast.LENGTH_SHORT).show();
            return;
        }

        String resumeUrl = currentUser.getResumeUrl();
        if (!resumeUrl.toLowerCase().startsWith("http://") && !resumeUrl.toLowerCase().startsWith("https://")) {
            String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
            resumeUrl = imageBaseUrl + (resumeUrl.startsWith("/") ? "" : "/") + resumeUrl;
        }

        Log.d(TAG, "Attempting to view CV at URL: " + resumeUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(resumeUrl), "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(intent, "Chọn ứng dụng để xem CV"));
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "No PDF viewer found", e);
            Toast.makeText(this, "Không tìm thấy ứng dụng nào để mở tệp PDF.", Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDeleteCv() {
        if (currentUser == null || TextUtils.isEmpty(currentUser.getResumeUrl())) {
            Toast.makeText(this, "Không có CV để xóa.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Xóa CV")
                .setMessage("Bạn có chắc chắn muốn xóa CV hiện tại không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteCvFromServer())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteCvFromServer() {
        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        showLoading(true);
        apiService.deletePortfolio("Bearer " + currentAccessToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                Log.d(TAG, "Delete_onResponse: Code = " + response.code() + ", isSuccessful = " + response.isSuccessful());
                if (!isFinishing()) {
                    if (response.isSuccessful()) { // Đối với Void, isSuccessful thường là 200, 204
                        Toast.makeText(ResumeMyInfoActivity.this, "CV đã được xóa!", Toast.LENGTH_SHORT).show();
                        if (currentUser != null) {
                            currentUser.setResumeUrl(null);
                        }
                        populateCvSection(currentUser); // Cập nhật UI
                        // fetchUserProfileData(); // Cân nhắc fetch lại toàn bộ profile để đảm bảo đồng bộ
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                            Log.e(TAG, "Lỗi xóa CV: " + response.code() + " - " + errorBody);
                            Toast.makeText(ResumeMyInfoActivity.this, "Lỗi xóa CV: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc error body khi xóa CV", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing()) {
                    Log.e(TAG, "Lỗi mạng khi xóa CV", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
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
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
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
                            Log.d(TAG, "User Profile Fetched. User ID: " + currentUser.getId() + ", AboutMe: '" + currentUser.getAboutMe() + "', ResumeURL: '" + currentUser.getResumeUrl() + "'. Fetching skills...");
                            populateUIBasicInfo(currentUser);
                            fetchUserSkills();
                        } else {
                            showLoading(false);
                            Log.e(TAG, "Fetched user data is null from API response data (profile/me).");
                            Toast.makeText(ResumeMyInfoActivity.this, "Lỗi tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ResumeMyInfoActivity.this, "Lỗi mạng khi tải hồ sơ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ResumeMyInfoActivity.this, "Lỗi tải kỹ năng.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ResumeMyInfoActivity.this, "Lỗi mạng khi tải kỹ năng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(ResumeMyInfoActivity.this, "Lỗi tải kinh nghiệm làm việc.", Toast.LENGTH_SHORT).show();
                        populateWorkExperience(new ArrayList<>());
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<ExperiencesApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to fetch experiences (onFailure)", t);
                    Toast.makeText(ResumeMyInfoActivity.this, "Lỗi mạng khi tải kinh nghiệm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    populateWorkExperience(new ArrayList<>());
                }
            }
        });
    }


    private void populateUIBasicInfo(User user) {
        if (user == null) {
            Log.w(TAG, "User object is null in populateUIBasicInfo.");
            etAboutMeResumeContent.setText("Không có thông tin.");
            setAboutMeEditState(false);
            populateCvSection(null);
            return;
        }

        etAboutMeResumeContent.setText(user.getAboutMe() != null && !user.getAboutMe().isEmpty() ? user.getAboutMe() : "Chưa có thông tin giới thiệu.");
        setAboutMeEditState(false);
        populateCvSection(user);
    }

    private void populateCvSection(User userWithCvInfo) {
        // ----- LOGGING CHI TIẾT -----
        if (userWithCvInfo != null) {
            Log.d(TAG, "populateCvSection_Entry: Received resumeUrl = " + userWithCvInfo.getResumeUrl() + ", User name: " + userWithCvInfo.getName());
        } else {
            Log.d(TAG, "populateCvSection_Entry: userWithCvInfo is null");
        }
        // ----- KẾT THÚC LOGGING -----

        if (userWithCvInfo == null || TextUtils.isEmpty(userWithCvInfo.getResumeUrl())) {
            tvCvFileNameResume.setText("Chưa có CV nào");
            tvCvUploadInfoResume.setVisibility(View.GONE);
            tvUploadChangeCvLink.setText("Tải lên CV");
            ivDeleteCv.setVisibility(View.GONE);
            Log.d(TAG, "populateCvSection_Display: No CV. tvUploadChangeCvLink set to 'Tải lên CV', ivDeleteCv GONE.");
        } else {
            String resumeUrlString = userWithCvInfo.getResumeUrl();
            String resumeName = "CV_Portfolio.pdf";
            if (resumeUrlString != null && !resumeUrlString.isEmpty()) {
                if (resumeUrlString.startsWith("http://") || resumeUrlString.startsWith("https://")) {
                    try {
                        URL parsedUrl = new URL(resumeUrlString);
                        String path = parsedUrl.getPath();
                        if (path != null && !path.isEmpty() && path.contains("/")) {
                            resumeName = path.substring(path.lastIndexOf('/') + 1);
                        }
                    } catch (MalformedURLException e) {
                        Log.w(TAG, "Resume URL is not a valid URL, using filename from URL: " + resumeUrlString, e);
                        if (resumeUrlString.contains("/")) {
                            resumeName = resumeUrlString.substring(resumeUrlString.lastIndexOf('/') + 1);
                        } else {
                            resumeName = resumeUrlString;
                        }
                    }
                } else {
                    resumeName = resumeUrlString;
                }
            }
            // ----- LOGGING CHI TIẾT -----
            Log.d(TAG, "populateCvSection_Display: Setting tvCvFileNameResume to: " + resumeName);
            Log.d(TAG, "populateCvSection_Display: Setting tvUploadChangeCvLink text to 'Thay đổi CV'");
            Log.d(TAG, "populateCvSection_Display: Setting ivDeleteCv visibility to VISIBLE");
            // ----- KẾT THÚC LOGGING -----

            tvCvFileNameResume.setText(resumeName.isEmpty() ? "Không rõ tên CV" : resumeName);

            String lastUpdatedText = "N/A";
            if (userWithCvInfo.getUpdatedAt() != null) {
                lastUpdatedText = formatDateString(userWithCvInfo.getUpdatedAt(), "dd MMM yy, HH:mm");
            }
            tvCvUploadInfoResume.setText("Cập nhật lần cuối: " + lastUpdatedText);
            tvCvUploadInfoResume.setVisibility(View.VISIBLE);
            tvUploadChangeCvLink.setText("Thay đổi CV");
            ivDeleteCv.setVisibility(View.VISIBLE);
        }
    }


    private void populateWorkExperience(List<Experience> experiences) {
        if (llWorkExperienceItemsContainerResume == null) return;
        llWorkExperienceItemsContainerResume.removeAllViews();
        if (experiences == null || experiences.isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View noDataView = inflater.inflate(R.layout.item_no_data_placeholder, llWorkExperienceItemsContainerResume, false);
            TextView noDataText = noDataView.findViewById(R.id.tv_no_data_message);
            noDataText.setText("Chưa có kinh nghiệm làm việc nào.");
            llWorkExperienceItemsContainerResume.addView(noDataView);
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
            View noDataView = inflater.inflate(R.layout.item_no_data_placeholder, llSkillsItemsContainerResume, false);
            TextView noDataText = noDataView.findViewById(R.id.tv_no_data_message);
            noDataText.setText("Chưa có kỹ năng nào.");
            llSkillsItemsContainerResume.addView(noDataView);
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
                else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.EXPERT")) { displayLevel = "Expert"; }
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
                try {
                    SimpleDateFormat altFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date altDate = altFormat.parse(dateString);
                    if (altDate != null) {
                        return new SimpleDateFormat(outputFormatPattern, Locale.getDefault()).format(altDate);
                    }
                } catch (ParseException pe) {
                    // Fallback
                }
                return dateString;
            }
            Date parsedDate = sdfFromApi.parse(dateString);
            if (parsedDate == null) return dateString;
            SimpleDateFormat sdfOutput = new SimpleDateFormat(outputFormatPattern, Locale.getDefault());
            return sdfOutput.format(parsedDate);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date string: '" + dateString + "' with pattern. ", e); return dateString;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Error formatting date string (illegal argument): '" + dateString + "'", iae); return dateString;
        }
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs) {
        if (isFinishing() || isDestroyed()) return;
        if (response.code() == 401) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            if (prefs != null) { SharedPreferences.Editor editor = prefs.edit(); editor.clear().apply(); }
            navigateToLogin();
        } else {
            String errorMsg = "Lỗi: " + response.code();
            if (response.errorBody() != null) {
                try {
                    String errorBodyString = response.errorBody().string();
                    if (errorBodyString.contains("\"message\"") || errorBodyString.contains("\"error\"")) {
                        try {
                            org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyString);
                            errorMsg += " - " + errorJson.optString("message", errorJson.optString("error", "Lỗi không xác định từ máy chủ"));
                        } catch (org.json.JSONException e) { errorMsg += " - (Không thể phân tích chi tiết lỗi)"; Log.w(TAG, "Error body was not valid JSON: " + errorBodyString); }
                    } else { errorMsg += " - " + errorBodyString; }
                } catch (IOException e) { Log.e(TAG, "Lỗi đọc error body API", e); errorMsg += " - (Lỗi đọc phản hồi từ máy chủ)"; }
            } else if (response.message() != null && !response.message().isEmpty()){ errorMsg += " - " + response.message(); }
            else { errorMsg += " - (Lỗi không xác định, không có thông báo trong phản hồi)"; }
            Log.e(TAG, "Lỗi API: " + errorMsg);
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