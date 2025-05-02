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

import com.example.foodorderapp.R;

public class RegisterActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText edtEmail, edtPassword, edtConfirmPassword;
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
            // TODO: Thêm logic xử lý đăng ký (validate input, gọi API,...)
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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


            // Xử lý đăng ký thành công (ví dụ)
            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            // Chuyển sang màn hình đăng nhập
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng RegisterActivity
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