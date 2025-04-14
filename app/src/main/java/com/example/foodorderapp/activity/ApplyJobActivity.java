package com.example.foodorderapp.activity; // Package của bạn

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog; // Import Dialog
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater; // Import LayoutInflater
import android.view.View; // Import View
import android.view.ViewGroup;
import android.view.Window; // Import Window
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodorderapp.R;
import com.example.foodorderapp.adapter.JobAdapter;
import com.example.foodorderapp.model.Job;

public class ApplyJobActivity extends AppCompatActivity {

    // ... (Các biến thành viên giữ nguyên) ...
    private ImageView ivCompanyLogoApply;
    private TextView tvCompanyNameApply, tvJobTitleApply, tvUploadHint;
    private ImageButton btnBackApply, btnFavoriteApply;
    private LinearLayout layoutUploadResume;
    private EditText etPhoneNumber, etCoverLetter;
    private Button btnApplyNow;

    private Job currentJob;
    private Uri selectedFileUri;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        String fileName = getFileNameFromUri(selectedFileUri);
                        // Cập nhật TextView hint để hiển thị tên file
                        if(tvUploadHint != null) { // Kiểm tra null trước khi dùng
                            tvUploadHint.setText(getString(R.string.file_selected_label, fileName));
                            tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.blue_primary));
                        }
                        Toast.makeText(this, "Đã chọn file: " + fileName, Toast.LENGTH_SHORT).show();
                    } else {
                        // Reset text nếu không lấy được URI
                        if(tvUploadHint != null) {
                            tvUploadHint.setText(getString(R.string.upload_resume_hint));
                            tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.grey));
                        }
                    }
                } else {
                    // Reset text nếu người dùng hủy chọn file
                    if(tvUploadHint != null) {
                        tvUploadHint.setText(getString(R.string.upload_resume_hint));
                        tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.grey));
                    }
                    selectedFileUri = null; // Đảm bảo URI là null nếu không chọn file
                    Toast.makeText(this, "Chưa chọn file nào", Toast.LENGTH_SHORT).show();
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);

        findViews();
        getIntentData();

        if (currentJob != null) {
            populateJobSummary();
            updateFavoriteButton(currentJob.isFavorite());
        } else {
            Toast.makeText(this, "Không thể tải thông tin công việc.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
    }

    private void findViews() {
        // ... (Ánh xạ các view khác giữ nguyên) ...
        ivCompanyLogoApply = findViewById(R.id.ivCompanyLogoApply);
        tvCompanyNameApply = findViewById(R.id.tvCompanyNameApply);
        tvJobTitleApply = findViewById(R.id.tvJobTitleApply);
        btnBackApply = findViewById(R.id.btnBackApply);
        btnFavoriteApply = findViewById(R.id.btnFavoriteApply);
        layoutUploadResume = findViewById(R.id.layoutUploadResume);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etCoverLetter = findViewById(R.id.etCoverLetter);
        btnApplyNow = findViewById(R.id.btnApplyNow);
        // Tìm TextView hint bên trong LinearLayout
        tvUploadHint = findViewById(R.id.tvUploadHint); // Đảm bảo ID này tồn tại trong XML
    }

    private void getIntentData() {
        // ... (Giữ nguyên) ...
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(JobAdapter.JOB_DETAIL_KEY)) {
            currentJob = (Job) intent.getSerializableExtra(JobAdapter.JOB_DETAIL_KEY);
        }
    }

    private void populateJobSummary() {
        // ... (Giữ nguyên) ...
        ivCompanyLogoApply.setImageResource(currentJob.getCompanyLogoResId());
        tvCompanyNameApply.setText(currentJob.getCompanyName());
        tvJobTitleApply.setText(currentJob.getJobTitle());
    }

    private void updateFavoriteButton(boolean isFavorite) {
        // ... (Giữ nguyên) ...
        if (isFavorite) {
            btnFavoriteApply.setImageResource(R.drawable.ic_heart_filled);
            btnFavoriteApply.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            btnFavoriteApply.setImageResource(R.drawable.ic_favorite_border);
            btnFavoriteApply.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void setupClickListeners() {
        btnBackApply.setOnClickListener(v -> finish());

        btnFavoriteApply.setOnClickListener(v -> {
            // ... (Logic nút favorite giữ nguyên) ...
            boolean isCurrentlyFavorite = currentJob.isFavorite();
            boolean isNowFavorite = !isCurrentlyFavorite;
            updateFavoriteButton(isNowFavorite);
            currentJob.setFavorite(isNowFavorite);
            // TODO: Lưu trạng thái isNowFavorite vào DB/SharedPreferences
            Toast.makeText(this, isNowFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });

        layoutUploadResume.setOnClickListener(v -> {
            openFilePicker();
        });

        // --- Cập nhật Listener cho nút Apply Now ---
        btnApplyNow.setOnClickListener(v -> {
            // 1. Kiểm tra xem file đã được chọn chưa
//            if (selectedFileUri == null) {
//                Toast.makeText(this, "Vui lòng tải lên CV của bạn", Toast.LENGTH_SHORT).show();
//                return; // Dừng lại nếu chưa chọn file
//            }

            // 2. (Tùy chọn) Kiểm tra các trường khác nếu cần
            String phone = etPhoneNumber.getText().toString().trim();
            String cover = etCoverLetter.getText().toString().trim();
            // if (phone.isEmpty()) { ... }

            // 3. Nếu hợp lệ, hiển thị dialog thành công
            // TODO: Trong ứng dụng thực tế, đây là lúc gọi API upload file và gửi đơn ứng tuyển
            showSuccessDialog();

        });
    }

    // Phương thức hiển thị Dialog chúc mừng
    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Bỏ tiêu đề mặc định của Dialog
        dialog.setContentView(R.layout.dialog_apply_success); // Set layout tùy chỉnh

        // Tìm các view bên trong dialog
        TextView tvSuccessMessage = dialog.findViewById(R.id.tvSuccessMessage);
        Button btnContinueExplore = dialog.findViewById(R.id.btnContinueExplore);
        ImageView ivSuccessIllustration = dialog.findViewById(R.id.ivSuccessIllustration); // Ví dụ nếu cần tương tác

        // Tạo nội dung message động
        String successMsg = getString(R.string.success_message_format,
                currentJob.getCompanyName(),
                currentJob.getJobTitle());
        tvSuccessMessage.setText(successMsg);

        // Xử lý nút "Continue Explore"
        btnContinueExplore.setOnClickListener(v -> {
            dialog.dismiss(); // Đóng dialog
            finish(); // Đóng ApplyJobActivity
        });

        // Thiết lập thuộc tính cho dialog (tùy chọn)
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Nền trong suốt để thấy bo góc của layout
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); // Chiều rộng tối đa, chiều cao tự động
        }
        dialog.setCancelable(false); // Không cho phép đóng dialog bằng cách nhấn ra ngoài hoặc nút back
        dialog.setCanceledOnTouchOutside(false);

        dialog.show(); // Hiển thị dialog
    }


    private void openFilePicker() {
        // ... (Giữ nguyên) ...
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Chọn CV của bạn"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Vui lòng cài đặt trình quản lý file.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        // ... (Giữ nguyên) ...
        String fileName = null;
        android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName != null ? fileName : "unknown_file";
    }

}

