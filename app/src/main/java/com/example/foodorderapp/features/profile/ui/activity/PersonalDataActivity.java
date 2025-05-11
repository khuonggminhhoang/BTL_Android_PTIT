// File: com/example/foodorderapp/features/profile/ui/activity/PersonalDataActivity.java
package com.example.foodorderapp.features.profile.ui.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone; // Thêm import TimeZone

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
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivBack, ivAvatar;
    private TextView tvTitle, tvSave;
    private EditText etFullName, etEmail, etPassword, etLocation, etDay, etMonth, etYear;
    private ProgressBar progressBarPersonalData;

    private ApiService apiService;
    private User currentUser;
    private Uri selectedImageUri;
    private boolean avatarChanged = false;

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
                saveUserData();
            }
        });
        ivAvatar.setOnClickListener(v -> openImageChooser());

        View.OnClickListener birthDateClickListener = v -> showDatePickerDialog();
        etDay.setOnClickListener(birthDateClickListener);
        etMonth.setOnClickListener(birthDateClickListener);
        etYear.setOnClickListener(birthDateClickListener);
        etDay.setFocusable(false);
        etMonth.setFocusable(false);
        etYear.setFocusable(false);
    }

    private void showLoading(boolean isLoading) {
        if (progressBarPersonalData != null) {
            progressBarPersonalData.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void fetchCurrentUserData() {
        showLoading(true);
        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        if (accessToken == null) {
            Toast.makeText(this, "Login session expired. Please login again.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            navigateToLogin();
            return;
        }

        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + accessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(Call<ProfileApiResponse> call, Response<ProfileApiResponse> response) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        currentUser = response.body().getData();
                        if (currentUser != null) {
                            Log.d(TAG, "Fetched User for populate - DOB: '" + currentUser.getDateOfBirth() + "', Name: '" + currentUser.getName() + "'");
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
            public void onFailure(Call<ProfileApiResponse> call, Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "Failed to fetch user data (onFailure)", t);
                    Toast.makeText(PersonalDataActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateUserData(User user) {
        if (user == null) {
            Log.e(TAG, "populateUserData: User object is null. Cannot populate fields.");
            etFullName.setText("");
            etEmail.setText("");
            etPassword.setText("********");
            etLocation.setText("");
            etDay.setText(""); etMonth.setText(""); etYear.setText("");
            loadAvatar(null);
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

            // Ưu tiên parse định dạng ISO 8601 đầy đủ (mà API POST trả về)
            try {
                SimpleDateFormat sdfIsoDateTime;
                if (dobFromApi.contains(".")) { // Có milliseconds
                    sdfIsoDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                } else { // Không có milliseconds
                    sdfIsoDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                }
                sdfIsoDateTime.setTimeZone(TimeZone.getTimeZone("UTC")); // Quan trọng nếu server trả về giờ UTC (chữ Z)
                cal.setTime(sdfIsoDateTime.parse(dobFromApi));
                parsedSuccessfully = true;
                Log.d(TAG, "Parsed DOB as ISO 8601 date-time.");
            } catch (ParseException e1) {
                Log.w(TAG, "Failed to parse DOB as ISO 8601 date-time: '" + dobFromApi + "'. Trying yyyy-MM-dd.", e1);
                // Nếu parse trên thất bại, thử parse định dạng yyyy-MM-dd (có thể API GET trả về dạng này)
                try {
                    SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    cal.setTime(sdfDateOnly.parse(dobFromApi));
                    parsedSuccessfully = true;
                    Log.d(TAG, "Parsed DOB as yyyy-MM-dd.");
                } catch (ParseException e2) {
                    Log.e(TAG, "Failed to parse DOB with yyyy-MM-dd as well: '" + dobFromApi + "'", e2);
                }
            }

            if (parsedSuccessfully) {
                etDay.setText(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)));
                etMonth.setText(String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)); // Calendar.MONTH là 0-11
                etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
                Log.d(TAG, "Populated EditTexts - Day: " + etDay.getText() + ", Month: " + etMonth.getText() + ", Year: " + etYear.getText());
            } else {
                etDay.setText(""); etMonth.setText(""); etYear.setText("");
            }
        } else {
            Log.d(TAG, "date_of_birth from API is null or empty for populate. Clearing date fields.");
            etDay.setText(""); etMonth.setText(""); etYear.setText("");
        }
        loadAvatar(user.getAvatar());
    }


    private void loadAvatar(String avatarPath) {
        // ... (Giữ nguyên hàm loadAvatar đã có Glide listener) ...
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

    private void openImageChooser() {
        // ... (Giữ nguyên) ...
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // ... (Giữ nguyên) ...
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).circleCrop().into(ivAvatar);
            avatarChanged = true;
        }
    }

    private boolean validateInputs() {
        // ... (Giữ nguyên) ...
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return false;
        }
        return true;
    }

    private void saveUserData() {
        // ... (Hàm saveUserData giữ nguyên như phiên bản bạn đã gửi ở lần trước,
        //      bao gồm việc tạo dateOfBirthForApi thành "yyyy-MM-dd'T'HH:mm:ss'Z'"
        //      và gửi key "dateOfBirth" (camelCase))
        //      Cũng như logic gọi fetchCurrentUserData() sau khi update thành công.
        //      Đảm bảo bạn đã có các dòng Log chi tiết ở đây.
        //      Tôi sẽ copy lại phần này từ phiên bản trước để đảm bảo tính đầy đủ.
        // ...
        showLoading(true);

        String fullName = etFullName.getText().toString().trim();
        String newPassword = etPassword.getText().toString();
        String location = etLocation.getText().toString().trim();

        String dayStr = etDay.getText().toString().trim();
        String monthStrInput = etMonth.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String dateOfBirthForApi = "";

        Log.d(TAG, "Attempting to save date: Day='" + dayStr + "', MonthInput='" + monthStrInput + "', Year='" + yearStr + "'");

        if (!dayStr.isEmpty() && !monthStrInput.isEmpty() && !yearStr.isEmpty()) {
            try {
                int day = Integer.parseInt(dayStr);
                int year = Integer.parseInt(yearStr);
                int monthNumber = Integer.parseInt(monthStrInput);

                if (monthNumber < 1 || monthNumber > 12 || day < 1 || day > 31) {
                    Log.e(TAG, "Invalid date values after parsing: Day=" + day + ", Month=" + monthNumber + ", Year=" + year);
                    Toast.makeText(this, "Invalid date values selected.", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    return;
                }

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthNumber - 1, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); // Gửi đi dưới dạng UTC
                dateOfBirthForApi = iso8601Format.format(calendar.getTime());
                Log.i(TAG, "SUCCESS: Formatted date_of_birth for API (ISO 8601): '" + dateOfBirthForApi + "'");

            } catch (NumberFormatException e) {
                Log.e(TAG, "ERROR: NumberFormatException for day/month/year. Day: '" + dayStr + "', MonthInput: '" + monthStrInput + "', Year: '" + yearStr + "'", e);
                Toast.makeText(this, "Invalid day, month, or year number.", Toast.LENGTH_SHORT).show();
                showLoading(false);
                return;
            }
        } else if (!dayStr.isEmpty() || !monthStrInput.isEmpty() || !yearStr.isEmpty()){
            Log.w(TAG, "WARN: Some birth date fields are empty. Day: '"+dayStr+"', Month: '"+monthStrInput+"', Year: '"+yearStr+"'");
            Toast.makeText(this, "Please complete all birth date fields or leave them all empty.", Toast.LENGTH_LONG).show();
            showLoading(false);
            return;
        } else {
            Log.d(TAG, "All birth date fields are empty. dateOfBirthForApi will be empty.");
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
            fields.put("dateOfBirth", RequestBody.create(MediaType.parse("text/plain"), dateOfBirthForApi));
            Log.i(TAG, "SUCCESS: Adding to request: key='dateOfBirth', value='" + dateOfBirthForApi + "'");
        } else {
            Log.w(TAG, "WARN: dateOfBirthForApi is empty, NOT adding 'dateOfBirth' to request.");
        }

        if (!newPassword.isEmpty() && !newPassword.equals("********")) {
            fields.put("password", RequestBody.create(MediaType.parse("text/plain"), newPassword));
        }

        MultipartBody.Part avatarFilePart = null;
        if (avatarChanged && selectedImageUri != null) {
            // ... (logic tạo avatarFilePart giữ nguyên) ...
            Log.d(TAG, "Avatar changed. URI: " + selectedImageUri.toString());
            try {
                String realPath = getRealPathFromURI(selectedImageUri);
                Log.d(TAG, "Real path from URI: " + realPath);
                if (realPath != null) {
                    File file = new File(realPath);
                    if (file.exists()) {
                        Log.d(TAG, "File exists: " + file.getAbsolutePath() + ", Size: " + file.length());
                        String mimeType = getContentResolver().getType(selectedImageUri);
                        Log.d(TAG, "MIME type: " + mimeType);
                        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType != null ? mimeType : "image/*"), file);
                        avatarFilePart = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);
                        Log.d(TAG, "Avatar file part created for: " + file.getName());
                    } else {
                        Log.e(TAG, "Avatar file does NOT exist at path: " + realPath);
                        Toast.makeText(this, "Selected image file not found at path.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Could not get real path for avatar URI: " + selectedImageUri);
                    Toast.makeText(this, "Could not process selected image path.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "CRITICAL ERROR creating avatar file part", e);
                Toast.makeText(this, "Error preparing image for upload. Check logs.", Toast.LENGTH_LONG).show();
                showLoading(false);
                return;
            }
        } else {
            Log.d(TAG, "Avatar not changed or URI is null.");
        }


        Call<ProfileApiResponse> call = apiService.updateMyProfile("Bearer " + accessToken, fields, avatarFilePart);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(Call<ProfileApiResponse> call, Response<ProfileApiResponse> response) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.d(TAG, "Update API Response Code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        ProfileApiResponse apiResponse = response.body();
                        Log.d(TAG, "Update API Response Success Flag: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(PersonalDataActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "SUCCESS: Profile updated successfully via API. Re-fetching latest profile data to ensure UI consistency...");
                            fetchCurrentUserData(); // LUÔN GỌI LẠI ĐỂ LẤY DỮ LIỆU MỚI NHẤT
                            avatarChanged = false;
                            selectedImageUri = null;
                        } else {
                            Log.e(TAG, "ERROR: API update call returned success=false. Message: " + apiResponse.getMessage());
                            Toast.makeText(PersonalDataActivity.this, "Update failed: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "ERROR: API update call HTTP error. Code: " + response.code());
                        handleApiError(response, prefs);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileApiResponse> call, Throwable t) {
                showLoading(false);
                if (!isFinishing() && !isDestroyed()) {
                    Log.e(TAG, "CRITICAL ERROR: Failed to call update profile API", t);
                    Toast.makeText(PersonalDataActivity.this, "Network error during update: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private String getRealPathFromURI(Uri contentUri) {
        // ... (Giữ nguyên) ...
        String result = null;
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                result = cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting real path from URI", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "getRealPathFromURI for " + contentUri + " resolved to: " + result);
        return result;
    }

    private void showDatePickerDialog() {
        // ... (Giữ nguyên như phiên bản bạn đã cung cấp, nó đã lưu số tháng vào etMonth) ...
        final Calendar calendar = Calendar.getInstance();
        if (currentUser != null && currentUser.getDateOfBirth() != null && !currentUser.getDateOfBirth().isEmpty()) {
            try {
                // Thử parse cả ISO date-time và date-only
                SimpleDateFormat sdfIsoDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                sdfIsoDateTime.setTimeZone(TimeZone.getTimeZone("UTC"));
                try {
                    calendar.setTime(sdfIsoDateTime.parse(currentUser.getDateOfBirth()));
                } catch (ParseException e1) {
                    SimpleDateFormat sdfDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    calendar.setTime(sdfDateOnly.parse(currentUser.getDateOfBirth()));
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing current DOB for DatePicker default: '" + currentUser.getDateOfBirth() + "'", e);
            }
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // 0-11
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    Log.d(TAG, "DatePickerDialog: Day=" + dayOfMonth + ", MonthOfYear=" + monthOfYear + ", Year=" + yearSelected);
                    etDay.setText(String.format(Locale.US, "%02d", dayOfMonth));
                    etMonth.setText(String.format(Locale.US, "%02d", monthOfYear + 1)); // Lưu số tháng (01-12)
                    etYear.setText(String.valueOf(yearSelected));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void navigateToLogin() {
        // ... (Giữ nguyên) ...
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs) {
        // ... (Giữ nguyên) ...
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
                } catch (IOException e) {
                    Log.e(TAG, "Error parsing error body", e);
                }
            }
            Log.e(TAG, "API Call Error: " + errorMsg);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
    }
}