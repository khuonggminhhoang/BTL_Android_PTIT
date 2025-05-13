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
import com.example.foodorderapp.network.request.CreateExperienceRequest;
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
    public static final String EXTRA_USER_ID = "USER_ID_FOR_NEW_EXPERIENCE";

    private Toolbar toolbarEditExperience;
    private TextInputLayout tilExperienceTitle, tilExperienceCompanyName, tilExperienceStartDate, tilExperienceEndDate, tilExperienceDescription;
    private TextInputEditText etExperienceTitle, etExperienceCompanyName, etExperienceStartDate, etExperienceEndDate, etExperienceDescription;
    private CheckBox cbCurrentJob;
    private Button btnSaveExperience;
    private ProgressBar progressBarEditExperience;

    private ApiService apiService;
    private String currentAccessToken;
    private int experienceIdToEdit = -1;
    private int currentUserId = -1;
    private boolean isEditMode = false;

    private SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_experience);

        // Khởi tạo lịch
        startDateCalendar = Calendar.getInstance();
        clearCalendarTime(startDateCalendar);
        endDateCalendar = Calendar.getInstance();
        clearCalendarTime(endDateCalendar);
        apiDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Khởi tạo giao diện và API
        initViews();
        setupToolbar();
        initApiService();

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
        if (intent != null && intent.hasExtra(EXTRA_EXPERIENCE_ID)) {
            isEditMode = true;
            experienceIdToEdit = intent.getIntExtra(EXTRA_EXPERIENCE_ID, -1);
            if (experienceIdToEdit != -1) {
                toolbarEditExperience.setTitle("Chỉnh sửa Kinh nghiệm");
                btnSaveExperience.setText("Lưu thay đổi");
                fetchExperienceDetails(experienceIdToEdit);
            } else {
                Toast.makeText(this, "ID kinh nghiệm không hợp lệ.", Toast.LENGTH_SHORT).show();
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
            toolbarEditExperience.setTitle("Thêm Kinh nghiệm mới");
            btnSaveExperience.setText("Thêm Kinh nghiệm");
            updateEndDateFieldState();
        } else {
            Toast.makeText(this, "Thiếu thông tin để thực hiện.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Cài đặt sự kiện
        setupDatePickers();
        btnSaveExperience.setOnClickListener(v -> saveOrUpdateExperience());
        cbCurrentJob.setOnCheckedChangeListener((buttonView, isChecked) -> updateEndDateFieldState());
    }

    // Xóa thời gian trong lịch
    private void clearCalendarTime(Calendar cal) {
        if (cal == null) return;
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    // Khởi tạo view
    private void initViews() {
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

    // Cài đặt Toolbar
    private void setupToolbar() {
        setSupportActionBar(toolbarEditExperience);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbarEditExperience.setNavigationOnClickListener(v -> finish());
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

    // Cài đặt chọn ngày
    private void setupDatePickers() {
        etExperienceStartDate.setOnClickListener(v -> showDatePickerDialog(true));
        etExperienceEndDate.setOnClickListener(v -> {
            if (!cbCurrentJob.isChecked()) showDatePickerDialog(false);
        });
    }

    // Hiển thị hộp thoại chọn ngày
    private void showDatePickerDialog(final boolean isStartDatePicker) {
        Calendar calendarToInitializeDialog = Calendar.getInstance();

        if (isStartDatePicker) {
            if (!etExperienceStartDate.getText().toString().isEmpty()) {
                try {
                    Date d = viewDateFormat.parse(etExperienceStartDate.getText().toString());
                    if (d != null) calendarToInitializeDialog.setTime(d);
                } catch (ParseException e) {
                    calendarToInitializeDialog.setTimeInMillis(startDateCalendar.getTimeInMillis());
                }
            } else {
                calendarToInitializeDialog.setTimeInMillis(startDateCalendar.getTimeInMillis());
            }
        } else {
            if (!etExperienceEndDate.getText().toString().isEmpty()) {
                try {
                    Date d = viewDateFormat.parse(etExperienceEndDate.getText().toString());
                    if (d != null) calendarToInitializeDialog.setTime(d);
                } catch (ParseException e) {
                    calendarToInitializeDialog.setTimeInMillis(endDateCalendar.getTimeInMillis());
                }
            } else if (!etExperienceStartDate.getText().toString().isEmpty()) {
                try {
                    Date d = viewDateFormat.parse(etExperienceStartDate.getText().toString());
                    if (d != null) calendarToInitializeDialog.setTime(d);
                } catch (ParseException e) {
                    calendarToInitializeDialog.setTimeInMillis(endDateCalendar.getTimeInMillis());
                }
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
                                    Toast.makeText(this, "Ngày kết thúc đã được xóa.", Toast.LENGTH_SHORT).show();
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

    // Cập nhật trạng thái trường ngày kết thúc
    private void updateEndDateFieldState() {
        boolean isCurrentJob = cbCurrentJob.isChecked();
        etExperienceEndDate.setEnabled(!isCurrentJob);
        tilExperienceEndDate.setEnabled(!isCurrentJob);
        if (isCurrentJob) etExperienceEndDate.setText("");
    }

    // Hiển thị/ẩn trạng thái tải
    private void showLoading(boolean isLoading) {
        progressBarEditExperience.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveExperience.setEnabled(!isLoading);
        etExperienceTitle.setEnabled(!isLoading);
        etExperienceCompanyName.setEnabled(!isLoading);
        etExperienceStartDate.setEnabled(!isLoading);
        etExperienceEndDate.setEnabled(!isLoading && !cbCurrentJob.isChecked());
        cbCurrentJob.setEnabled(!isLoading);
        etExperienceDescription.setEnabled(!isLoading);
    }

    // Tải chi tiết kinh nghiệm
    private void fetchExperienceDetails(int experienceId) {
        showLoading(true);
        Log.d(TAG, "Tải chi tiết kinh nghiệm ID: " + experienceId);
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
                        Toast.makeText(EditExperienceActivity.this, "Lỗi tải chi tiết: " + response.code(), Toast.LENGTH_SHORT).show();
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

    // Điền dữ liệu kinh nghiệm vào giao diện
    private void populateExperienceDataToFields(Experience exp) {
        etExperienceTitle.setText(exp.getTitle());
        etExperienceCompanyName.setText(exp.getCompanyName());
        etExperienceDescription.setText(exp.getDescription());

        SimpleDateFormat sourceDateFormat;
        if (exp.getStartDate() != null && !exp.getStartDate().isEmpty()) {
            sourceDateFormat = exp.getStartDate().contains("T") ?
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US) :
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sourceDateFormat.parse(exp.getStartDate());
                if (date != null) {
                    startDateCalendar.setTime(date);
                    clearCalendarTime(startDateCalendar);
                    etExperienceStartDate.setText(viewDateFormat.format(date));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Lỗi phân tích ngày bắt đầu: " + exp.getStartDate(), e);
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
            sourceDateFormat = exp.getEndDate().contains("T") ?
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US) :
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sourceDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sourceDateFormat.parse(exp.getEndDate());
                if (date != null) {
                    endDateCalendar.setTime(date);
                    clearCalendarTime(endDateCalendar);
                    etExperienceEndDate.setText(viewDateFormat.format(date));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Lỗi phân tích ngày kết thúc: " + exp.getEndDate(), e);
                etExperienceEndDate.setText(exp.getEndDate());
            }
        } else {
            cbCurrentJob.setChecked(true);
            etExperienceEndDate.setText("");
            etExperienceEndDate.setEnabled(false);
            tilExperienceEndDate.setEnabled(false);
            endDateCalendar.setTimeInMillis(startDateCalendar.getTimeInMillis() != 0 ?
                    startDateCalendar.getTimeInMillis() : System.currentTimeMillis());
            clearCalendarTime(endDateCalendar);
        }
    }

    // Lưu hoặc cập nhật kinh nghiệm
    private void saveOrUpdateExperience() {
        String title = etExperienceTitle.getText().toString().trim();
        String companyName = etExperienceCompanyName.getText().toString().trim();
        String startDateStrView = etExperienceStartDate.getText().toString().trim();
        String endDateStrView = cbCurrentJob.isChecked() ? null : etExperienceEndDate.getText().toString().trim();
        String description = etExperienceDescription.getText().toString().trim();

        // Xác thực đầu vào
        boolean isValid = true;
        if (TextUtils.isEmpty(title)) {
            tilExperienceTitle.setError("Chức danh không được để trống");
            isValid = false;
        } else {
            tilExperienceTitle.setError(null);
        }
        if (TextUtils.isEmpty(companyName)) {
            tilExperienceCompanyName.setError("Tên công ty không được để trống");
            isValid = false;
        } else {
            tilExperienceCompanyName.setError(null);
        }
        if (TextUtils.isEmpty(startDateStrView)) {
            tilExperienceStartDate.setError("Ngày bắt đầu không được để trống");
            isValid = false;
        } else {
            tilExperienceStartDate.setError(null);
        }
        if (!cbCurrentJob.isChecked() && TextUtils.isEmpty(endDateStrView)) {
            tilExperienceEndDate.setError("Ngày kết thúc không được để trống");
            isValid = false;
        } else {
            tilExperienceEndDate.setError(null);
        }

        if (!isValid) return;

        // Chuyển đổi định dạng ngày
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
            Log.e(TAG, "Lỗi phân tích ngày khi lưu", e);
            Toast.makeText(this, "Định dạng ngày không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra ngày kết thúc hợp lệ
        if (apiStartDate != null && apiEndDate != null) {
            try {
                Date sDate = apiDateFormat.parse(apiStartDate);
                Date eDate = apiDateFormat.parse(apiEndDate);
                if (sDate != null && eDate != null && eDate.before(sDate)) {
                    tilExperienceEndDate.setError("Ngày kết thúc không thể trước ngày bắt đầu");
                    return;
                }
            } catch (ParseException e) { /* Không xảy ra nếu định dạng đúng */ }
        }

        showLoading(true);

        if (isEditMode && experienceIdToEdit != -1) {
            // Cập nhật kinh nghiệm
            UpdateExperienceRequest updateRequest = new UpdateExperienceRequest(title, companyName, apiStartDate, apiEndDate, description);
            Log.d(TAG, "Cập nhật kinh nghiệm ID: " + experienceIdToEdit);
            apiService.updateExperience("Bearer " + currentAccessToken, experienceIdToEdit, updateRequest).enqueue(new Callback<ExperienceDetailApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Response<ExperienceDetailApiResponse> response) {
                    showLoading(false);
                    if (!isFinishing()) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(EditExperienceActivity.this, "Kinh nghiệm đã được cập nhật!", Toast.LENGTH_SHORT).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("dataChanged", true);
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                                Log.e(TAG, "Lỗi cập nhật kinh nghiệm: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditExperienceActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Lỗi mạng khi cập nhật kinh nghiệm", t);
                        Toast.makeText(EditExperienceActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Thêm kinh nghiệm mới
            if (currentUserId == -1) {
                Toast.makeText(this, "Lỗi: Không có ID người dùng.", Toast.LENGTH_LONG).show();
                showLoading(false);
                return;
            }
            CreateExperienceRequest createRequest = new CreateExperienceRequest(currentUserId, title, companyName, apiStartDate, apiEndDate, description);
            Log.d(TAG, "Thêm kinh nghiệm mới cho User ID: " + currentUserId);
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
                                Log.e(TAG, "Lỗi thêm kinh nghiệm: " + response.code() + " - " + errorBody);
                                Toast.makeText(EditExperienceActivity.this, "Lỗi thêm mới: " + response.code(), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Log.e(TAG, "Lỗi đọc thông báo lỗi", e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ExperienceDetailApiResponse> call, @NonNull Throwable t) {
                    showLoading(false);
                    if (!isFinishing()) {
                        Log.e(TAG, "Lỗi mạng khi thêm kinh nghiệm", t);
                        Toast.makeText(EditExperienceActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}