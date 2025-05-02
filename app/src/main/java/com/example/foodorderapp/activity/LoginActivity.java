package com.example.foodorderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foodorderapp.R;

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

            // --- Logic đăng nhập ---
            // TODO: Thay thế phần này bằng logic xác thực thực tế (ví dụ: gọi API, kiểm tra database)
            // Ví dụ đơn giản: Nếu đăng nhập thành công
            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Đóng LoginActivity sau khi đăng nhập thành công

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