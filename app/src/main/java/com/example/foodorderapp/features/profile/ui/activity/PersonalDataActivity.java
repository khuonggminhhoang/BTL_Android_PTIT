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
import android.os.SystemClock;
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
import com.google.gson.Gson;

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
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PersonalDataActivity extends AppCompatActivity {

    private static final String TAG = "PersonalDataActivity";

    private ImageView ivBack, ivAvatar;
    private TextView tvTitle, tvSave;
    private EditText etFullName, etEmail, etLocation, etDay, etMonth, etYear, etPhoneNumber;
    private ProgressBar progressBarPersonalData;
    private NestedScrollView scrollViewPersonalDataContent;
    private LinearLayout layoutErrorRetryPersonalData;
    private TextView tvErrorMessagePersonalData;
    private Button btnRetryPersonalData;

    private ApiService apiService;
    private User currentUser;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat iso8601WithMillisFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private final SimpleDateFormat iso8601WithoutMillisFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        // Cài đặt TimeZone cho định dạng ISO
        iso8601WithMillisFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        iso8601WithoutMillisFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Khởi tạo giao diện và API
        initViews();
        initApiService();
        setupClickListeners();
        fetchCurrentUserData();
    }

    // Khởi tạo view
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

    // Khởi tạo dịch vụ API
    private void initApiService() {
        String baseUrl = Config.BE_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL chưa được cấu hình!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!baseUrl.endsWith("/")) baseUrl += "/";
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d("OkHttp", message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    // Cài đặt sự kiện click
    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());
        tvSave.setOnClickListener(v -> {
            if (validateInputs()) saveUserData();
        });
        ivAvatar.setOnClickListener(v -> viewAvatar());
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

    // Hiển thị trạng thái tải
    private void showLoadingState(boolean show) {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(show ? View.VISIBLE : View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(show ? View.GONE : View.VISIBLE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.GONE);
        tvSave.setEnabled(!show);
    }

    // Hiển thị nội dung
    private void showContentState() {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.VISIBLE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.GONE);
        tvSave.setEnabled(true);
    }

    // Hiển thị trạng thái lỗi
    private void showErrorState(String message) {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.GONE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.VISIBLE);
        if (tvErrorMessagePersonalData != null) tvErrorMessagePersonalData.setText(message);
        tvSave.setEnabled(true);
    }

    // Tải dữ liệu người dùng
    private void fetchCurrentUserData() {
        if (apiService == null) {
            showErrorState("Lỗi cấu hình dịch vụ.");
            return;
        }
        showLoadingState(true);
        long startTime = SystemClock.elapsedRealtime();
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        if (accessToken == null) {
            showErrorState("Phiên làm việc hết hạn. Vui lòng đăng nhập lại.");
            btnRetryPersonalData.setEnabled(false);
            new android.os.Handler(getMainLooper()).postDelayed(this::navigateToLogin, 2000);
            return;
        }
        btnRetryPersonalData.setEnabled(true);
        Log.d(TAG, "Bắt đầu gọi API: getMyProfile");
        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + accessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                long endTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "Kết thúc gọi API: getMyProfile. Thời gian: " + (endTime - startTime) + "ms. Mã: " + response.code());
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    if (currentUser != null) {
                        populateUserData(currentUser);
                        showContentState();
                    } else {
                        Log.e(TAG, "Dữ liệu người dùng rỗng từ API GET /profile/me");
                        showErrorState("Không thể lấy dữ liệu người dùng.");
                    }
                } else {
                    handleApiError(response, prefs, "Lỗi khi tải dữ liệu cá nhân.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                long endTime = SystemClock.elapsedRealtime();
                Log.e(TAG, "Lỗi mạng khi tải dữ liệu. Thời gian: " + (endTime - startTime) + "ms", t);
                if (!isFinishing() && !isDestroyed()) {
                    showErrorState("Lỗi mạng: " + t.getMessage() + ". Vui lòng thử lại.");
                }
            }
        });
    }

    // Điền dữ liệu người dùng vào giao diện
    private void populateUserData(User user) {
        if (user == null) {
            Log.e(TAG, "Dữ liệu người dùng rỗng.");
            showErrorState("Dữ liệu người dùng không hợp lệ.");
            return;
        }
        long populateStartTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "Bắt đầu điền dữ liệu: Tên=" + user.getName());
        etFullName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etEmail.setEnabled(false);
        etPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        etLocation.setText(user.getLocation() != null ? user.getLocation() : "");
        String dobFromApi = user.getDateOfBirth();
        if (dobFromApi != null && !dobFromApi.isEmpty()) {
            Date parsedDate = parseDateString(dobFromApi);
            if (parsedDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(parsedDate);
                etDay.setText(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)));
                etMonth.setText(String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1));
                etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
            } else {
                Log.w(TAG, "Không thể phân tích ngày sinh: '" + dobFromApi + "'.");
                clearDateFields();
            }
        } else {
            clearDateFields();
        }
        loadAvatar(user.getAvatar());
        long populateEndTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "Kết thúc điền dữ liệu. Thời gian: " + (populateEndTime - populateStartTime) + "ms");
    }

    // Xóa các trường ngày tháng
    private void clearDateFields() {
        etDay.setText("");
        etMonth.setText("");
        etYear.setText("");
    }

    // Phân tích chuỗi ngày tháng
    private Date parseDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        try { return iso8601WithMillisFormat.parse(dateString); } catch (ParseException e) {}
        try { return iso8601WithoutMillisFormat.parse(dateString); } catch (ParseException e) {}
        try { return apiDateFormat.parse(dateString); } catch (ParseException e) {}
        try { return displayDateFormat.parse(dateString); } catch (ParseException e) {
            Log.w(TAG, "Không thể phân tích ngày: " + dateString);
        }
        return null;
    }

    // Tải ảnh đại diện
    private void loadAvatar(String avatarPath) {
        if (ivAvatar == null || isFinishing() || isDestroyed()) return;
        Log.d(TAG, "Tải ảnh đại diện: '" + avatarPath + "'");
        String fullAvatarUrl = avatarPath;
        if (avatarPath != null && !avatarPath.isEmpty()) {
            if (!avatarPath.toLowerCase().startsWith("http://") && !avatarPath.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                fullAvatarUrl = imageBaseUrl + (avatarPath.startsWith("/") ? "" : "/") + avatarPath;
            }
        } else {
            Glide.with(this).load(R.drawable.ic_placeholder_avatar).circleCrop().into(ivAvatar);
            Log.d(TAG, "Không có ảnh đại diện, sử dụng placeholder.");
            return;
        }
        Log.d(TAG, "Tải ảnh từ URL: '" + fullAvatarUrl + "'");
        Glide.with(this).load(fullAvatarUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .error(R.drawable.ic_placeholder_avatar)
                .circleCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "Tải ảnh thất bại: " + model, e);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "Tải ảnh thành công: " + model);
                        return false;
                    }
                })
                .into(ivAvatar);
    }

    // Xem ảnh đại diện
    private void viewAvatar() {
        if (currentUser != null && currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
            String avatarUrl = currentUser.getAvatar();
            if (!avatarUrl.toLowerCase().startsWith("http://") && !avatarUrl.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                avatarUrl = imageBaseUrl + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
            }
            Log.d(TAG, "Xem ảnh đại diện: " + avatarUrl);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(avatarUrl), "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Không thể mở ảnh đại diện", e);
                Toast.makeText(PersonalDataActivity.this, "Không thể mở ảnh đại diện.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(PersonalDataActivity.this, "Không có ảnh đại diện.", Toast.LENGTH_SHORT).show();
        }
    }

    // Xác thực đầu vào
    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Họ và tên không được để trống");
            etFullName.requestFocus();
            return false;
        }
        return true;
    }

    // Lưu dữ liệu người dùng
    private void saveUserData() {
        showLoadingState(true);
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String dayStr = etDay.getText().toString().trim();
        String monthStrInput = etMonth.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String dateOfBirthForApi = "";
        Log.d(TAG, "Lưu dữ liệu: Ngày='" + dayStr + "', Tháng='" + monthStrInput + "', Năm='" + yearStr + "'");
        if (!dayStr.isEmpty() && !monthStrInput.isEmpty() && !yearStr.isEmpty()) {
            try {
                int day = Integer.parseInt(dayStr);
                int month = Integer.parseInt(monthStrInput);
                int year = Integer.parseInt(yearStr);
                if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                    Toast.makeText(this, "Ngày tháng năm không hợp lệ.", Toast.LENGTH_SHORT).show();
                    showContentState();
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dateOfBirthForApi = apiDateFormat.format(calendar.getTime());
                Log.i(TAG, "Ngày sinh định dạng cho API: '" + dateOfBirthForApi + "'");
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ngày, tháng, hoặc năm không đúng định dạng.", Toast.LENGTH_SHORT).show();
                showContentState();
                return;
            }
        } else if (!dayStr.isEmpty() || !monthStrInput.isEmpty() || !yearStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ ngày, tháng, năm hoặc để trống.", Toast.LENGTH_LONG).show();
            showContentState();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        if (accessToken == null) {
            Toast.makeText(this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
            showContentState();
            navigateToLogin();
            return;
        }
        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("name", RequestBody.create(MediaType.parse("text/plain"), fullName));
        if (!TextUtils.isEmpty(phoneNumber)) {
            fields.put("phoneNumber", RequestBody.create(MediaType.parse("text/plain"), phoneNumber));
        }
        if (!TextUtils.isEmpty(location)) {
            fields.put("location", RequestBody.create(MediaType.parse("text/plain"), location));
        }
        if (!dateOfBirthForApi.isEmpty()) {
            fields.put("dateOfBirth", RequestBody.create(MediaType.parse("text/plain"), dateOfBirthForApi));
        }
        Log.d(TAG, "Gọi API updateMyProfile với các trường: " + new Gson().toJson(fields.keySet()));
        Call<ProfileApiResponse> call = apiService.updateMyProfile("Bearer " + accessToken, fields, null);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                Log.d(TAG, "Kết quả cập nhật: Mã = " + response.code());
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PersonalDataActivity.this, "Thông tin cá nhân đã được cập nhật!", Toast.LENGTH_SHORT).show();
                    User updatedUser = response.body().getData();
                    if (updatedUser != null) {
                        currentUser = updatedUser;
                        Log.d(TAG, "Cập nhật thành công: Tên=" + currentUser.getName());
                        populateUserData(currentUser);
                    } else {
                        Log.d(TAG, "Không có dữ liệu trong phản hồi, tải lại dữ liệu.");
                        fetchCurrentUserData();
                    }
                    showContentState();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("profileDataChanged", true);
                    setResult(Activity.RESULT_OK, resultIntent);
                } else {
                    Log.e(TAG, "Lỗi cập nhật từ API.");
                    handleApiError(response, prefs, "Lỗi khi cập nhật thông tin.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Lỗi mạng khi cập nhật", t);
                showErrorState("Lỗi mạng khi cập nhật: " + t.getMessage());
            }
        });
    }

    // Hiển thị hộp thoại chọn ngày
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        try {
            if (!etYear.getText().toString().isEmpty() && !etMonth.getText().toString().isEmpty() && !etDay.getText().toString().isEmpty()) {
                int year = Integer.parseInt(etYear.getText().toString());
                int month = Integer.parseInt(etMonth.getText().toString()) - 1;
                int day = Integer.parseInt(etDay.getText().toString());
                calendar.set(year, month, day);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Không thể phân tích ngày từ EditText", e);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    Log.d(TAG, "Chọn ngày: Ngày=" + dayOfMonth + ", Tháng=" + monthOfYear + ", Năm=" + yearSelected);
                    etDay.setText(String.format(Locale.US, "%02d", dayOfMonth));
                    etMonth.setText(String.format(Locale.US, "%02d", monthOfYear + 1));
                    etYear.setText(String.valueOf(yearSelected));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // Điều hướng đến màn hình đăng nhập
    private void navigateToLogin() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Xử lý lỗi API
    private void handleApiError(Response<?> response, SharedPreferences prefs, String defaultMessagePrefix) {
        if (isFinishing() || isDestroyed()) return;
        String errorMessage = defaultMessagePrefix;
        if (response.code() == 401) {
            showErrorState("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.");
            btnRetryPersonalData.setEnabled(false);
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear().apply();
            }
            new android.os.Handler(getMainLooper()).postDelayed(this::navigateToLogin, 2000);
            return;
        } else {
            errorMessage = defaultMessagePrefix + " (Mã lỗi: " + response.code() + ")";
            if (response.errorBody() != null) {
                try {
                    String errorBodyStr = response.errorBody().string();
                    Log.e(TAG, "Thông báo lỗi: " + errorBodyStr);
                    if (errorBodyStr.trim().startsWith("{")) {
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyStr);
                        errorMessage += ": " + errorJson.optString("message", errorJson.optString("error", "Lỗi không xác định."));
                    } else if (!errorBodyStr.isEmpty()) {
                        errorMessage += ": " + errorBodyStr;
                    }
                } catch (IOException | org.json.JSONException e) {
                    Log.e(TAG, "Lỗi phân tích thông báo lỗi", e);
                }
            } else if (response.message() != null && !response.message().isEmpty()) {
                errorMessage += ". " + response.message();
            }
        }
        Log.e(TAG, "Lỗi API: " + errorMessage);
        showErrorState(errorMessage);
    }
}