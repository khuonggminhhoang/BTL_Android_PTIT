package com.example.foodorderapp.features.auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.main.ui.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView imgTogglePassword;
    private TextView tvForgotPassword;
    private Button btnLogin, btnRegister;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViews();
        setupClickListeners();
    }

    private void findViews() {
        etEmail = findViewById(R.id.login_edt_email);
        etPassword = findViewById(R.id.login_edt_password);
        imgTogglePassword = findViewById(R.id.login_img_toggle_password);
        tvForgotPassword = findViewById(R.id.login_txt_forgot_password);
        btnLogin = findViewById(R.id.login_btn_login);
        btnRegister = findViewById(R.id.login_btn_register);
    }

    private void setupClickListeners() {
        imgTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, imgTogglePassword, isPasswordVisible);
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(LoginActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo JSON object chứa thông tin đăng nhập
            JSONObject loginData = new JSONObject();
            try {
                loginData.put("email", email);
                loginData.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Tạo request
            String url = Config.BE_URL + "/auth/login";
            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, url, loginData,
                response -> {
                    try {
                        // Kiểm tra success và statusCode
                        boolean success = response.getBoolean("success");
                        int statusCode = response.getInt("statusCode");
                        
                        if (success && statusCode == 201) {
                            JSONObject data = response.getJSONObject("data");
                            String accessToken = data.getString("accessToken");
                            String refreshToken = data.getString("refreshToken");
                            JSONObject userData = data.getJSONObject("user");
                            
                            // Lưu tokens và user data vào SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("accessToken", accessToken);
                            editor.putString("refreshToken", refreshToken);
                            editor.putString("user_id", String.valueOf(userData.getInt("id")));
                            editor.putString("user_name", userData.getString("name"));
                            editor.putString("user_email", userData.getString("email"));
                            editor.putString("user_username", userData.getString("username"));
                            editor.putString("user_phone", userData.getString("phoneNumber"));
                            editor.putString("user_avatar", userData.isNull("avatar") ? null : userData.getString("avatar"));
                            editor.putString("user_date_of_birth", userData.getString("dateOfBirth"));
                            editor.apply();
                            
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, "Có lỗi xảy ra khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Xử lý lỗi
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                                break;
                            case 404:
                                Toast.makeText(LoginActivity.this, "Không tìm thấy tài nguyên", Toast.LENGTH_SHORT).show();
                                break;
                            case 500:
                                Toast.makeText(LoginActivity.this, "Lỗi máy chủ", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                    }
                });

            // Thêm request vào queue
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(loginRequest);

            // Ví dụ xử lý nếu đăng nhập thất bại:
            // else {
            //    Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            // }
            // --- Kết thúc Logic đăng nhập ---
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleButton, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(null);
            toggleButton.setColorFilter(ContextCompat.getColor(this, R.color.blue_primary));
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setColorFilter(ContextCompat.getColor(this, R.color.grey));
        }
        editText.setSelection(editText.getText().length());
    }
}