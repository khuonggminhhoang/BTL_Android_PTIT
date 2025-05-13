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
import android.os.SystemClock; // Thêm để đo thời gian
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
import okhttp3.OkHttpClient; // Thêm
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor; // Thêm
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
    private User currentUser;

    // Định dạng ngày tháng thống nhất
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Chỉ ngày, tháng, năm cho API
    private final SimpleDateFormat iso8601WithMillisFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private final SimpleDateFormat iso8601WithoutMillisFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        // Thiết lập TimeZone cho các định dạng ISO
        iso8601WithMillisFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        iso8601WithoutMillisFormat.setTimeZone(TimeZone.getTimeZone("UTC"));


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
        if (baseUrl == null || baseUrl.isEmpty()) {
            Log.e(TAG, "BE_URL is not configured!");
            Toast.makeText(this, "Lỗi cấu hình máy chủ.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        // Thêm HttpLoggingInterceptor để debug network calls
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d("OkHttp", message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client) // Sử dụng OkHttpClient đã cấu hình
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
                    String imageBaseUrl = Config.BE_URL.replace("/api/v1", ""); // Giả sử BE_URL có /api/v1
                    avatarUrl = imageBaseUrl + (avatarUrl.startsWith("/") ? "" : "/") + avatarUrl;
                }
                Log.d(TAG, "Viewing avatar: " + avatarUrl);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(avatarUrl), "image/*");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Cần thiết nếu mở từ context không phải Activity
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

        // Ngăn bàn phím hiện khi click vào EditText ngày tháng
        etDay.setFocusable(false);
        etDay.setFocusableInTouchMode(false);
        etMonth.setFocusable(false);
        etMonth.setFocusableInTouchMode(false);
        etYear.setFocusable(false);
        etYear.setFocusableInTouchMode(false);

        btnRetryPersonalData.setOnClickListener(v -> fetchCurrentUserData());
    }

    private void showLoadingState(boolean show) {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(show ? View.VISIBLE : View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(show ? View.GONE : View.VISIBLE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.GONE); // Luôn ẩn lỗi khi bắt đầu load
        tvSave.setEnabled(!show); // Vô hiệu hóa nút Save khi đang tải
    }

    private void showContentState() {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.VISIBLE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.GONE);
        tvSave.setEnabled(true);
    }

    private void showErrorState(String message) {
        if (progressBarPersonalData != null) progressBarPersonalData.setVisibility(View.GONE);
        if (scrollViewPersonalDataContent != null) scrollViewPersonalDataContent.setVisibility(View.GONE);
        if (layoutErrorRetryPersonalData != null) layoutErrorRetryPersonalData.setVisibility(View.VISIBLE);
        if (tvErrorMessagePersonalData != null) tvErrorMessagePersonalData.setText(message);
        tvSave.setEnabled(true); // Cho phép thử lưu lại nếu lỗi không phải do xác thực
    }

    private void fetchCurrentUserData() {
        if (apiService == null) {
            showErrorState("Lỗi cấu hình dịch vụ. Vui lòng thử lại sau.");
            return;
        }
        showLoadingState(true);
        long startTime = SystemClock.elapsedRealtime(); // Đo thời gian bắt đầu

        SharedPreferences prefs = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        if (accessToken == null) {
            showErrorState("Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.");
            btnRetryPersonalData.setEnabled(false); // Không cho retry nếu token mất
            new android.os.Handler(getMainLooper()).postDelayed(this::navigateToLogin, 2000);
            return;
        }
        btnRetryPersonalData.setEnabled(true); // Cho phép retry nếu có token

        Log.d(TAG, "API_CALL_START: getMyProfile");
        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + accessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                long endTime = SystemClock.elapsedRealtime();
                Log.d(TAG, "API_CALL_END: getMyProfile onResponse. Time taken: " + (endTime - startTime) + "ms. Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());

                // Log response thô (đã có interceptor, nhưng log ở đây cũng tốt để đối chiếu)
                // if (response.body() != null) {
                //     Log.d(TAG, "GET_Profile_Response_ParsedBody_GSON: " + new Gson().toJson(response.body()));
                // } else if (response.errorBody() != null) {
                //     try { Log.e(TAG, "GET_Profile_ErrorBody_String: " + response.errorBody().string()); }
                //     catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                // }

                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    currentUser = response.body().getData();
                    if (currentUser != null) {
                        Log.d(TAG, "GET_PROFILE_DATA_PARSED: Name='" + currentUser.getName() +
                                "', Phone='" + currentUser.getPhoneNumber() + // Đã sửa key
                                "', Avatar='" + currentUser.getAvatar() +
                                "', Portfolio='" + currentUser.getResumeUrl() + "'");
                        populateUserData(currentUser);
                        showContentState();
                    } else {
                        Log.e(TAG, "Fetched user data (from response.body().getData()) is null from GET /profile/me");
                        showErrorState("Không thể lấy dữ liệu người dùng (dữ liệu rỗng).");
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

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                long endTime = SystemClock.elapsedRealtime();
                Log.e(TAG, "API_CALL_FAILURE: getMyProfile onFailure. Time taken: " + (endTime - startTime) + "ms", t);
                if (!isFinishing() && !isDestroyed()) {
                    showErrorState("Lỗi mạng: " + t.getMessage() + ". Vui lòng thử lại.");
                }
            }
        });
    }

    private void populateUserData(User user) {
        if (user == null) {
            Log.e(TAG, "POPULATE_USER_DATA: User object is null. Cannot populate fields.");
            showErrorState("Dữ liệu người dùng không hợp lệ để hiển thị.");
            return;
        }
        long populateStartTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "POPULATE_USER_DATA_START: Populating with Name=" + user.getName());

        etFullName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etEmail.setEnabled(false); // Email thường không cho sửa

        etPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        etLocation.setText(user.getLocation() != null ? user.getLocation() : "");

        // Xử lý ngày sinh
        String dobFromApi = user.getDateOfBirth();
        if (dobFromApi != null && !dobFromApi.isEmpty()) {
            Log.d(TAG, "Populating DOB from API value: '" + dobFromApi + "'");
            Date parsedDate = parseDateString(dobFromApi);
            if (parsedDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(parsedDate);
                etDay.setText(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)));
                etMonth.setText(String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)); // Calendar.MONTH bắt đầu từ 0
                etYear.setText(String.valueOf(cal.get(Calendar.YEAR)));
            } else {
                Log.w(TAG, "Failed to parse DOB: '" + dobFromApi + "'. Clearing date fields.");
                clearDateFields();
            }
        } else {
            Log.d(TAG, "dateOfBirth from API is null or empty. Clearing date fields.");
            clearDateFields();
        }

        loadAvatar(user.getAvatar());
        long populateEndTime = SystemClock.elapsedRealtime();
        Log.d(TAG, "POPULATE_USER_DATA_END. Time taken: " + (populateEndTime - populateStartTime) + "ms");
    }

    private void clearDateFields() {
        etDay.setText("");
        etMonth.setText("");
        etYear.setText("");
    }

    private Date parseDateString(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;

        // Thử định dạng ISO 8601 có milliseconds
        try { return iso8601WithMillisFormat.parse(dateString); }
        catch (ParseException e) { /* Thử định dạng tiếp theo */ }

        // Thử định dạng ISO 8601 không có milliseconds
        try { return iso8601WithoutMillisFormat.parse(dateString); }
        catch (ParseException e) { /* Thử định dạng tiếp theo */ }

        // Thử định dạng yyyy-MM-dd
        try { return apiDateFormat.parse(dateString); }
        catch (ParseException e) { /* Thử định dạng tiếp theo */ }

        // Thử định dạng dd/MM/yyyy (nếu có thể nhập liệu thủ công)
        try { return displayDateFormat.parse(dateString); }
        catch (ParseException e) { Log.w(TAG, "Could not parse date string with any known format: " + dateString); }

        return null;
    }


    private void loadAvatar(String avatarPath) {
        if (ivAvatar == null || isFinishing() || isDestroyed()) return;
        Log.d(TAG, "loadAvatar called with path: '" + avatarPath + "'");

        String fullAvatarUrl = avatarPath;
        if (avatarPath != null && !avatarPath.isEmpty()) {
            if (!avatarPath.toLowerCase().startsWith("http://") && !avatarPath.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", ""); // Đảm bảo logic này đúng
                fullAvatarUrl = imageBaseUrl + (avatarPath.startsWith("/") ? "" : "/") + avatarPath;
            }
        } else {
            // Nếu path rỗng hoặc null, load placeholder
            Glide.with(this).load(R.drawable.ic_placeholder_avatar).circleCrop().into(ivAvatar);
            Log.d(TAG, "loadAvatar: Path is null or empty, loading placeholder.");
            return;
        }

        Log.d(TAG, "loadAvatar: Using URL: '" + fullAvatarUrl + "'");
        Glide.with(this).load(fullAvatarUrl)
                .placeholder(R.drawable.ic_placeholder_avatar)
                .error(R.drawable.ic_placeholder_avatar) // Quan trọng: fallback nếu lỗi
                .circleCrop()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e(TAG, "GLIDE_LOAD_FAILED for model: " + model, e);
                        return false; // false để Glide xử lý error drawable
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "GLIDE_LOAD_SUCCESS for model: " + model);
                        return false;
                    }
                })
                .into(ivAvatar);
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(etFullName.getText().toString().trim())) {
            etFullName.setError("Họ và tên không được để trống");
            etFullName.requestFocus();
            return false;
        }
        // Thêm các validate khác nếu cần (ví dụ: định dạng số điện thoại)
        return true;
    }

    private void saveUserData() {
        showLoadingState(true); // Hiển thị loading khi bắt đầu lưu

        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String dayStr = etDay.getText().toString().trim();
        String monthStrInput = etMonth.getText().toString().trim(); // Đã là tháng (1-12)
        String yearStr = etYear.getText().toString().trim();
        String dateOfBirthForApi = "";

        Log.d(TAG, "SaveUserData: Attempting to save date: Day='" + dayStr + "', MonthInput='" + monthStrInput + "', Year='" + yearStr + "'");

        if (!dayStr.isEmpty() && !monthStrInput.isEmpty() && !yearStr.isEmpty()) {
            try {
                int day = Integer.parseInt(dayStr);
                int month = Integer.parseInt(monthStrInput); // Đã là tháng 1-12
                int year = Integer.parseInt(yearStr);

                // Validate ngày tháng năm cơ bản
                if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                    Toast.makeText(this, "Ngày tháng năm không hợp lệ.", Toast.LENGTH_SHORT).show();
                    showContentState(); // Trả về trạng thái content
                    return;
                }
                // Tạo Calendar để lấy Date object
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, day, 0,0,0); // Calendar.MONTH là 0-11
                calendar.set(Calendar.MILLISECOND, 0);

                dateOfBirthForApi = apiDateFormat.format(calendar.getTime());
                Log.i(TAG, "SaveUserData: Formatted date_of_birth for API: '" + dateOfBirthForApi + "'");

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Ngày, tháng, hoặc năm không đúng định dạng số.", Toast.LENGTH_SHORT).show();
                showContentState(); return;
            }
        } else if (!dayStr.isEmpty() || !monthStrInput.isEmpty() || !yearStr.isEmpty()) {
            // Nếu một trong các trường ngày tháng có dữ liệu nhưng không đủ cả 3
            Toast.makeText(this, "Vui lòng điền đầy đủ ngày, tháng, năm sinh hoặc để trống tất cả.", Toast.LENGTH_LONG).show();
            showContentState(); return;
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

        // Chỉ gửi các trường nếu chúng có giá trị hoặc API yêu cầu gửi rỗng để xóa
        // Hiện tại, nếu rỗng thì không gửi, server sẽ giữ giá trị cũ (nếu có) hoặc null
        if (!TextUtils.isEmpty(phoneNumber)) {
            fields.put("phoneNumber", RequestBody.create(MediaType.parse("text/plain"), phoneNumber));
        }
        if (!TextUtils.isEmpty(location)) {
            fields.put("location", RequestBody.create(MediaType.parse("text/plain"), location));
        }
        if (!dateOfBirthForApi.isEmpty()) {
            fields.put("dateOfBirth", RequestBody.create(MediaType.parse("text/plain"), dateOfBirthForApi));
        }
        // Nếu muốn xóa một trường trên server bằng cách gửi giá trị rỗng, bạn cần đảm bảo API hỗ trợ:
        // Ví dụ: fields.put("location", RequestBody.create(MediaType.parse("text/plain"), ""));


        Log.d(TAG, "SaveUserData: Calling updateMyProfile with fields: " + new Gson().toJson(fields.keySet()));
        Call<ProfileApiResponse> call = apiService.updateMyProfile("Bearer " + accessToken, fields, null); // Giả sử không cập nhật avatar ở đây
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                // Không gọi showContentState() ngay, vì có thể là lỗi
                Log.d(TAG, "SaveUserData_onResponse: Code = " + response.code() + ", isSuccessful = " + response.isSuccessful());
                // Log chi tiết hơn
                // if (response.body() != null) Log.d(TAG, "SaveUserData_onResponse: Body as JSON = " + new Gson().toJson(response.body()));
                // else if (response.errorBody() != null) try { Log.e(TAG, "SaveUserData_onResponse: ErrorBody = " + response.errorBody().string()); } catch (IOException e) { /* ignore */ }

                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PersonalDataActivity.this, "Thông tin cá nhân đã được cập nhật!", Toast.LENGTH_SHORT).show();
                    User updatedUser = response.body().getData();
                    if (updatedUser != null) {
                        currentUser = updatedUser;
                        Log.d(TAG, "SAVE_SUCCESS_DATA_AFTER_UPDATE: Name=" + currentUser.getName() +
                                ", Phone=" + currentUser.getPhoneNumber() +
                                ", Location=" + currentUser.getLocation());
                        populateUserData(currentUser); // Cập nhật UI với dữ liệu mới nhất từ server
                    } else {
                        Log.d(TAG, "SAVE_SUCCESS_NO_DATA_IN_RESPONSE: response.body().getData() is null. Re-fetching profile.");
                        fetchCurrentUserData(); // Lấy lại nếu API update không trả data
                    }
                    showContentState(); // Hiển thị content sau khi thành công
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("profileDataChanged", true); // Báo cho Activity trước đó biết có thay đổi
                    setResult(Activity.RESULT_OK, resultIntent);
                    // Không finish() ở đây, để người dùng xem lại thông tin đã cập nhật

                } else {
                    Log.e(TAG, "API update call HTTP error or body indicates failure.");
                    handleApiError(response, prefs, "Lỗi khi cập nhật thông tin.");
                    // showErrorState đã được gọi trong handleApiError nếu cần
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "SaveUserData_onFailure: Network failure", t);
                showErrorState("Lỗi mạng khi cập nhật: " + t.getMessage());
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        // Lấy ngày hiện tại từ EditText nếu có, nếu không dùng ngày hiện tại
        try {
            if (!etYear.getText().toString().isEmpty() && !etMonth.getText().toString().isEmpty() && !etDay.getText().toString().isEmpty()){
                int year = Integer.parseInt(etYear.getText().toString());
                int month = Integer.parseInt(etMonth.getText().toString()) - 1; // Calendar.MONTH là 0-11
                int day = Integer.parseInt(etDay.getText().toString());
                calendar.set(year, month, day);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Could not parse date from EditTexts for DatePicker default", e);
            // Nếu parse lỗi, calendar sẽ giữ ngày hiện tại
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    Log.d(TAG, "DatePickerDialog: Day=" + dayOfMonth + ", MonthOfYear=" + monthOfYear + ", Year=" + yearSelected);
                    etDay.setText(String.format(Locale.US, "%02d", dayOfMonth));
                    etMonth.setText(String.format(Locale.US, "%02d", monthOfYear + 1)); // Hiển thị tháng 1-12
                    etYear.setText(String.valueOf(yearSelected));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()); // Không cho chọn ngày tương lai
        datePickerDialog.show();
    }

    private void navigateToLogin() {
        if (isFinishing() || isDestroyed()) return;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs, String defaultMessagePrefix) {
        if (isFinishing() || isDestroyed()) return;

        String errorMessage = defaultMessagePrefix;
        if (response.code() == 401) { // Unauthorized
            showErrorState("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            btnRetryPersonalData.setEnabled(false); // Không cho retry nếu token hết hạn
            if (prefs != null) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear().apply(); // Xóa token
            }
            new android.os.Handler(getMainLooper()).postDelayed(this::navigateToLogin, 2000);
            return; // Không xử lý thêm
        } else {
            errorMessage = defaultMessagePrefix + " (Mã lỗi: " + response.code() + ")";
            if (response.errorBody() != null) {
                try {
                    String errorBodyStr = response.errorBody().string(); // Đọc errorBody một lần
                    Log.e(TAG, "Error body string from handleApiError: " + errorBodyStr);
                    // Cố gắng parse JSON nếu có thể
                    if (errorBodyStr.trim().startsWith("{")) {
                        org.json.JSONObject errorJson = new org.json.JSONObject(errorBodyStr);
                        errorMessage += ": " + errorJson.optString("message", errorJson.optString("error", "Lỗi không xác định từ máy chủ."));
                    } else if (!errorBodyStr.isEmpty()){
                        errorMessage += ": " + errorBodyStr; // Hiển thị text nếu không phải JSON
                    }
                } catch (IOException | org.json.JSONException e) {
                    Log.e(TAG, "Error parsing error body for API in handleApiError", e);
                    // errorMessage đã có mã lỗi, không cần thêm gì
                }
            } else if (response.message() != null && !response.message().isEmpty()){
                errorMessage += ". " + response.message();
            }
        }
        Log.e(TAG, "API Call Error (handleApiError): " + errorMessage);
        showErrorState(errorMessage); // Hiển thị lỗi cho người dùng
        // Không tự động ẩn ProgressBar ở đây, showErrorState sẽ xử lý
    }
}

