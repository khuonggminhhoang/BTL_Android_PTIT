package com.example.foodorderapp.features.auth.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foodorderapp.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.foodorderapp.config.Config;

import java.util.Locale;

public class OtpVerificationActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvSubtitle, tvTimer, tvResend;
    private EditText etOtp1, etOtp2, etOtp3, etOtp4;
    private Button btnVerify;
    private CountDownTimer countDownTimer;
    private String userEmail;

    private static final long COUNTDOWN_TIME = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        userEmail = getIntent().getStringExtra("email");

        findViews();
        setupOtpInput();
        setupClickListeners();

        if (userEmail != null && !userEmail.isEmpty()) {
            String maskedEmail = maskEmail(userEmail);
            tvSubtitle.setText(getString(R.string.otp_subtitle_format, maskedEmail));
        } else {
            tvSubtitle.setText(getString(R.string.otp_subtitle_default));
        }

        startTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }


    private void findViews() {
        btnBack = findViewById(R.id.otp_btn_back);
        tvSubtitle = findViewById(R.id.otp_txt_subtitle);
        etOtp1 = findViewById(R.id.otp_edt_text1);
        etOtp2 = findViewById(R.id.otp_edt_text2);
        etOtp3 = findViewById(R.id.otp_edt_text3);
        etOtp4 = findViewById(R.id.otp_edt_text4);
        tvTimer = findViewById(R.id.otp_txt_timer);
        tvResend = findViewById(R.id.otp_txt_resend_code);
        btnVerify = findViewById(R.id.otp_btn_verify);
    }

    // Hàm che email (ví dụ đơn giản)
    private String maskEmail(String email) {
        // Ví dụ: maulana***@gmail.com
        try {
            int atIndex = email.indexOf('@');
            if (atIndex > 3) {
                return email.substring(0, 3) + "***" + email.substring(atIndex);
            }
        } catch (Exception e) {
            // Handle error or return original email
        }
        return email;
    }

    private void setupOtpInput() {
        EditText[] otpEditTexts = {etOtp1, etOtp2, etOtp3, etOtp4};

        for (int i = 0; i < otpEditTexts.length; i++) {
            final int currentIndex = i;
            otpEditTexts[currentIndex].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < otpEditTexts.length - 1) {
                        otpEditTexts[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && before == 1 && currentIndex > 0) {
                        otpEditTexts[currentIndex - 1].requestFocus();
                        otpEditTexts[currentIndex - 1].setSelection(otpEditTexts[currentIndex - 1].length());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            otpEditTexts[currentIndex].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (otpEditTexts[currentIndex].getText().toString().isEmpty() && currentIndex > 0) {
                        otpEditTexts[currentIndex - 1].requestFocus();
                        // otpEditTexts[currentIndex - 1].setText("");
                        return true;
                    }
                }
                return false;
            });
        }
        etOtp1.requestFocus();
    }


    private void startTimer() {
        tvResend.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(String.format(Locale.getDefault(), "0:%02d", seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvResend.setVisibility(View.VISIBLE);
                tvResend.setTextColor(ContextCompat.getColor(OtpVerificationActivity.this, R.color.blue_primary));
                tvResend.setEnabled(true);
            }
        }.start();
        tvResend.setEnabled(false);
        tvResend.setTextColor(ContextCompat.getColor(OtpVerificationActivity.this, R.color.grey));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnVerify.setOnClickListener(v -> {
            String otp1 = etOtp1.getText().toString();
            String otp2 = etOtp2.getText().toString();
            String otp3 = etOtp3.getText().toString();
            String otp4 = etOtp4.getText().toString();

            if (otp1.isEmpty() || otp2.isEmpty() || otp3.isEmpty() || otp4.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            String enteredOtp = otp1 + otp2 + otp3 + otp4;

            // Gọi API xác thực OTP
            String url = Config.BE_URL + "/auth/verify-otp";
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("email", userEmail);
                requestData.put("otp", enteredOtp);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest verifyOtpRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    JSONObject data = response.optJSONObject("data");
                    boolean isSuccess = false;
                    if (data != null) {
                        isSuccess = data.optBoolean("isVerified", false);
                    }
                    if (isSuccess) {
                        String verifiedEmail = data.optString("email", "");
                        Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("email", verifiedEmail);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Mã OTP không đúng", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Có lỗi xảy ra. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            );

            RequestQueue queue = Volley.newRequestQueue(OtpVerificationActivity.this);
            queue.add(verifyOtpRequest);
        });

        tvResend.setOnClickListener(v -> {
            String url = Config.BE_URL + "/auth/forgot-password";
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("email", userEmail);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest resendOtpRequest = new JsonObjectRequest(Request.Method.POST, url, requestData,
                response -> {
                    Toast.makeText(this, "Đã gửi lại mã OTP về email!", Toast.LENGTH_SHORT).show();
                    startTimer();
                },
                error -> {
                    Toast.makeText(this, "Gửi lại mã OTP thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            );

            RequestQueue queue = Volley.newRequestQueue(OtpVerificationActivity.this);
            queue.add(resendOtpRequest);
        });
    }
}