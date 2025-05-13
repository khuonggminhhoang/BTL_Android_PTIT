package com.example.foodorderapp.features.profile.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.User;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.google.gson.Gson; // Import Gson để log chi tiết object

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

public class PersonalDataActivity extends AppCompatActivity {

    private static final String TAG = "PersonalDataActivity";

    private ImageView ivBack, ivAvatar;
    private TextView tvTitle, tvSave;
    private EditText etFullName, etEmail, etLocation, etDay, etMonth, etYear;
    private EditText etPhoneNumber;
    private ProgressBar progressBarPersonalData;

    private NestedScrollView scrollViewPersonalDataContent;
    private LinearLayout layoutErrorRetryPersonalData;
    private TextView tvErrorMessagePersonalData;
    private Button btnRetryPersonalData;

    private ApiService apiService;
    private User currentUser; // Biến lưu trữ user data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        initViews();
        initApiService();
        setupClickListeners();
        fetchCurrentUserData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        tvSave = findViewById(R.id.tv_save);
        ivAvatar = findViewById(R.id.iv_avatar);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etLocation = findViewById(R.id.et_location);
        etDay = findViewById(R.id.et_day);
        etMonth = findViewById(R.id.et_month);
        etYear = findViewById(R.id.et_year);
        progressBarPersonalData = findViewById(R.id.progressBar_personal_data);

        scrollViewPersonalDataContent = findViewById(R.id.scroll_view_personal_data_content);
        layoutErrorRetryPersonalData = findViewById(R.id.layout_error_retry_personal_data);
        tvErrorMessagePersonalData = findViewById(R.id.tv_error_message_personal_data);
        btnRetryPersonalData = findViewById(R.id.btn_retry_personal_data);
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

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        tvSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveUserData();
            }
        });
        ivAvatar.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                String avatarUrl = currentUser.getAvatar();
                if (!avatarUrl.toLowerCase().startsWith("http://") && !avatarUrl.toLowerCase().startsWith("https://")) {
                    String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                    avatarUrl = imageBaseUrl + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                }
                Log.d(TAG, "Viewing avatar: " + avatarUrl);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(avatarUrl), "image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not open avatar URL", e);
                    Toast.makeText(PersonalDataActivity.this, "Không thể mở ảnh đại diện.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(PersonalDataActivity.this, "Không có ảnh đại diện để xem.", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener birthDateClickListener = v -> showDatePickerDialog();
        etDay.setOnClickListener(birthDateClickListener);
        etMonth.setOnClickListener(birthDateClickListener);
        etYear.setOnClickListener(birthDateClickListener);
        etDay.setFocusable(false);
        etDay.setFocusableInTouchMode(false);
        etMonth.setFocusable(false);
        etMonth.setFocusableInTouchMode(false);
        etYear.setFocusable(false);
        etYear.setFocusableInTouchMode(false);

        btnRetryPersonalData.setOnClickListener(v -> fetchCurrentUserData());
    }

    private void showLoadingState() {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.VISIBLE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.GONE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.GONE);
    }

    private void showContentState() {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.VISIBLE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.GONE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.VISIBLE);
        if (tvErrorMessagePersonalData != null) tvErrorMessagePersonalData.setText(message);
    }

    private void fetchCurrentUserData() {
        showLoadingState();
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        if (accessToken == null) {
            showErrorState("Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.");
            btnRetryPersonalData.setEnabled(false);
            new android.os.Handler(getMainLooper()).postDelayed(this::navigateToLogin, 3000);
            return;
        }
        btnRetryPersonalData.setEnabled(true);

        Log.d(TAG, "API_CALL_START: getMyProfile");
        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + accessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                Log.d(TAG, "API_CALL_END: getMyProfile onResponse - Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (response.body() != null) {
                    Log.d(TAG, "GET_Profile_Response_RawBody: " + new Gson().toJson(response.body()));
                } else if (response.errorBody() != null) {
                    try {
                        Log.e(TAG, "GET_Profile_ErrorBody: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body from GET profile", e);
                    }
                }


                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        currentUser = response.body().getData();
                        if (currentUser != null) {
                            // LOG QUAN TRỌNG CẦN KIỂM TRA
                            Log.d(TAG, "GET_PROFILE_DATA_PARSED: Name='" + currentUser.getName() +
                                    "', Phone='" + currentUser.getPhoneNumber() +
                                    "', Avatar='" + currentUser.getAvatar() +
                                    "', Portfolio='" + currentUser.getResumeUrl() + "'");
                            populateUserData(currentUser);
                            showContentState();
                        } else {
                            Log.e(TAG, "Fetched user data (from response.body().getData()) is null from GET /profile/me");
                            showErrorState("Không thể lấy dữ liệu người dùng (dữ liệu trả về rỗng).");
                        }
                    } else {
                        String logMsg = "GET /profile/me failed or body indicates failure. Code: " + response.code();
                        if(response.body() != null) {
                            logMsg += ", BodySuccess: " + response.body().isSuccess() + ", BodyMsg: " + response.body().getMessage();
                        }
                        Log.e(TAG, logMsg);
                        handleApiError(response, prefs, "Lỗi khi tải dữ liệu cá nhân.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API_CALL_FAILURE: getMyProfile onFailure", t);
                if (!isFinishing() && !isDestroyed()) {
                    showErrorState("Lỗi mạng: " + t.getMessage() + ". Vui lòng thử lại.");
                }
            }
        });
    }

    private void populateUserData(User user) {
        if (user == null) {
            Log.e(TAG, "POPULATE_USER_DATA: User object is null. Cannot populate fields.");
            showErrorState("Dữ liệu người dùng không hợp lệ để hiển thị."); // Hiển thị lỗi nếu user null
            return;
        }
        Log.d(TAG, "POPULATE_USER_DATA: Populating with Name=" + user.getName() +
                ", Phone=" + user.getPhoneNumber() +
                ", Location=" + user.getLocation());

        etFullName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etEmail.setEnabled(false);

        etPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        Log.d(TAG, "POPULATE_USER_DATA: Set etPhoneNumber to: '" + etPhoneNumber.getText().toString() + "'");


        etLocation.setText(user.getLocation());

        if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
            String dobFromApi = user.getDateOfBirth();
            Log.d(TAG, "Populating DOB from API value: '" + dobFromApi + "'");
            Calendar cal = Calendar.getInstance();
            boolean parsedSuccessfully = false;
            SimpleDateFormat sdfApiDate;

            if (dobFromApi.contains("T") && dobFromApi.contains("Z")) {
                sdfApiDate = new SimpleDateFormat(dobFromApi.contains(".") ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdfApiDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else if (dobFromApi.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sdfApiDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            } else {
                Log.w(TAG, "Unrecognized dateOfBirth format from API: " + dobFromApi);
                sdfApiDate = null;
            }

            if (sdfApiDate != null) {
                try {
                    Date parsedDate = sdfApiDate.parse(dobFromApi);
                    if(parsedDate != null) {
                        cal.setTime(parsedDate);
                        parsedSuccessfully = true;
                        Log.d(TAG, "Parsed DOB successfully.");
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "Failed to parse DOB: '" + dobFromApi + "'", e);
                }
            }

            if (parsedSuccessfully) {
                etDay.setText(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)));
                etMonth.setText(String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1));
                etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
            } else {
                etDay.setText(""); etMonth.setText(""); etYear.setText("");
            }
        } else {
            Log.d(TAG, "dateOfBirth from API is null or empty. Clearing date fields.");
            etDay.setText(""); etMonth.setText(""); etYear.setText("");
        }
        loadAvatar(user.getAvatar());
    }


    private void loadAvatar(String avatarPath) {
        if (ivAvatar == null) return;
        Log.d(TAG, "loadAvatar called with path: '" + avatarPath + "'");
        if (avatarPath != null && !avatarPath.isEmpty()) {
            String fullAvatarUrl = avatarPath;
            if (!avatarPath.toLowerCase().startsWith("http://") && !avatarPath.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                fullAvatarUrl = imageBaseUrl + (avatarPath.startsWith("/") ? "" : "/") + avatarPath;
            }
            Log.d(TAG, "loadAvatar: Using URL: '" + fullAvatarUrl + "'");
            Glide.with(this).load(fullAvatarUrl)
                    .placeholder(R.drawable.ic_placeholder_avatar)
                    .error(R.drawable.ic_placeholder_avatar)
                    .circleCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "GLIDE_LOAD_FAILED: " + model, e);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "GLIDE_LOAD_SUCCESS: " + model);
                            return false;
                        }
                    })
                    .into(ivAvatar);
        } else {
            Log.d(TAG, "loadAvatar: Path is null or empty, loading placeholder.");
            Glide.with(this).load(R.drawable.ic_placeholder_avatar).circleCrop().into(ivAvatar);
        }
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Họ và tên không được để trống");
            etFullName.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserData() {
        Toast.makeText(this, "Đang lưu...", Toast.LENGTH_SHORT).show();
        // Tạm thời disable nút save để tránh click nhiều lần
        tvSave.setEnabled(false);


        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String dayStr = etDay.getText().toString().trim();
        String monthStrInput = etMonth.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String dateOfBirthForApi = "";

        Log.d(TAG, "SaveUserData: Attempting to save date: Day='" + dayStr + "', MonthInput='" + monthStrInput + "', Year='" + yearStr + "'");
        if (!dayStr.isEmpty() && !monthStrInput.isEmpty() && !yearStr.isEmpty()) {
            try {
                int day = Integer.parseInt(dayStr);
                int year = Integer.parseInt(yearStr);
                int monthNumber = Integer.parseInt(monthStrInput);

                if (monthNumber < 1 || monthNumber > 12 || day < 1 || day > 31) {
                    Toast.makeText(this, "Ngày tháng năm không hợp lệ.", Toast.LENGTH_SHORT).show();
                    tvSave.setEnabled(true); return;
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthNumber - 1, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                dateOfBirthForApi = apiDateFormat.format(calendar.getTime());
                Log.i(TAG, "SaveUserData: Formatted date_of_birth for API: '" + dateOfBirthForApi + "'");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ngày, tháng, hoặc năm không hợp lệ.", Toast.LENGTH_SHORT).show();
                tvSave.setEnabled(true); return;
            }
        } else if (!dayStr.isEmpty() || !monthStrInput.isEmpty() || !yearStr.isEmpty()){
            Toast.makeText(this, "Vui lòng điền đầy đủ ngày sinh hoặc để trống tất cả.", Toast.LENGTH_LONG).show();
            tvSave.setEnabled(true); return;
        }


        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        if (accessToken == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            tvSave.setEnabled(true);
            navigateToLogin();
            return;
        }

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("name", RequestBody.create(MediaType.parse("text/plain"), fullName));

        if (!TextUtils.isEmpty(phoneNumber)) {
            fields.put("phone_number", RequestBody.create(MediaType.parse("text/plain"), phoneNumber));
        } else {
            // Nếu muốn gửi rỗng để xóa trên server, hãy đảm bảo backend xử lý được
            // Hoặc không gửi nếu không có thay đổi/muốn giữ giá trị cũ (nếu có)
            // Hiện tại: nếu trống thì không gửi, server sẽ giữ giá trị cũ (nếu có) hoặc null
        }


        if (!TextUtils.isEmpty(location)) {
            fields.put("location", RequestBody.create(MediaType.parse("text/plain"), location));
        }
        if (!dateOfBirthForApi.isEmpty()) {
            fields.put("dateOfBirth", RequestBody.create(MediaType.parse("text/plain"), dateOfBirthForApi));
        }

        Log.d(TAG, "SaveUserData: Calling updateMyProfile with fields: " + new Gson().toJson(fields.keySet()));
        Call<ProfileApiResponse> call = apiService.updateMyProfile("Bearer " + accessToken, fields, null);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                tvSave.setEnabled(true); // Kích hoạt lại nút Save
                Log.d(TAG, "SaveUserData_onResponse: Code = " + response.code() + ", isSuccessful = " + response.isSuccessful());
                if (response.body() != null) {
                    Log.d(TAG, "SaveUserData_onResponse: Body as JSON = " + new Gson().toJson(response.body()));
                } else if (response.errorBody() != null) {
                    try {
                        Log.e(TAG, "SaveUserData_onResponse: ErrorBody = " + response.errorBody().string());
                    } catch (IOException e) { /* ignore */ }
                }

                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(PersonalDataActivity.this, "Thông tin cá nhân đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        User updatedUser = response.body().getData();
                        if (updatedUser != null) {
                            currentUser = updatedUser; // Cập nhật lại currentUser với phản hồi từ server
                            Log.d(TAG, "SAVE_SUCCESS_DATA_AFTER_UPDATE: Name=" + currentUser.getName() +
                                    ", Phone=" + currentUser.getPhoneNumber() +
                                    ", Location=" + currentUser.getLocation());
                            populateUserData(currentUser); // Hiển thị dữ liệu mới
                        } else {
                            Log.d(TAG, "SAVE_SUCCESS_NO_DATA_IN_RESPONSE: response.body().getData() is null. Re-fetching profile.");
                            fetchCurrentUserData(); // Lấy lại toàn bộ profile nếu API update không trả về data
                        }
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("profileDataChanged", true);
                        setResult(Activity.RESULT_OK, resultIntent);

                    } else {
                        Log.e(TAG, "API update call HTTP error or body indicates failure.");
                        handleApiError(response, prefs, "Lỗi khi cập nhật thông tin.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                tvSave.setEnabled(true); // Kích hoạt lại nút Save
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "SaveUserData_onFailure: Network failure", t);
                    Toast.makeText(PersonalDataActivity.this, "Lỗi mạng khi cập nhật: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        try {
            if (!etYear.getText().toString().isEmpty() && !etMonth.getText().toString().isEmpty() && !etDay.getText().toString().isEmpty()){
                int year = Integer.parseInt(etYear.getText().toString());
                int month = Integer.parseInt(etMonth.getText().toString()) - 1;
                int day = Integer.parseInt(etDay.getText().toString());
                calendar.set(year, month, day);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Could not parse date from EditTexts for DatePicker default", e);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    Log.d(TAG, "DatePickerDialog: Day=" + dayOfMonth + ", MonthOfYear=" + monthOfYear + ", Year=" + yearSelected);
                    etDay.setText(String.format(Locale.US, "%02d", dayOfMonth));
                    etMonth.setText(String.format(Locale.US, "%02d", monthOfYear + 1));
                    etYear.setText(String.valueOf(yearSelected));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs, String defaultMessagePrefix) {
        if (isFinishing() || isDestroyed()) return;

        String errorMessage = defaultMessagePrefix;
        if (response.code() == 401) {
            showErrorState("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear().apply();
            }
            new android.os.Handler(getMainLooper()).postDelayed(this::navigateToLogin, 3000);
            return;
        } else {
            errorMessage = defaultMessagePrefix + " Mã lỗi: " + response.code();
            if (response.errorBody() != null) {
                try {
                    String errorBodyStr = response.errorBody().string();
                    Log.e(TAG, "Error body string from handleApiError: " + errorBodyStr);
                    if (errorBodyStr.trim().startsWith("{")) { // Check if it's likely JSON
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyStr);
                        errorMessage += " - " + errorJson.optString("message", errorJson.optString("error", "Không có thông điệp lỗi cụ thể."));
                    } else {
                        errorMessage += " - " + errorBodyStr; // Display as plain text if not JSON
                    }
                } catch (IOException | org.json.JSONException e) { // Catch both exceptions
                    Log.e(TAG, "Error parsing error body for API in handleApiError", e);
                }
            } else if (response.message() != null && !response.message().isEmpty()){
                errorMessage += " - " + response.message();
            }
        }
        Log.e(TAG, "API Call Error (handleApiError): " + errorMessage);
        showErrorState(errorMessage);
    }
}