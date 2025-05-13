package com.example.foodorderapp.features.jobs.ui.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter;
import com.example.foodorderapp.features.main.ui.activity.MainActivity;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.ApplicationResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApplyJobActivity extends AppCompatActivity {

    private static final String TAG = "ApplyJobActivity";
    private ImageView ivCompanyLogoApply;
    private TextView tvCompanyNameApply, tvJobTitleApply, tvUploadHint;
    private ImageButton btnBackApply;
    private LinearLayout layoutUploadResume;
    private EditText etPhoneNumber, etCoverLetter;
    private Button btnApplyNow;
    private ProgressBar progressBarApplyJob;

    private Job currentJob;
    private Uri selectedFileUri;
    private ApiService apiService;
    private String currentAccessToken;


    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        String fileName = getFileNameFromUri(selectedFileUri);
                        if(tvUploadHint != null) {
                            tvUploadHint.setText(getString(R.string.file_selected_label, fileName));
                            tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.setting_icon_tint));
                        }
                        Toast.makeText(this, "Đã chọn file: " + fileName, Toast.LENGTH_SHORT).show();
                    } else {
                        resetUploadHint();
                    }
                } else {
                    resetUploadHint();
                    selectedFileUri = null;
                    Toast.makeText(this, "Chưa chọn file nào", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);

        initApiService();
        loadAuthToken();
        findViews();
        getIntentData();

        if (currentJob != null) {
            populateJobSummary();
        } else {
            Toast.makeText(this, "Không thể tải thông tin công việc.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        setupClickListeners();
    }

    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            finish();
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

    private void loadAuthToken() {
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);
        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void findViews() {
        ivCompanyLogoApply = findViewById(R.id.ivCompanyLogoApply);
        tvCompanyNameApply = findViewById(R.id.tvCompanyNameApply);
        tvJobTitleApply = findViewById(R.id.tvJobTitleApply);
        btnBackApply = findViewById(R.id.btnBackApply);
        layoutUploadResume = findViewById(R.id.layoutUploadResume);
        etPhoneNumber = findViewById(R.id.etPhoneNumber); // Mặc dù không gửi, vẫn giữ để không lỗi UI
        etCoverLetter = findViewById(R.id.etCoverLetter);
        btnApplyNow = findViewById(R.id.btnApplyNow);
        tvUploadHint = findViewById(R.id.tvUploadHint);
        progressBarApplyJob = findViewById(R.id.progressBarApplyJob);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String jobDetailKey = JobAdapter.JOB_DETAIL_KEY;
        if (intent != null && intent.hasExtra(jobDetailKey)) {
            currentJob = (Job) intent.getSerializableExtra(jobDetailKey);
        }
    }

    private void populateJobSummary() {
        String logoUrl = currentJob.getCompany() != null ? currentJob.getCompany().getLogoUrl() : null;
        if (logoUrl != null && !logoUrl.toLowerCase().startsWith("http")) {
            String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
            logoUrl = imageBaseUrl + (logoUrl.startsWith("/") ? "" : "/") + logoUrl;
        }

        Glide.with(this)
                .load(logoUrl)
                .placeholder(R.drawable.ic_company_logo_placeholder)
                .error(R.drawable.ic_company_logo_placeholder)
                .into(ivCompanyLogoApply);

        tvCompanyNameApply.setText(currentJob.getCompany() != null ? currentJob.getCompany().getName() : "");
        tvJobTitleApply.setText(currentJob.getTitle());
    }


    private void setupClickListeners() {
        btnBackApply.setOnClickListener(v -> finish());

        layoutUploadResume.setOnClickListener(v -> openFilePicker());

        btnApplyNow.setOnClickListener(v -> {
            if (currentJob == null) {
                Toast.makeText(this, "Lỗi thông tin công việc.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedFileUri == null) {
                Toast.makeText(this, "Vui lòng tải lên CV của bạn", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentAccessToken == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để ứng tuyển.", Toast.LENGTH_SHORT).show();
                navigateToLogin();
                return;
            }

            submitApplication();
        });
    }

    private void submitApplication() {
        progressBarApplyJob.setVisibility(android.view.View.VISIBLE);
        btnApplyNow.setEnabled(false);

        String jobIdStr = String.valueOf(currentJob.getId());
        String coverLetterStr = etCoverLetter.getText().toString().trim();

        RequestBody jobIdBody = RequestBody.create(MediaType.parse("text/plain"), jobIdStr);
        RequestBody coverLetterBody = RequestBody.create(MediaType.parse("text/plain"), coverLetterStr);

        MultipartBody.Part filePart = prepareFilePart("file", selectedFileUri);
        if (filePart == null) {
            progressBarApplyJob.setVisibility(android.view.View.GONE);
            btnApplyNow.setEnabled(true);
            Toast.makeText(this, "Không thể chuẩn bị tệp CV.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<ApplicationResponse> call = apiService.applyForJob("Bearer " + currentAccessToken, jobIdBody, coverLetterBody, filePart);
        call.enqueue(new Callback<ApplicationResponse>() {
            @Override
            public void onResponse(Call<ApplicationResponse> call, Response<ApplicationResponse> response) {
                progressBarApplyJob.setVisibility(android.view.View.GONE);
                btnApplyNow.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showSuccessDialog();
                } else {
                    String errorMsg = "Lỗi khi nộp đơn.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " Mã lỗi: " + response.code() + " - " + response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Lỗi đọc errorBody", e);
                        }
                    } else {
                        errorMsg += " Mã lỗi: " + response.code();
                    }
                    Toast.makeText(ApplyJobActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "API Error: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApplicationResponse> call, Throwable t) {
                progressBarApplyJob.setVisibility(android.view.View.GONE);
                btnApplyNow.setEnabled(true);
                Log.e(TAG, "Lỗi mạng khi nộp đơn: " + t.getMessage(), t);
                Toast.makeText(ApplyJobActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        ContentResolver contentResolver = getContentResolver();
        String fileName = getFileNameFromUri(fileUri);
        try {
            InputStream inputStream = contentResolver.openInputStream(fileUri);
            if (inputStream == null) {
                Toast.makeText(this, "Không thể mở tệp.", Toast.LENGTH_SHORT).show();
                return null;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] fileBytes = byteBuffer.toByteArray();
            inputStream.close();

            String mimeType = contentResolver.getType(fileUri);
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "application/pdf"; // Mặc định là PDF nếu không xác định được
            }
            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), fileBytes);
            return MultipartBody.Part.createFormData(partName, fileName, requestFile);
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi chuẩn bị tệp: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi chuẩn bị tệp.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_apply_success);

        TextView tvSuccessMessage = dialog.findViewById(R.id.tvSuccessMessage);
        Button btnContinueExplore = dialog.findViewById(R.id.btnContinueExplore);

        String successMsg = getString(R.string.success_message_format,
                currentJob.getCompany() != null ? currentJob.getCompany().getName() : "",
                currentJob.getTitle());
        tvSuccessMessage.setText(successMsg);

        btnContinueExplore.setOnClickListener(v -> {
            dialog.dismiss();
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf"); // Chỉ cho phép chọn file PDF
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Chọn CV/Resume (PDF)"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Vui lòng cài đặt trình quản lý file.", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetUploadHint() {
        if(tvUploadHint != null) {
            tvUploadHint.setText(getString(R.string.upload_resume_hint));
            tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.grey));
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lấy tên tệp từ ContentResolver", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (fileName == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    fileName = path.substring(cut + 1);
                } else {
                    fileName = path;
                }
            }
        }
        return fileName != null ? fileName : "unknown_cv.pdf";
    }
}
