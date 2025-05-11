package com.example.foodorderapp.features.profile.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Đã import
import android.widget.ProgressBar; // Đã import
import android.widget.TextView; // Đã import
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config; // Import Config
import com.example.foodorderapp.core.model.User; // Model User
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity; // Cho việc logout
import com.example.foodorderapp.features.profile.ui.adapter.MenuAdapter;
import com.example.foodorderapp.features.profile.ui.model.MenuItem;
import com.example.foodorderapp.network.ApiService; // Import ApiService
import com.example.foodorderapp.network.response.ProfileApiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private TextView tvRole;
    private TextView tvApplied;
    private TextView tvReviewed;
    private TextView tvContacted;
    private ImageView ivAvatar;
    private ImageView ivCameraIcon; // Giữ lại nếu bạn có chức năng upload avatar
    private ProgressBar progressBarProfile;

    private ApiService apiService;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String baseUrl = Config.BE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"; // Đảm bảo baseUrl kết thúc bằng dấu "/"
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl) // Sử dụng BE_URL từ Config.java
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các View
        recyclerView = view.findViewById(R.id.rv_menu);
        tvName = view.findViewById(R.id.tv_name);
        tvRole = view.findViewById(R.id.tv_role);
        tvApplied = view.findViewById(R.id.tv_applied);
        tvReviewed = view.findViewById(R.id.tv_reviewed);
        tvContacted = view.findViewById(R.id.tv_contacted);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        ivCameraIcon = view.findViewById(R.id.iv_camera_icon);
        progressBarProfile = view.findViewById(R.id.progressBar_profile_loading); // Ánh xạ ProgressBar mới thêm

        setupRecyclerView();
        // setupUserData(); // Bỏ hàm này, thay bằng fetchUserProfile
        fetchUserProfile();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        menuItems = new ArrayList<>();
        menuItems.add(getString(R.string.account_section_title));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.personal_data), "ACCOUNT"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_edit, getString(R.string.resume_my_info), "ACCOUNT"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_agenda, getString(R.string.my_application), "ACCOUNT"));
        menuItems.add(getString(R.string.other_section_title));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_preferences, getString(R.string.settings_title), "OTHER"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_help, getString(R.string.faq), "OTHER"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.privacy_policy), "OTHER"));
        adapter = new MenuAdapter(getContext(), menuItems);
        recyclerView.setAdapter(adapter);
    }

    private void showLoading(boolean isLoading) {
        if (progressBarProfile != null) {
            progressBarProfile.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        // Ẩn/hiện các view dữ liệu để tránh hiển thị dữ liệu cũ khi đang tải
        int dataVisibility = isLoading ? View.INVISIBLE : View.VISIBLE;
        if (tvName != null) tvName.setVisibility(dataVisibility);
        if (tvRole != null) tvRole.setVisibility(dataVisibility);
        if (ivAvatar != null) ivAvatar.setVisibility(dataVisibility);
        if (ivCameraIcon != null) ivCameraIcon.setVisibility(dataVisibility);
        // Các view khác trong stats_container có thể không cần ẩn vì chúng sẽ được cập nhật
    }

    private void fetchUserProfile() {
        if (getContext() == null) {
            return;
        }
        showLoading(true);

        SharedPreferences prefs = getContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Access Token not found. Please log in.", Toast.LENGTH_LONG).show();
            showLoading(false);
            navigateToLogin();
            return;
        }

//        Call<User> call = apiService.getMyProfile("Bearer " + accessToken);
        Call<ProfileApiResponse> call = apiService.getMyProfile("Bearer " + accessToken); // Dòng mới
        // Nếu ApiService của bạn dùng ProfileResponse:
        // Call<ProfileResponse> call = apiService.getMyProfile("Bearer " + accessToken);

        call.enqueue(new Callback<ProfileApiResponse>() { // Thay Callback<User> bằng Callback<ProfileApiResponse>
            @Override
            public void onResponse(Call<ProfileApiResponse> call, Response<ProfileApiResponse> response) {
                showLoading(false);
                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ProfileApiResponse apiResponse = response.body();
                    // Giả sử ProfileApiResponse có phương thức isSuccess() hoặc bạn kiểm tra statusCode
                    if (apiResponse.isSuccess()) { // Hoặc if (apiResponse.getStatusCode() == 200)
                        User user = apiResponse.getData(); // Lấy User từ đối tượng data
                        if (user != null) {
                            Log.d(TAG, "User data received (from wrapper): Name=" + user.getName() + ", Headline=" + user.getHeadline() + ", Avatar=" + user.getAvatar());
                            updateUIWithUserData(user);
                        } else {
                            Log.e(TAG, "User object within API response data is null.");
                            Toast.makeText(getContext(), "User data is null in API response.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "API indicated failure: " + apiResponse.getMessage());
                        Toast.makeText(getContext(), "Failed to fetch profile: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Xử lý lỗi HTTP (giữ nguyên logic handleApiError)
                    handleApiError(response, prefs); // prefs cần được truyền hoặc là biến instance
                }
            }

            @Override
            public void onFailure(Call<ProfileApiResponse> call, Throwable t) { // Thay Call<User>
                showLoading(false);
                if (!isAdded() || getContext() == null) {
                    return;
                }
                Log.e(TAG, "Network request failed: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleApiError(Response<?> response, SharedPreferences prefs) {
        if (response.code() == 401) { // Unauthorized - Token hết hạn hoặc không hợp lệ
            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear().apply(); // Xóa hết SharedPreferences "AuthPrefs"
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
        if (user == null || getContext() == null) {
            Log.w(TAG, "Attempted to update UI with null user or null context.");
            return;
        }

        if (tvName != null) {
            tvName.setText(user.getName() != null ? user.getName() : "N/A");
        }
        if (tvRole != null) {
            // Trường 'headline' từ model User.java sẽ được dùng cho tvRole
            tvRole.setText(user.getHeadline() != null ? user.getHeadline() : "No headline");
        }

        // Các trường applied, reviewed, contacted hiện tại chưa có trong model User
        // hoặc API /profile/me có thể không trả về.
        // Bạn cần làm rõ dữ liệu này từ đâu để hiển thị chính xác.
        // Tạm thời để giá trị mặc định.
        if (tvApplied != null) tvApplied.setText("-");
        if (tvReviewed != null) tvReviewed.setText("-");
        if (tvContacted != null) tvContacted.setText("-");

        if (ivAvatar != null) {
            String avatarPath = user.getAvatar(); // Lấy từ user object
            Log.d(TAG, "updateUI: Avatar path from user object: '" + avatarPath + "'");

            if (avatarPath != null && !avatarPath.isEmpty()) {
                String fullAvatarUrl = avatarPath;
                // Kiểm tra xem avatarPath có phải là URL đầy đủ không
                if (!avatarPath.toLowerCase().startsWith("http://") && !avatarPath.toLowerCase().startsWith("https://")) {
                    // Nối với base URL của server chứa ảnh.
                    // Ví dụ: Config.BE_URL = "http://192.168.245.252:3001/api/v1"
                    // Nếu ảnh nằm ở "http://192.168.245.252:3001/uploads/..."
                    // thì imageBaseUrl phải là "http://192.168.245.252:3001"

                    String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                    // Cẩn thận: Nếu Config.BE_URL không chứa "/api/v1" thì dòng trên sẽ lỗi.
                    // Một cách an toàn hơn là bạn có một hằng số riêng cho URL gốc của server, ví dụ:
                    // String imageBaseUrl = "http://192.168.245.252:3001";

                    if (avatarPath.startsWith("/")) {
                        fullAvatarUrl = imageBaseUrl + avatarPath;
                    } else {
                        fullAvatarUrl = imageBaseUrl + "/" + avatarPath;
                    }
                    Log.d(TAG, "updateUI: Constructed full avatar URL: '" + fullAvatarUrl + "'");
                } else {
                    Log.d(TAG, "updateUI: Avatar path is already a full URL: '" + fullAvatarUrl + "'");
                }

                Glide.with(this)
                        .load(fullAvatarUrl)
                        .placeholder(R.drawable.ic_placeholder_avatar)
                        .error(R.drawable.ic_placeholder_avatar) // Ảnh lỗi
                        .circleCrop()
                        .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "Glide: Image load FAILED for URL: " + model, e);
                                if (e != null) {
                                    for (Throwable t : e.getRootCauses()) {
                                        Log.e(TAG, "Glide: Root cause: ", t);
                                    }
                                }
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d(TAG, "Glide: Image loaded successfully for URL: " + model);
                                return false;
                            }
                        })
                        .into(ivAvatar);
            } else {
                Log.d(TAG, "updateUI: Avatar path is null or empty from API, loading placeholder.");
                Glide.with(this)
                        .load(R.drawable.ic_placeholder_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }

    private void navigateToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish(); // Đóng MainActivity chứa Fragment này
        }
    }
}