package com.example.foodorderapp.features.profile.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.User;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;
import com.example.foodorderapp.features.profile.ui.adapter.MenuAdapter;
import com.example.foodorderapp.features.profile.ui.model.MenuItem;
import com.example.foodorderapp.network.ApiService;
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<Object> menuItems;

    private TextView tvName;
    private TextInputLayout tilProfileHeadline;
    private TextInputEditText etProfileHeadline;
    private ImageView ivEditProfileHeadline;

    private TextView tvApplied;
    private TextView tvReviewed;
    private TextView tvContacted;
    private ImageView ivAvatar;
    private ImageView ivCameraIcon;
    private ProgressBar progressBarProfile;

    private ApiService apiService;
    private User currentUser;
    private String currentAccessToken;
    private boolean isHeadlineEditing = false;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    // Không cần selectedImageUri ở cấp lớp nếu chỉ dùng trong callback

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String baseUrl = Config.BE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        SharedPreferences prefs = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        currentAccessToken = prefs.getString("accessToken", null);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            Log.d(TAG, "Image selected: " + selectedUri.toString());
                            Glide.with(this).load(selectedUri).circleCrop().placeholder(R.drawable.ic_placeholder_avatar).into(ivAvatar);
                            uploadAvatar(selectedUri);
                        }
                    }
                });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(getContext(), "Quyền truy cập bộ nhớ bị từ chối.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_menu);
        tvName = view.findViewById(R.id.tv_name);
        tilProfileHeadline = view.findViewById(R.id.til_profile_headline);
        etProfileHeadline = view.findViewById(R.id.et_profile_headline);
        ivEditProfileHeadline = view.findViewById(R.id.iv_edit_profile_headline);
//        tvApplied = view.findViewById(R.id.tv_applied);
//        tvReviewed = view.findViewById(R.id.tv_reviewed);
//        tvContacted = view.findViewById(R.id.tv_contacted);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        ivCameraIcon = view.findViewById(R.id.iv_camera_icon);
        progressBarProfile = view.findViewById(R.id.progressBar_profile_loading);

        setupRecyclerView();
        setupClickListeners();

        if (currentAccessToken == null || currentAccessToken.isEmpty()) {
            Toast.makeText(getContext(), "Access Token not found. Please log in.", Toast.LENGTH_LONG).show();
            navigateToLogin();
        } else {
            fetchUserProfile();
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuItems = new ArrayList<>();
        if (getContext() != null) {
            menuItems.add(getString(R.string.account_section_title));
            menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.personal_data), "ACCOUNT"));
            menuItems.add(new MenuItem(android.R.drawable.ic_menu_edit, getString(R.string.resume_my_info), "ACCOUNT"));
            menuItems.add(new MenuItem(android.R.drawable.ic_menu_agenda, getString(R.string.my_application), "ACCOUNT"));
            menuItems.add(getString(R.string.other_section_title));
            menuItems.add(new MenuItem(android.R.drawable.ic_menu_preferences, getString(R.string.settings_title), "OTHER"));
            menuItems.add(new MenuItem(android.R.drawable.ic_menu_help, getString(R.string.faq), "OTHER"));
            menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.privacy_policy), "OTHER"));
        }
        adapter = new MenuAdapter(getContext(), menuItems);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        ivEditProfileHeadline.setOnClickListener(v -> {
            if (isHeadlineEditing) {
                saveHeadlineChanges();
            } else {
                setHeadlineEditState(true);
            }
        });

        ivCameraIcon.setOnClickListener(v -> {
            Log.d(TAG, "Camera icon clicked");
            checkStoragePermissionAndOpenPicker();
        });
    }

    private void checkStoragePermissionAndOpenPicker() {
        if (getContext() == null) return;
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Yêu cầu quyền")
                    .setMessage("Ứng dụng cần quyền truy cập bộ nhớ để bạn có thể chọn ảnh đại diện.")
                    .setPositiveButton("OK", (dialog, which) -> requestPermissionLauncher.launch(permission))
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // intent.setType("image/*"); // Bạn có thể thêm dòng này để chỉ hiển thị ảnh
        imagePickerLauncher.launch(intent);
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath = null;
        if (getContext() == null || contentUri == null) return null;
        // Thử cách lấy đường dẫn chuẩn hơn
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(contentUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error getting path from URI (IllegalArgumentException): " + contentUri.toString(), e);
            // Thử một cách khác nếu cách trên thất bại (ví dụ cho một số URI đặc biệt)
            if ("file".equalsIgnoreCase(contentUri.getScheme())) {
                filePath = contentUri.getPath();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting path from URI: " + contentUri.toString(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "File path from URI " + contentUri.toString() + " : " + filePath);
        return filePath;
    }


    private void uploadAvatar(Uri imageUri) {
        if (getContext() == null || currentAccessToken == null) {
            Toast.makeText(getContext(), "Lỗi: Không thể tải ảnh lên.", Toast.LENGTH_SHORT).show();
            return;
        }

        String filePath = getPathFromUri(imageUri);
        if (filePath == null) {
            Toast.makeText(getContext(), "Không thể lấy đường dẫn tệp ảnh.", Toast.LENGTH_SHORT).show();
            // Khôi phục avatar cũ nếu có
            if (currentUser != null) loadAvatarIntoView(currentUser.getAvatar());
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(getContext(), "Tệp ảnh không tồn tại: " + filePath, Toast.LENGTH_LONG).show();
            Log.e(TAG, "File does not exist: " + filePath);
            if (currentUser != null) loadAvatarIntoView(currentUser.getAvatar());
            return;
        }

        showLoading(true);
        ivCameraIcon.setEnabled(false);

        String mimeType = requireContext().getContentResolver().getType(imageUri);
        if (mimeType == null) {
            // Đoán mime type từ phần mở rộng file nếu không lấy được từ ContentResolver
            String extension = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
            if (extension.equals("jpg") || extension.equals("jpeg")) {
                mimeType = "image/jpeg";
            } else if (extension.equals("png")) {
                mimeType = "image/png";
            } else {
                mimeType = "application/octet-stream"; // Mặc định nếu không biết
                Log.w(TAG, "Could not determine MIME type for " + imageUri + ", using default.");
            }
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        // SỬA TÊN TRƯỜNG THÀNH "photoFile" ĐỂ KHỚP VỚI API
        MultipartBody.Part body = MultipartBody.Part.createFormData("photoFile", file.getName(), requestFile);

        Map<String, RequestBody> fields = new HashMap<>();
        // Nếu API yêu cầu gửi các trường khác ngay cả khi chỉ upload avatar,
        // bạn cần điền chúng vào đây từ currentUser.
        // Ví dụ, nếu API yêu cầu 'name' phải có:
        // if (currentUser != null && currentUser.getName() != null) {
        //     fields.put("name", RequestBody.create(MediaType.parse("text/plain"), currentUser.getName()));
        // } else {
        //     // Xử lý trường hợp currentUser hoặc name là null nếu 'name' là bắt buộc
        //     Toast.makeText(getContext(), "Thiếu thông tin tên người dùng.", Toast.LENGTH_SHORT).show();
        //     showLoading(false);
        //     ivCameraIcon.setEnabled(true);
        //     if (currentUser != null) loadAvatarIntoView(currentUser.getAvatar());
        //     return;
        // }
        // Hiện tại, giả sử API chấp nhận chỉ gửi photoFile khi các trường khác không thay đổi.

        Log.d(TAG, "Uploading avatar: " + file.getName() + " with MIME type: " + mimeType);
        apiService.updateMyProfile("Bearer " + currentAccessToken, fields, body)
                .enqueue(new Callback<ProfileApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                        showLoading(false);
                        ivCameraIcon.setEnabled(true);
                        if (isAdded() && getContext() != null) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                Toast.makeText(getContext(), "Ảnh đại diện đã được cập nhật!", Toast.LENGTH_SHORT).show();
                                User updatedUser = response.body().getData();
                                if (updatedUser != null && updatedUser.getAvatar() != null) {
                                    Log.d(TAG, "Avatar updated successfully. New URL: " + updatedUser.getAvatar());
                                    if(currentUser != null) currentUser.setAvatar(updatedUser.getAvatar());
                                    loadAvatarIntoView(updatedUser.getAvatar());
                                } else {
                                    Log.w(TAG, "Avatar URL is null in success response. Fetching full profile.");
                                    fetchUserProfile(); // Tải lại toàn bộ profile để đảm bảo có URL avatar mới nhất
                                }
                            } else {
                                String errorMessage = "Lỗi tải lên ảnh đại diện.";
                                if (response.body() != null && response.body().getMessage() != null) {
                                    errorMessage += " (" + response.body().getMessage() + ")";
                                } else if (response.errorBody() != null) {
                                    try {
                                        errorMessage += " Code: " + response.code() + " - " + response.errorBody().string();
                                    } catch (IOException e) { /* ignore */ }
                                } else {
                                    errorMessage += " Code: " + response.code();
                                }
                                Log.e(TAG, errorMessage);
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                if (currentUser != null) loadAvatarIntoView(currentUser.getAvatar()); // Khôi phục ảnh cũ
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        ivCameraIcon.setEnabled(true);
                        if (isAdded() && getContext() != null) {
                            Log.e(TAG, "Network failure uploading avatar", t);
                            Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            if (currentUser != null) loadAvatarIntoView(currentUser.getAvatar());
                        }
                    }
                });
    }


    private void setHeadlineEditState(boolean editing) {
        // ... (giữ nguyên)
        isHeadlineEditing = editing;
        etProfileHeadline.setEnabled(editing);
        etProfileHeadline.setFocusable(editing);
        etProfileHeadline.setFocusableInTouchMode(editing);
        etProfileHeadline.setCursorVisible(editing);
        etProfileHeadline.setLongClickable(editing);

        if (editing) {
            ivEditProfileHeadline.setImageResource(R.drawable.ic_save);
            etProfileHeadline.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etProfileHeadline, InputMethodManager.SHOW_IMPLICIT);
            }
            tilProfileHeadline.setBoxStrokeWidth(getResources().getDimensionPixelSize(R.dimen.dp_1));
        } else {
            ivEditProfileHeadline.setImageResource(R.drawable.ic_edit);
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getActivity() != null && getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
            tilProfileHeadline.setBoxStrokeWidth(0);
        }
    }

    private void saveHeadlineChanges() {
        // ... (giữ nguyên)
        String newHeadline = etProfileHeadline.getText().toString().trim();

        if (currentUser != null && currentUser.getHeadline() != null && currentUser.getHeadline().equals(newHeadline)) {
            Toast.makeText(getContext(), "Không có thay đổi nào để lưu.", Toast.LENGTH_SHORT).show();
            setHeadlineEditState(false);
            return;
        }

        showLoading(true);
        ivEditProfileHeadline.setEnabled(false);

        Map<String, RequestBody> fields = new HashMap<>();
        fields.put("headline", RequestBody.create(MediaType.parse("text/plain"), newHeadline));

        Log.d(TAG, "Updating headline to: " + newHeadline);
        apiService.updateMyProfile("Bearer " + currentAccessToken, fields, null).enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                showLoading(false);
                ivEditProfileHeadline.setEnabled(true);
                if (isAdded() && getContext() != null) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "Headline đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        if (currentUser != null) {
                            currentUser.setHeadline(newHeadline);
                        }
                        setHeadlineEditState(false);
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Lỗi không xác định";
                            Log.e(TAG, "Error updating headline: " + response.code() + " - " + errorBody);
                            Toast.makeText(getContext(), "Lỗi cập nhật Headline: " + response.code(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading error body for headline update", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                ivEditProfileHeadline.setEnabled(true);
                if (isAdded() && getContext() != null) {
                    Log.e(TAG, "Network failure updating headline", t);
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoading(boolean isLoading) {
        // ... (giữ nguyên)
        if (progressBarProfile != null) {
            progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        int dataVisibility = isLoading ? View.INVISIBLE : View.VISIBLE;
        if (tvName != null) tvName.setVisibility(dataVisibility);
        if (etProfileHeadline != null) etProfileHeadline.setVisibility(dataVisibility);
        if (ivAvatar != null) ivAvatar.setVisibility(dataVisibility);
        if (ivEditProfileHeadline != null) ivEditProfileHeadline.setVisibility(dataVisibility);
        if (tilProfileHeadline != null) tilProfileHeadline.setVisibility(dataVisibility);
        // Giữ ivCameraIcon luôn hiển thị trừ khi đang upload, được xử lý riêng trong uploadAvatar
    }

    private void fetchUserProfile() {
        // ... (giữ nguyên)
        if (getContext() == null || currentAccessToken == null) {
            return;
        }
        showLoading(true);

        Log.d(TAG, "Fetching user profile for ProfileFragment...");
        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + currentAccessToken);
        call.enqueue(new Callback<ProfileApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileApiResponse> call, @NonNull Response<ProfileApiResponse> response) {
                showLoading(false);
                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User user = response.body().getData();
                    if (user != null) {
                        currentUser = user;
                        Log.d(TAG, "User data received: Name=" + user.getName() + ", Headline=" + user.getHeadline() + ", Avatar=" + user.getAvatar());
                        updateUIWithUserData(user);
                    } else {
                        Log.e(TAG, "User object within API response data is null.");
                        Toast.makeText(getContext(), "User data is null in API response.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    SharedPreferences prefs = requireActivity().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
                    handleApiError(response, prefs);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileApiResponse> call, @NonNull Throwable t) {
                showLoading(false);
                if (!isAdded() || getContext() == null) {
                    return;
                }
                Log.e(TAG, "Network request failed for ProfileFragment: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs) {
        // ... (giữ nguyên)
        if (response.code() == 401) {
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
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
                    Log.e(TAG, "Error parsing error body from API response", e);
                }
            }
            Log.e(TAG, "API Call Error: " + errorMsg);
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void updateUIWithUserData(User user) {
        // ... (phần tvName, etProfileHeadline, stats giữ nguyên) ...
        if (user == null || getContext() == null) {
            Log.w(TAG, "Attempted to update UI with null user or null context.");
            return;
        }

        if (tvName != null) {
            tvName.setText(user.getName() != null ? user.getName() : "N/A");
        }
        if (etProfileHeadline != null) {
            etProfileHeadline.setText(user.getHeadline() != null ? user.getHeadline() : "");
            setHeadlineEditState(false);
        }

        if (tvApplied != null) tvApplied.setText("-");
        if (tvReviewed != null) tvReviewed.setText("-");
        if (tvContacted != null) tvContacted.setText("-");
        loadAvatarIntoView(user.getAvatar());
    }

    private void loadAvatarIntoView(String avatarPath) {
        if (ivAvatar == null || getContext() == null) return;

        Log.d(TAG, "Loading avatar from path: '" + avatarPath + "'");
        String urlToLoad = String.valueOf(R.drawable.ic_placeholder_avatar);

        if (avatarPath != null && !avatarPath.isEmpty()) {
            String fullAvatarUrl = avatarPath;
            if (!avatarPath.toLowerCase().startsWith("http://") && !avatarPath.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                fullAvatarUrl = imageBaseUrl + (avatarPath.startsWith("/") ? "" : "/") + avatarPath;
            }
            Glide.with(this)
                    .load(fullAvatarUrl)
                    .placeholder(R.drawable.ic_placeholder_avatar)
                    .error(R.drawable.ic_placeholder_avatar)
                    .circleCrop()
                    .into(ivAvatar);
        } else {
            Glide.with(this)
                    .load(urlToLoad) // Load placeholder
                    .circleCrop()
                    .into(ivAvatar);
        }
    }

    private void navigateToLogin() {
        // ... (giữ nguyên)
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
