package com.example.foodorderapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodorderapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etEmail;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        findViews();
        setupClickListeners();
    }

    private void findViews() {
        btnBack = findViewById(R.id.forgot_btn_back);
        etEmail = findViewById(R.id.forgot_edt_email);
        btnSend = findViewById(R.id.forgot_btn_send);
    }

    private void setupClickListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Nút Send
        btnSend.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Vui lòng nhập địa chỉ email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(ForgotPasswordActivity.this, "Địa chỉ email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Thực hiện logic gửi yêu cầu reset mật khẩu (ví dụ: gọi API)
            // Ví dụ tạm thời:
            Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu reset mật khẩu đã được gửi đến " + email, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
            startActivity(intent);
            // Có thể thêm finish() ở đây để đóng màn hình sau khi gửi
             finish();
        });
    }
}