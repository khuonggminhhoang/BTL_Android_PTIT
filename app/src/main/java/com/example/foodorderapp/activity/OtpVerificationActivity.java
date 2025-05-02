package com.example.foodorderapp.activity;

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

        userEmail = getIntent().getStringExtra("USER_EMAIL"); // Đảm bảo key này được dùng khi gọi Intent

        findViews();
        setupOtpInput();
        setupClickListeners();

        // Cập nhật subtitle
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
        // Hủy timer để tránh memory leak
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

            // TODO: Thực hiện logic xác thực OTP ở đây (so sánh với mã đã gửi)
            // Ví dụ:
            if (verifyOtp(enteredOtp)) {
                Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OtpVerificationActivity.this, ResetPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Mã OTP không đúng", Toast.LENGTH_SHORT).show();
            }
        });

        tvResend.setOnClickListener(v -> {
            // TODO: Thực hiện logic gửi lại mã OTP
            Toast.makeText(this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();
            startTimer();
        });
    }

    // Hàm giả lập xác thực OTP
    private boolean verifyOtp(String otp) {
        // TODO: Thay thế bằng logic kiểm tra OTP thực tế
        // Ví dụ: return otp.equals("1234");
        return true; // Luôn đúng để test
    }
}