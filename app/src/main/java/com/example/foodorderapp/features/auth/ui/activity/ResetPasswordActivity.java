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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;

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
        String email = getIntent().getStringExtra("email");
        findViews();
        setupClickListeners(email);
    }

    private void findViews() {
        btnBack = findViewById(R.id.reset_btn_back);
        etPassword = findViewById(R.id.reset_edt_password);
        etConfirmPassword = findViewById(R.id.reset_edt_confirm_password);
        imgTogglePassword = findViewById(R.id.reset_toggle_password);
        imgToggleConfirmPassword = findViewById(R.id.reset_toggle_confirm_password);
        btnReset = findViewById(R.id.reset_btn_reset);
    }

    private void setupClickListeners(String email) {
        btnBack.setOnClickListener(v -> finish());

        imgTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(etPassword, imgTogglePassword, isPasswordVisible);
        });

        imgToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, imgToggleConfirmPassword, isConfirmPasswordVisible);
        });

        btnReset.setOnClickListener(v -> {
            String newPassword = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Vui lòng nhập đầy đủ mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(ResetPasswordActivity.this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email == null || email.isEmpty()) {
                Toast.makeText(ResetPasswordActivity.this, "Không tìm thấy email để đặt lại mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = Config.BE_URL + "/auth/reset-password";
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("email", email);
                requestData.put("newPassword", newPassword);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ResetPasswordActivity.this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest resetPasswordRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(ResetPasswordActivity.this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            );

            RequestQueue queue = Volley.newRequestQueue(ResetPasswordActivity.this);
            queue.add(resetPasswordRequest);
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