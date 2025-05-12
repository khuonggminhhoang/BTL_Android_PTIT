package com.example.foodorderapp.features.profile.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.request.CreateExperienceRequest; // Import mới
import com.example.foodorderapp.network.request.UpdateExperienceRequest;
import com.example.foodorderapp.network.response.ExperienceDetailApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditExperienceActivity extends AppCompatActivity {

    private static final String TAG = "EditExperienceActivity";
    public static final String EXTRA_EXPERIENCE_ID = "EXPERIENCE_ID";
    public static final String EXTRA_USER_ID = "USER_ID_FOR_NEW_EXPERIENCE"; // Extra mới

    private Toolbar toolbarEditExperience;
    private TextInputLayout tilExperienceTitle, tilExperienceCompanyName, tilExperienceStartDate, tilExperienceEndDate, tilExperienceDescription;
    private TextInputEditText etExperienceTitle, etExperienceCompanyName, etExperienceStartDate, etExperienceEndDate, etExperienceDescription;
    private CheckBox cbCurrentJob;
    private Button btnSaveExperience;
    private ProgressBar progressBarEditExperience;

    private ApiService apiService;
    private String currentAccessToken;
    // private Experience currentExperience; // Không cần thiết nếu chỉ fetch khi edit
    private int experienceIdToEdit = -1;
    private int currentUserId = -1; // Lưu userId khi thêm mới
    private boolean isEditMode = false;

    private SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_experience);

        startDateCalendar = Calendar.getInstance();
        clearCalendarTime(startDateCalendar);
        endDateCalendar = Calendar.getInstance();
        clearCalendarTime(endDateCalendar);
        apiDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        initViews();
        setupToolbar();
        initApiService();

        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);

        if (currentAccessToken == null) {
            Toast.makeText(this, "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_EXPERIENCE_ID)) { // Chế độ Sửa
            experienceIdToEdit = intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1);
            if (experienceIdToEdit != -1) {
                isEditMode = true;
                toolbarEditExperience.setTitle("Chỉnh sửa Kinh nghiệm");
                btnSaveExperience.setText("Lưu thay đổi");
                fetchExperienceDetails(experienceIdToEdit);
            } else {
                Toast.makeText(this, "ID Kinh nghiệm không hợp lệ để sửa.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (intent != null && intent.hasExtra(EXTRA_USER_ID)) { // Chế độ Thêm mới
            isEditMode = false;
            currentUserId = intent.getIntExtra(EXTRA_USER_ID, -1);
            if (currentUserId == -1) {
                Toast.makeText(this, "ID Người dùng không hợp lệ để thêm kinh nghiệm.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbarEditExperience.setTitle("Thêm Kinh nghiệm mới");
            btnSaveExperience.setText("Thêm Kinh nghiệm");
            if (cbCurrentJob.isChecked()) {
                etExperienceEndDate.setText("");
                etExperienceEndDate.setEnabled(false);
                tilExperienceEndDate.setEnabled(false);
            }
        } else {
            Toast.makeText(this, "Không đủ thông tin để thực hiện.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupDatePickers();
        btnSaveExperience.setOnClickListener(v -> saveOrUpdateExperience());
        cbCurrentJob.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etExperienceEndDate.setEnabled(!isChecked);
            tilExperienceEndDate.setEnabled(!isChecked);
            if (isChecked) {
                etExperienceEndDate.setText("");
            }
        });
    }

    private void clearCalendarTime(Calendar cal) {
        // ... (giữ nguyên)
        if (cal == null) return;
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void initViews() {
        // ... (giữ nguyên)
        toolbarEditExperience = findViewById(R.id.toolbar_edit_experience);
        tilExperienceTitle = findViewById(R.id.til_experience_title);
        etExperienceTitle = findViewById(R.id.et_experience_title);
        tilExperienceCompanyName = findViewById(R.id.til_experience_company_name);
        etExperienceCompanyName = findViewById(R.id.et_experience_company_name);
        tilExperienceStartDate = findViewById(R.id.til_experience_start_date);
        etExperienceStartDate = findViewById(R.id.et_experience_start_date);
        tilExperienceEndDate = findViewById(R.id.til_experience_end_date);
        etExperienceEndDate = findViewById(R.id.et_experience_end_date);
        cbCurrentJob = findViewById(R.id.cb_experience_current_job);
        tilExperienceDescription = findViewById(R.id.til_experience_description);
        etExperienceDescription = findViewById(R.id.et_experience_description);
        btnSaveExperience = findViewById(R.id.btn_save_experience);
        progressBarEditExperience = findViewById(R.id.progressBar_edit_experience);
    }

    private void setupToolbar() {
        // ... (giữ nguyên)
        setSupportActionBar(toolbarEditExperience);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarEditExperience.setNavigationOnClickListener(v -> finish());
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

    private void setupDatePickers() {
        // ... (giữ nguyên)
        etExperienceStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        etExperienceEndDate.setOnClickListener(v -> {
            if (!cbCurrentJob.isChecked()) {
                showDatePickerDialog(false);
            }
        });
    }

    private void showDatePickerDialog(final boolean isStartDatePicker) {
        // ... (giữ nguyên như phiên bản trước)
        Calendar calendarToInitializeDialog = Calendar.getInstance();

        if (isStartDatePicker) {
            if (!etExperienceStartDate.getText().toString().isEmpty()) {
                try {
                    Date d = viewDateFormat.parse(etExperienceStartDate.getText().toString());
                    if (d != null) calendarToInitializeDialog.setTime(d);
                } catch (ParseException e) { calendarToInitializeDialog.setTimeInMillis(startDateCalendar.getTimeInMillis()); }
            } else {
                calendarToInitializeDialog.setTimeInMillis(startDateCalendar.getTimeInMillis());
            }
        } else {
            if (!etExperienceEndDate.getText().toString().isEmpty()) {
                try {
                    Date d = viewDateFormat.parse(etExperienceEndDate.getText().toString());
                    if (d != null) calendarToInitializeDialog.setTime(d);
                } catch (ParseException e) { calendarToInitializeDialog.setTimeInMillis(endDateCalendar.getTimeInMillis());}
            } else if (!etExperienceStartDate.getText().toString().isEmpty()) {
                try {
                    Date d = viewDateFormat.parse(etExperienceStartDate.getText().toString());
                    if (d != null) calendarToInitializeDialog.setTime(d);
                } catch (ParseException e) { calendarToInitializeDialog.setTimeInMillis(endDateCalendar.getTimeInMillis()); }
            } else {
                calendarToInitializeDialog.setTimeInMillis(endDateCalendar.getTimeInMillis());
            }
        }
        clearCalendarTime(calendarToInitializeDialog);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, monthOfYear, dayOfMonth);
                    clearCalendarTime(selectedDate);

                    if (isStartDatePicker) {
                        startDateCalendar.setTime(selectedDate.getTime());
                        etExperienceStartDate.setText(viewDateFormat.format(selectedDate.getTime()));
                        tilExperienceStartDate.setError(null);
                        if (!cbCurrentJob.isChecked() && !etExperienceEndDate.getText().toString().isEmpty()) {
                            try {
                                Date currentEndDate = viewDateFormat.parse(etExperienceEndDate.getText().toString());
                                if (currentEndDate != null && currentEndDate.before(selectedDate.getTime())) {
                                    etExperienceEndDate.setText("");
                                    Toast.makeText(EditExperienceActivity.this, "Ngày kết thúc đã được xóa.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ParseException e) {/* ignore */}
                        }
                    } else {
                        endDateCalendar.setTime(selectedDate.getTime());
                        etExperienceEndDate.setText(viewDateFormat.format(selectedDate.getTime()));
                        tilExperienceEndDate.setError(null);
                    }
                },
                calendarToInitializeDialog.get(Calendar.YEAR),
                calendarToInitializeDialog.get(Calendar.MONTH),
                calendarToInitializeDialog.get(Calendar.DAY_OF_MONTH));

        if (!isStartDatePicker) {
            if (!etExperienceStartDate.getText().toString().isEmpty()) {
                try {
                    Date sDate = viewDateFormat.parse(etExperienceStartDate.getText().toString());
                    if (sDate != null) datePickerDialog.getDatePicker().setMinDate(sDate.getTime());
                } catch (ParseException e) { /* ignore */ }
            }
        } else {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            if (!cbCurrentJob.isChecked() && !etExperienceEndDate.getText().toString().isEmpty()) {
                try {
                    Date eDate = viewDateFormat.parse(etExperienceEndDate.getText().toString());
                    if (eDate != null) datePickerDialog.getDatePicker().setMaxDate(eDate.getTime());
                } catch (ParseException e) { /* ignore */ }
            }
        }
        datePickerDialog.show();
    }

    private void showLoading(boolean isLoading) {
        // ... (giữ nguyên)
        progressBarEditExperience.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveExperience.setEnabled(!isLoading);
        etExperienceTitle.setEnabled(!isLoading);
        etExperienceCompanyName.setEnabled(!isLoading);
        etExperienceStartDate.setEnabled(!isLoading);
        etExperienceEndDate.setEnabled(!isLoading && !cbCurrentJob.isChecked());
        cbCurrentJob.setEnabled(!isLoading);
        etExperienceDescription.setEnabled(!isLoading);
    }

    private void fetchExperienceDetails(int experienceId) {
        // ... (giữ nguyên)
        showLoading(true);
        Log.d(TAG, "Fetching details for experience ID: " + experienceId);
        apiService.getExperienceDetail("Bearer " + currentAccessToken, experienceId).enqueue(new Callback<ExperienceDetailApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Response<ExperienceDetailApiResponse> response) {
                showLoading(false);
                if (!isFinishing()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Experience expToEdit = response.body().getData();
                        if (expToEdit != null) {
                            populateExperienceDataToFields(expToEdit);
                        } else {
                            Toast.makeText(EditExperienceActivity.this, "Không tìm thấy chi tiết kinh nghiệm.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(EditExperienceActivity.this, "Lỗi khi tải chi tiết kinh nghiệm. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing()) {
                    Toast.makeText(EditExperienceActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateExperienceDataToFields(Experience exp) {
        // ... (giữ nguyên như phiên bản trước, đảm bảo parse ngày đúng)
        etExperienceTitle.setText(exp.getTitle());
        etExperienceCompanyName.setText(exp.getCompanyName());
        etExperienceDescription.setText(exp.getDescription());

        SimpleDateFormat sourceDateFormat;
        if (exp.getStartDate() != null && !exp.getStartDate().isEmpty()) {
            if (exp.getStartDate().contains("T")) {
                sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            }
            try {
                Date date = sourceDateFormat.parse(exp.getStartDate());
                if (date != null) {
                    startDateCalendar.setTime(date);
                    clearCalendarTime(startDateCalendar);
                    etExperienceStartDate.setText(viewDateFormat.format(date));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing start date from API: " + exp.getStartDate(), e);
                etExperienceStartDate.setText(exp.getStartDate());
            }
        } else {
            etExperienceStartDate.setText("");
            startDateCalendar.setTimeInMillis(System.currentTimeMillis());
            clearCalendarTime(startDateCalendar);
        }

        if (exp.getEndDate() != null && !exp.getEndDate().isEmpty()) {
            cbCurrentJob.setChecked(false);
            etExperienceEndDate.setEnabled(true);
            tilExperienceEndDate.setEnabled(true);
            if (exp.getEndDate().contains("T")) {
                sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                sourceDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            }
            try {
                Date date = sourceDateFormat.parse(exp.getEndDate());
                if (date != null) {
                    endDateCalendar.setTime(date);
                    clearCalendarTime(endDateCalendar);
                    etExperienceEndDate.setText(viewDateFormat.format(date));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing end date from API: " + exp.getEndDate(), e);
                etExperienceEndDate.setText(exp.getEndDate());
            }
        } else {
            cbCurrentJob.setChecked(true);
            etExperienceEndDate.setText("");
            etExperienceEndDate.setEnabled(false);
            tilExperienceEndDate.setEnabled(false);
            if (startDateCalendar.getTimeInMillis() != 0 && !etExperienceStartDate.getText().toString().isEmpty()) {
                endDateCalendar.setTimeInMillis(startDateCalendar.getTimeInMillis());
            } else {
                endDateCalendar.setTimeInMillis(System.currentTimeMillis());
            }
            clearCalendarTime(endDateCalendar);
        }
    }

    private void saveOrUpdateExperience() {
        String title = etExperienceTitle.getText().toString().trim();
        String companyName = etExperienceCompanyName.getText().toString().trim();
        String startDateStrView = etExperienceStartDate.getText().toString().trim();
        String endDateStrView = cbCurrentJob.isChecked() ? null : etExperienceEndDate.getText().toString().trim();
        String description = etExperienceDescription.getText().toString().trim();

        boolean isValid = true;
        // ... (logic validate giữ nguyên) ...
        if (TextUtils.isEmpty(title)) {
            tilExperienceTitle.setError("Chức danh không được để trống"); isValid = false;
        } else { tilExperienceTitle.setError(null); }
        if (TextUtils.isEmpty(companyName)) {
            tilExperienceCompanyName.setError("Tên công ty không được để trống"); isValid = false;
        } else { tilExperienceCompanyName.setError(null); }
        if (TextUtils.isEmpty(startDateStrView)) {
            tilExperienceStartDate.setError("Ngày bắt đầu không được để trống"); isValid = false;
        } else { tilExperienceStartDate.setError(null); }
        if (!cbCurrentJob.isChecked() && TextUtils.isEmpty(endDateStrView)) {
            tilExperienceEndDate.setError("Ngày kết thúc không được để trống (hoặc chọn công việc hiện tại)"); isValid = false;
        } else { tilExperienceEndDate.setError(null); }

        if (!isValid) return;

        String apiStartDate = null;
        String apiEndDate = null;

        try {
            if (!startDateStrView.isEmpty()) {
                Date date = viewDateFormat.parse(startDateStrView);
                if (date != null) apiStartDate = apiDateFormat.format(date);
            }
            if (endDateStrView != null && !endDateStrView.isEmpty()) {
                Date date = viewDateFormat.parse(endDateStrView);
                if (date != null) apiEndDate = apiDateFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing dates for API submission", e);
            Toast.makeText(this, "Định dạng ngày không hợp lệ khi lưu.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (apiStartDate != null && apiEndDate != null) {
            try {
                Date sDate = apiDateFormat.parse(apiStartDate);
                Date eDate = apiDateFormat.parse(apiEndDate);
                if (sDate != null && eDate != null && eDate.before(sDate)) {
                    tilExperienceEndDate.setError("Ngày kết thúc không thể trước ngày bắt đầu");
                    return;
                }
            } catch (ParseException e) { /* Should not happen if formatted correctly */ }
        }

        showLoading(true);

        if (isEditMode && experienceIdToEdit != -1) {
            // CHẾ ĐỘ SỬA: Gọi API PATCH
            UpdateExperienceRequest updateRequest = new UpdateExperienceRequest(title, companyName, apiStartDate, apiEndDate, description);
            Log.d(TAG, "Updating experience ID: " + experienceIdToEdit + ", Start: " + apiStartDate + ", End: " + apiEndDate);
            apiService.updateExperience("Bearer " + currentAccessToken, experienceIdToEdit, updateRequest).enqueue(new Callback<ExperienceDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Response<ExperienceDetailApiResponse> response) {
                    showLoading(false);
                    if(!isFinishing()){
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditExperienceActivity.this, "Kinh nghiệm đã được cập nhật!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("dataChanged", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                                Log.e(TAG, "Error updating experience: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditExperienceActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if(!isFinishing()){
                        Log.e(TAG, "Network failure updating experience", t);
                        Toast.makeText(EditExperienceActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // CHẾ ĐỘ THÊM MỚI: Gọi API POST
            if (currentUserId == -1) {
                Toast.makeText(this, "Lỗi: Không có ID người dùng để thêm kinh nghiệm.", Toast.LENGTH_LONG).show();
                showLoading(false);
                return;
            }
            CreateExperienceRequest createRequest = new CreateExperienceRequest(currentUserId, title, companyName, apiStartDate, apiEndDate, description);
            Log.d(TAG, "Adding new experience for User ID: " + currentUserId + " - StartDate: " + apiStartDate + ", EndDate: " + apiEndDate);
            apiService.createExperience("Bearer " + currentAccessToken, createRequest).enqueue(new Callback<ExperienceDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Response<ExperienceDetailApiResponse> response) {
                    showLoading(false);
                    if (!isFinishing()) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditExperienceActivity.this, "Kinh nghiệm đã được thêm!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("dataChanged", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                                Log.e(TAG, "Error creating experience: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditExperienceActivity.this, "Lỗi thêm mới: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Network failure creating experience", t);
                        Toast.makeText(EditExperienceActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
