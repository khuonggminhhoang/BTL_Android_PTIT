package com.example.foodorderapp.features.auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import org.json.JSONException;
import org.json.JSONObject;

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
        btnBack.setOnClickListener(v -> finish());

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

            String url = Config.BE_URL + "/auth/forgot-password";
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest forgotPasswordRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu reset mật khẩu đã được gửi đến " + email, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, OtpVerificationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(ForgotPasswordActivity.this, "Gửi yêu cầu thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            );

            RequestQueue queue = Volley.newRequestQueue(ForgotPasswordActivity.this);
            queue.add(forgotPasswordRequest);
        });
    }
}