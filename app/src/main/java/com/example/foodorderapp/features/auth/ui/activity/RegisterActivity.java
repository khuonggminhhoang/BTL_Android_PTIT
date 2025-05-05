package com.example.foodorderapp.features.auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import để dùng ContextCompat.getColor

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;

public class RegisterActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText edtName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private ImageView imgTogglePassword, imgToggleConfirmPassword;
    private Button btnRegister, btnLogin;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();

        setupClickListeners();
    }

    private void findViews() {
        btnBack = findViewById(R.id.register_btn_back);
        edtName = findViewById(R.id.register_edt_name);
        edtUsername = findViewById(R.id.register_edt_username);
        edtEmail = findViewById(R.id.register_txt_email);
        edtPassword = findViewById(R.id.register_edt_password);
        edtConfirmPassword = findViewById(R.id.register_edt_confirm_password);
        imgTogglePassword = findViewById(R.id.register_toggle_password);
        imgToggleConfirmPassword = findViewById(R.id.register_toggle_confirm_password);
        btnRegister = findViewById(R.id.register_btn_register);
        btnLogin = findViewById(R.id.register_btn_login);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        imgTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(edtPassword, imgTogglePassword, isPasswordVisible);
        });

        imgToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(edtConfirmPassword, imgToggleConfirmPassword, isConfirmPasswordVisible);
        });

        // Nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(RegisterActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6){
                Toast.makeText(RegisterActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi API đăng ký
            org.json.JSONObject registerData = new org.json.JSONObject();
            try {
                registerData.put("name", name);
                registerData.put("username", username);
                registerData.put("email", email);
                registerData.put("password", password);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }

            String url = Config.BE_URL + "/auth/register";
            JsonObjectRequest registerRequest = new com.android.volley.toolbox.JsonObjectRequest(
                Request.Method.POST, url, registerData,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        int statusCode = response.getInt("statusCode");
                        if (success && (statusCode == 201 || statusCode == 200)) {
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = response.optString("message", "Đăng ký thất bại");
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (org.json.JSONException e) {
                        Toast.makeText(RegisterActivity.this, "Có lỗi xảy ra khi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        String errorMsg = "Lỗi: " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            errorMsg += "\n" + responseBody;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            );
            com.android.volley.RequestQueue requestQueue = com.android.volley.toolbox.Volley.newRequestQueue(this);
            requestQueue.add(registerRequest);
        });

        // Nút chuyển sang Đăng nhập
        btnLogin.setOnClickListener(v -> {
            // Chuyển sang LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class); // Đảm bảo LoginActivity tồn tại
            startActivity(intent);
            finish(); // Đóng RegisterActivity
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleButton, boolean isVisible) {
        if (isVisible) {
            editText.setTransformationMethod(null); // Xóa transformation
            toggleButton.setColorFilter(ContextCompat.getColor(this, R.color.blue_primary));
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setColorFilter(ContextCompat.getColor(this, R.color.grey));
        }
        editText.setSelection(editText.getText().length());
    }
}