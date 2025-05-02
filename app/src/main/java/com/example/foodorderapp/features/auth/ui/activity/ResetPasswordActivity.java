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
import androidx.core.content.ContextCompat;

import com.example.foodorderapp.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etPassword, etConfirmPassword;
    private ImageView imgTogglePassword, imgToggleConfirmPassword;
    private Button btnReset;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        findViews();
        setupClickListeners();
    }

    private void findViews() {
        btnBack = findViewById(R.id.reset_btn_back);
        etPassword = findViewById(R.id.reset_edt_password);
        etConfirmPassword = findViewById(R.id.reset_edt_confirm_password);
        imgTogglePassword = findViewById(R.id.reset_toggle_password);
        imgToggleConfirmPassword = findViewById(R.id.reset_toggle_confirm_password);
        btnReset = findViewById(R.id.reset_btn_reset);
    }

    private void setupClickListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Toggle mật khẩu mới
        imgTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, imgTogglePassword, isPasswordVisible);
        });

        // Toggle xác nhận mật khẩu mới
        imgToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, imgToggleConfirmPassword, isConfirmPasswordVisible);
        });

        // Nút Reset Password
        btnReset.setOnClickListener(v -> {
            String newPassword = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Kiểm tra input
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) { // Ví dụ kiểm tra độ dài tối thiểu
                Toast.makeText(ResetPasswordActivity.this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Thực hiện logic cập nhật mật khẩu mới (ví dụ: gọi API)
            // Ví dụ tạm thời:
            if (updatePassword(newPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                // Chuyển về màn hình Login và xóa các màn hình trước đó (OTP, ForgotPassword)
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Đóng màn hình hiện tại
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }

        });
    }

    // Hàm giả lập cập nhật password
    private boolean updatePassword(String password) {
        // TODO: Thay thế bằng logic gọi API cập nhật mật khẩu thực tế
        System.out.println("Đang cập nhật mật khẩu mới: " + password);
        return true; // Giả lập thành công
    }


    // Hàm toggle ẩn/hiện mật khẩu (tương tự các màn hình trước)
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