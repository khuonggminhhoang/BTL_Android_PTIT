package com.example.foodorderapp.features.jobs.ui.activity; // Package của bạn

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor; // <<< THÊM IMPORT CURSOR
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns; // <<< THÊM IMPORT OPENABLECOLUMNS
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// --- THÊM IMPORT GLIDE ---
import com.bumptech.glide.Glide;

import com.example.foodorderapp.R;
// Đảm bảo import đúng JobAdapter và Job model
import com.example.foodorderapp.features.jobs.ui.adapter.JobAdapter; // Kiểm tra lại đường dẫn nếu cần
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.features.main.ui.activity.MainActivity;

public class ApplyJobActivity extends AppCompatActivity {

    private ImageView ivCompanyLogoApply;
    private TextView tvCompanyNameApply, tvJobTitleApply, tvUploadHint;
    private ImageButton btnBackApply, btnFavoriteApply;
    private LinearLayout layoutUploadResume;
    private EditText etPhoneNumber, etCoverLetter;
    private Button btnApplyNow;

    private Job currentJob; // Sử dụng Job model đã sửa (có id và companyLogoUrl)
    private Uri selectedFileUri;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedFileUri = result.getData().getData();
                    if (selectedFileUri != null) {
                        String fileName = getFileNameFromUri(selectedFileUri);
                        if(tvUploadHint != null) {
                            tvUploadHint.setText(getString(R.string.file_selected_label, fileName)); // Cần định nghĩa string này
                            // Đổi màu chữ để báo hiệu đã chọn file
                            tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.setting_icon_tint)); // <<< Hoặc màu phù hợp khác
                        }
                        Toast.makeText(this, "Đã chọn file: " + fileName, Toast.LENGTH_SHORT).show();
                    } else {
                        resetUploadHint();
                    }
                } else {
                    resetUploadHint();
                    selectedFileUri = null;
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
            populateJobSummary(); // <<< HÀM NÀY SẼ ĐƯỢC SỬA
//            updateFavoriteButton(currentJob.isFavorite());
            updateFavoriteButton(true);
        } else {
            Toast.makeText(this, "Không thể tải thông tin công việc.", Toast.LENGTH_SHORT).show();
            finish(); // Thoát nếu không có dữ liệu Job
            return;
        }

        setupClickListeners();
    }

    private void findViews() {
        ivCompanyLogoApply = findViewById(R.id.ivCompanyLogoApply);
        tvCompanyNameApply = findViewById(R.id.tvCompanyNameApply);
        tvJobTitleApply = findViewById(R.id.tvJobTitleApply);
        btnBackApply = findViewById(R.id.btnBackApply);
        btnFavoriteApply = findViewById(R.id.btnFavoriteApply);
        layoutUploadResume = findViewById(R.id.layoutUploadResume);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etCoverLetter = findViewById(R.id.etCoverLetter);
        btnApplyNow = findViewById(R.id.btnApplyNow);
        tvUploadHint = findViewById(R.id.tvUploadHint); // Đảm bảo ID này có trong R.layout.activity_apply_job
    }

    private void getIntentData() {
        Intent intent = getIntent();
        // Kiểm tra key truyền từ JobAdapter (hoặc nơi gọi)
        String jobDetailKey = JobAdapter.JOB_DETAIL_KEY; // <<< Hoặc key bạn dùng để truyền Job
        if (intent != null && intent.hasExtra(jobDetailKey)) {
            // Nhận Job object đã được sửa (Serializable)
            currentJob = (Job) intent.getSerializableExtra(jobDetailKey);
        }
    }

    private void populateJobSummary() {
        // --- SỬA Ở ĐÂY: Dùng Glide để load ảnh từ URL ---
        String logoUrl = currentJob.getCompany() != null ? currentJob.getCompany().getLogoUrl() : null;
        Glide.with(this) // Context là Activity này
                .load(logoUrl) // Load từ URL
                .placeholder(R.mipmap.ic_launcher) // Ảnh tạm (thay bằng ảnh phù hợp)
                .error(R.mipmap.ic_launcher_round)      // Ảnh lỗi (thay bằng ảnh phù hợp)
                // .circleCrop() // Tùy chọn: Bo tròn nếu cần
                .into(ivCompanyLogoApply); // ImageView đích

        // --- Các phần khác giữ nguyên ---
        tvCompanyNameApply.setText(currentJob.getCompany() != null ? currentJob.getCompany().getName() : "");
        tvJobTitleApply.setText(currentJob.getTitle());
    }

    private void updateFavoriteButton(boolean isFavorite) {
        if (isFavorite) {
            btnFavoriteApply.setImageResource(R.drawable.ic_heart_filled_red); // Dùng icon trái tim đỏ đã tạo
            // Không cần setColorFilter nếu drawable đã có màu đỏ
            // btnFavoriteApply.setColorFilter(ContextCompat.getColor(this, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            // Cần icon trái tim viền (tạo drawable ic_heart_outline_gray.xml hoặc tương tự)
            btnFavoriteApply.setImageResource(R.drawable.ic_favorite_border); // Thay bằng icon viền phù hợp
            // btnFavoriteApply.clearColorFilter(); // Xóa filter nếu có
            // Hoặc set màu viền nếu icon là vector trắng
            // btnFavoriteApply.setColorFilter(ContextCompat.getColor(this, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void setupClickListeners() {
        btnBackApply.setOnClickListener(v -> finish());

        btnFavoriteApply.setOnClickListener(v -> {
//            if (currentJob == null) return; // Kiểm tra job không null
//
//            boolean isNowFavorite = !currentJob.isFavorite(); // Đảo trạng thái
//            currentJob.setFavorite(isNowFavorite); // Cập nhật trạng thái trong object Job
//            updateFavoriteButton(isNowFavorite); // Cập nhật UI nút favorite
//
//            // TODO: Lưu trạng thái isNowFavorite và currentJob.getId() vào DB/SharedPreferences
//            Toast.makeText(this, isNowFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });

        layoutUploadResume.setOnClickListener(v -> openFilePicker());

        btnApplyNow.setOnClickListener(v -> {
            if (currentJob == null) {
                Toast.makeText(this, "Lỗi thông tin công việc.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedFileUri == null) {
                Toast.makeText(this, "Vui lòng tải lên CV của bạn", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Thực hiện logic ứng tuyển (upload file, gọi API...)
            // Sau khi xử lý thành công:
            showSuccessDialog();
        });
    }

    private void showSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_apply_success); // Đảm bảo layout này tồn tại

        TextView tvSuccessMessage = dialog.findViewById(R.id.tvSuccessMessage);
        Button btnContinueExplore = dialog.findViewById(R.id.btnContinueExplore);

        // Lấy đúng tên công ty và tiêu đề từ currentJob
        String successMsg = getString(R.string.success_message_format, // Cần định nghĩa string này
                currentJob.getCompany() != null ? currentJob.getCompany().getName() : "",
                currentJob.getTitle());
        tvSuccessMessage.setText(successMsg);

        btnContinueExplore.setOnClickListener(v -> {
            dialog.dismiss();
            // Có thể quay lại màn hình trước đó hoặc màn hình chính
            // finish(); // Đóng màn hình apply
            Intent mainIntent = new Intent(this, MainActivity.class); // <<< Hoặc Activity chính của bạn
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish(); // Đóng activity hiện tại
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // Giới hạn kiểu file nếu muốn, ví dụ PDF và DOCX:
        intent.setType("*/*"); // Cho phép chọn mọi loại file
        // String[] mimeTypes = {"application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        // intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // intent.addCategory(Intent.CATEGORY_OPENABLE); // Quan trọng khi dùng ACTION_GET_CONTENT

        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Chọn CV/Resume"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Vui lòng cài đặt trình quản lý file.", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm reset text hint upload
    private void resetUploadHint() {
        if(tvUploadHint != null) {
            tvUploadHint.setText(getString(R.string.upload_resume_hint)); // Cần định nghĩa string này
            tvUploadHint.setTextColor(ContextCompat.getColor(this, R.color.grey)); // Màu mặc định
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        // Thử lấy tên file từ ContentResolver trước
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e("ApplyJobActivity", "Error getting file name from ContentResolver", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Nếu không lấy được từ ContentResolver, thử lấy từ path
        if (fileName == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    fileName = path.substring(cut + 1);
                } else {
                    fileName = path; // Nếu path không có dấu /
                }
            }
        }
        return fileName != null ? fileName : "unknown_file"; // Trả về tên mặc định nếu không tìm được
    }
}