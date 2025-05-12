package com.example.foodorderapp.features.profile.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri; // Import Uri
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date; // Import Date
import java.util.HashMap;
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

public class PersonalDataActivity extends AppCompatActivity {

    private static final String TAG = "PersonalDataActivity";
    // Bỏ PICK_IMAGE_REQUEST nếu không dùng để chọn ảnh trên màn hình này nữa
    // private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivBack, ivAvatar;
    private TextView tvTitle, tvSave;
    private EditText etFullName, etEmail, etPassword, etLocation, etDay, etMonth, etYear;
    private ProgressBar progressBarPersonalData;

    private ApiService apiService;
    private User currentUser;
    // Bỏ selectedImageUri và avatarChanged nếu không cho phép thay đổi avatar từ màn hình này
    // private Uri selectedImageUri;
    // private boolean avatarChanged = false;

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
        ivAvatar = findViewById(R.id.iv_avatar); // Đây là ImageView avatar trên màn hình Personal Data

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etLocation = findViewById(R.id.et_location);
        etDay = findViewById(R.id.et_day);
        etMonth = findViewById(R.id.et_month);
        etYear = findViewById(R.id.et_year);

        progressBarPersonalData = findViewById(R.id.progressBar_personal_data);
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
                saveUserData(); // Hàm saveUserData sẽ không cố gắng upload avatar nếu không có thay đổi
            }
        });

        // SỬA ĐỔI ONCLICKLISTENER CHO IVAVATAR
        ivAvatar.setOnClickListener(v -> {
            if (currentUser != null && currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                String avatarUrl = currentUser.getAvatar();
                // Đảm bảo URL có scheme (http hoặc https)
                if (!avatarUrl.toLowerCase().startsWith("http://") && !avatarUrl.toLowerCase().startsWith("https://")) {
                    // Nối với base URL của server chứa ảnh (nếu avatarUrl là đường dẫn tương đối)
                    String imageBaseUrl = Config.BE_URL.replace("/api/v1", ""); // Hoặc URL gốc của server ảnh
                    avatarUrl = imageBaseUrl + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                }
                Log.d(TAG, "Viewing avatar: " + avatarUrl);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(avatarUrl), "image/*");
                    // Thêm cờ để mở trong ứng dụng khác nếu có thể, tránh lỗi ActivityNotFoundException
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not open avatar URL", e);
                    Toast.makeText(PersonalDataActivity.this, "Không thể mở ảnh đại diện.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(PersonalDataActivity.this, "Không có ảnh đại diện để xem.", Toast.LENGTH_SHORT).show();
            }
            // KHÔNG GỌI openImageChooser() ở đây nữa
        });

        View.OnClickListener birthDateClickListener = v -> showDatePickerDialog();
        etDay.setOnClickListener(birthDateClickListener);
        etMonth.setOnClickListener(birthDateClickListener);
        etYear.setOnClickListener(birthDateClickListener);
        // Giữ các EditText ngày tháng không focusable
        etDay.setFocusable(false);
        etDay.setFocusableInTouchMode(false);
        etMonth.setFocusable(false);
        etMonth.setFocusableInTouchMode(false);
        etYear.setFocusable(false);
        etYear.setFocusableInTouchMode(false);
    }

    private void showLoading(boolean isLoading) {
        // ... (giữ nguyên)
        if (progressBarPersonalData != null) {
            progressBarPersonalData.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchCurrentUserData() {
        // ... (giữ nguyên)
        showLoading(true);
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        if (accessToken == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            navigateToLogin();
            return;
        }

        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + accessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        currentUser = response.body().getData();
                        if (currentUser != null) {
                            Log.d(TAG, "Fetched User for populate - DOB: '" + currentUser.getDateOfBirth() + "', Name: '" + currentUser.getName() + "', Avatar: '" + currentUser.getAvatar() + "'");
                            populateUserData(currentUser);
                        } else {
                            Log.e(TAG, "Fetched user data is null from GET /profile/me");
                            Toast.makeText(PersonalDataActivity.this, "Failed to get user data.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        handleApiError(response, prefs);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to fetch user data (onFailure)", t);
                    Toast.makeText(PersonalDataActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateUserData(User user) {
        // ... (phần điền các EditText giữ nguyên) ...
        if (user == null) {
            Log.e(TAG, "populateUserData: User object is null. Cannot populate fields.");
            etFullName.setText("");
            etEmail.setText("");
            etPassword.setText("********");
            etLocation.setText("");
            etDay.setText(""); etMonth.setText(""); etYear.setText("");
            loadAvatar(null); // Load placeholder
            return;
        }
        etFullName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etEmail.setEnabled(false);
        etPassword.setText("********");
        etLocation.setText(user.getLocation());

        if (user.getDateOfBirth() != null && !user.getDateOfBirth().isEmpty()) {
            String dobFromApi = user.getDateOfBirth();
            Log.d(TAG, "Populating DOB from API value: '" + dobFromApi + "'");
            Calendar cal = Calendar.getInstance();
            boolean parsedSuccessfully = false;
            SimpleDateFormat sdfApiDate;

            if (dobFromApi.contains("T") && dobFromApi.contains("Z")) { // ISO 8601 đầy đủ
                sdfApiDate = new SimpleDateFormat(dobFromApi.contains(".") ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdfApiDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else if (dobFromApi.matches("\\d{4}-\\d{2}-\\d{2}")) { // Chỉ ngày YYYY-MM-DD
                sdfApiDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            } else {
                Log.w(TAG, "Unrecognized dateOfBirth format from API: " + dobFromApi);
                sdfApiDate = null; // Sẽ không parse được
            }

            if (sdfApiDate != null) {
                try {
                    cal.setTime(sdfApiDate.parse(dobFromApi));
                    parsedSuccessfully = true;
                    Log.d(TAG, "Parsed DOB successfully.");
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
        // ... (giữ nguyên) ...
        if (ivAvatar == null) return;
        Log.d(TAG, "loadAvatar called with path: '" + avatarPath + "'");
        if (avatarPath != null && !avatarPath.isEmpty()) {
            String fullAvatarUrl = avatarPath;
            if (!avatarPath.toLowerCase().startsWith("http://") && !avatarPath.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                fullAvatarUrl = imageBaseUrl + (avatarPath.startsWith("/") ? "" : "/") + avatarPath;
                Log.d(TAG, "loadAvatar: Constructed full URL: '" + fullAvatarUrl + "'");
            } else {
                Log.d(TAG, "loadAvatar: Using absolute URL: '" + fullAvatarUrl + "'");
            }
            Glide.with(this).load(fullAvatarUrl)
                    .placeholder(R.drawable.ic_placeholder_avatar)
                    .error(R.drawable.ic_placeholder_avatar)
                    .circleCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide: Image load FAILED for URL: " + model, e);
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "Glide: Image loaded successfully for URL: " + model);
                            return false;
                        }
                    })
                    .into(ivAvatar);
        } else {
            Log.d(TAG, "loadAvatar: Path is null or empty, loading placeholder.");
            Glide.with(this).load(R.drawable.ic_placeholder_avatar).circleCrop().into(ivAvatar);
        }
    }

    // Bỏ phương thức openImageChooser() và onActivityResult() nếu không dùng nữa
    // private void openImageChooser() { ... }
    // @Override
    // protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { ... }


    private boolean validateInputs() {
        // ... (giữ nguyên) ...
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }
        // Thêm các validate khác nếu cần
        return true;
    }

    private void saveUserData() {
        showLoading(true);

        String fullName = etFullName.getText().toString().trim();
        String newPassword = etPassword.getText().toString(); // Lấy text, có thể là "********"
        String location = etLocation.getText().toString().trim();

        String dayStr = etDay.getText().toString().trim();
        String monthStrInput = etMonth.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String dateOfBirthForApi = ""; // Sẽ là YYYY-MM-DD hoặc ISO tùy API của bạn

        Log.d(TAG, "Attempting to save date: Day='" + dayStr + "', MonthInput='" + monthStrInput + "', Year='" + yearStr + "'");

        if (!dayStr.isEmpty() && !monthStrInput.isEmpty() && !yearStr.isEmpty()) {
            try {
                int day = Integer.parseInt(dayStr);
                int year = Integer.parseInt(yearStr);
                int monthNumber = Integer.parseInt(monthStrInput);

                if (monthNumber < 1 || monthNumber > 12 || day < 1 || day > 31) { // Validate cơ bản
                    Toast.makeText(this, "Ngày tháng năm không hợp lệ.", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    return;
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthNumber - 1, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                // API /profile/me của bạn (trong JSON bạn cung cấp) nhận dateOfBirth dạng "YYYY-MM-DD"
                // hoặc "YYYY-MM-DDTHH:mm:ss.SSSZ"
                // Chọn một định dạng nhất quán để gửi lên. Ví dụ, chỉ ngày:
                SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                // Hoặc nếu API PATCH /profile/me yêu cầu ISO đầy đủ:
                // SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                // apiDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                dateOfBirthForApi = apiDateFormat.format(calendar.getTime());
                Log.i(TAG, "Formatted date_of_birth for API: '" + dateOfBirthForApi + "'");

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ngày, tháng, hoặc năm không hợp lệ.", Toast.LENGTH_SHORT).show();
                showLoading(false);
                return;
            }
        } else if (!dayStr.isEmpty() || !monthStrInput.isEmpty() || !yearStr.isEmpty()){
            Toast.makeText(this, "Vui lòng điền đầy đủ ngày sinh hoặc để trống tất cả.", Toast.LENGTH_LONG).show();
            showLoading(false);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        if (accessToken == null) {
            showLoading(false);
            navigateToLogin();
            return;
        }

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("name", RequestBody.create(MediaType.parse("text/plain"), fullName));
        if (!TextUtils.isEmpty(location)) {
            fields.put("location", RequestBody.create(MediaType.parse("text/plain"), location));
        }
        if (!dateOfBirthForApi.isEmpty()) {
            // Key phải khớp với API PATCH /profile/me của bạn (ví dụ: "dateOfBirth")
            fields.put("dateOfBirth", RequestBody.create(MediaType.parse("text/plain"), dateOfBirthForApi));
        }
        // Chỉ gửi password nếu nó không phải là "********" (nghĩa là người dùng đã thay đổi)
        if (!newPassword.equals("********") && !newPassword.isEmpty()) {
            fields.put("password", RequestBody.create(MediaType.parse("text/plain"), newPassword));
        }

        // QUAN TRỌNG: Không gửi avatarFilePart nếu không có thay đổi avatar từ màn hình này
        // MultipartBody.Part avatarFilePart = null;
        // if (avatarChanged && selectedImageUri != null) { ... } // Logic này bị loại bỏ

        Log.d(TAG, "Calling updateMyProfile with fields: " + fields.keySet().toString());
        Call<ProfileApiResponse> call = apiService.updateMyProfile("Bearer " + accessToken, fields, null); // Truyền null cho avatarFilePart
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.d(TAG, "Update API Response Code: " + response.code());
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(PersonalDataActivity.this, "Thông tin cá nhân đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Profile updated successfully. Re-fetching latest profile data...");
                        // Cập nhật currentUser với dữ liệu mới từ response nếu có
                        if (response.body().getData() != null) {
                            currentUser = response.body().getData();
                            populateUserData(currentUser); // Cập nhật lại UI với dữ liệu mới nhất
                        } else {
                            fetchCurrentUserData(); // Hoặc fetch lại nếu API không trả về user data khi update
                        }
                        // avatarChanged = false; // Không cần nữa
                        // selectedImageUri = null; // Không cần nữa
                        etPassword.setText("********"); // Reset trường password về placeholder

                        // Báo hiệu có thay đổi dữ liệu để ProfileFragment có thể làm mới
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("profileDataChanged", true);
                        setResult(Activity.RESULT_OK, resultIntent);

                    } else {
                        Log.e(TAG, "API update call HTTP error. Code: " + response.code());
                        handleApiError(response, prefs);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to call update profile API", t);
                    Toast.makeText(PersonalDataActivity.this, "Lỗi mạng khi cập nhật: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        // ... (giữ nguyên) ...
        final Calendar calendar = Calendar.getInstance();
        // Cố gắng đặt ngày mặc định cho DatePicker từ các EditText nếu đã có giá trị
        try {
            if (!etYear.getText().toString().isEmpty() && !etMonth.getText().toString().isEmpty() && !etDay.getText().toString().isEmpty()){
                int year = Integer.parseInt(etYear.getText().toString());
                int month = Integer.parseInt(etMonth.getText().toString()) - 1; // Calendar month là 0-11
                int day = Integer.parseInt(etDay.getText().toString());
                calendar.set(year, month, day);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Could not parse date from EditTexts for DatePicker default", e);
            // Nếu lỗi, DatePicker sẽ dùng ngày hiện tại
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
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Không cho chọn ngày tương lai
        datePickerDialog.show();
    }

    private void navigateToLogin() {
        // ... (giữ nguyên) ...
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs) {
        // ... (giữ nguyên) ...
        if (isFinishing() || isDestroyed()) return;
        if (response.code() == 401) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear().apply();
            }
            navigateToLogin();
        } else {
            String errorMsg = "Error Code: " + response.code();
            if (response.errorBody() != null) {
                try {
                    errorMsg += " - " + response.errorBody().string();
                } catch (IOException e) { Log.e(TAG, "Error parsing error body", e); }
            }
            Log.e(TAG, "API Call Error: " + errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }
}
