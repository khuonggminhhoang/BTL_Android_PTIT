package com.example.foodorderapp.features.jobs.ui.adapter; // Sử dụng package của bạn

import android.content.Context;
import android.content.Intent;
import android.util.Log; // Import Log để debug nếu cần
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

// --- THÊM IMPORT GLIDE ---
import com.bumptech.glide.Glide;

import java.util.List;
import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.core.model.Job; // Import Job model đã sửa
import com.example.foodorderapp.core.model.Company;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    public static final String JOB_DETAIL_KEY = "JOB_DETAIL"; // Key để gửi dữ liệu qua Intent

    // --- (Optional but Recommended) Interface để xử lý click từ Activity/Fragment ---
    public interface OnJobInteractionListener {
        void onJobClick(Job job, int position);
        void onFavoriteToggle(Job job, int position, boolean isNowFavorite);
    }
    private OnJobInteractionListener listener; // Biến listener

    // Constructor có thể nhận thêm listener
    public JobAdapter(Context context, List<Job> jobList) {
        this(context, jobList, null); // Gọi constructor khác với listener là null
    }

    public JobAdapter(Context context, List<Job> jobList, OnJobInteractionListener listener) {
        this.context = context;
        this.jobList = jobList;
        this.listener = listener; // Lưu listener
    }
    // --- Hết phần listener ---


    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        if (job == null) return;

        // Sử dụng getter cho các trường
        Company company = job.getCompany();
        holder.tvCompanyName.setText(company != null ? company.getName() : "");
        holder.tvJobTitle.setText(job.getTitle());
        holder.tvLocation.setText(job.getLocation());
        String salary = job.getSalaryMin() + " - " + job.getSalaryMax() + " / " + job.getSalaryPeriod();
        holder.tvSalary.setText(salary);
        holder.tvPostTime.setText(job.getCreatedAt());

        // Load Logo bằng Glide từ URL
        String logoUrl = company != null ? company.getLogoUrl() : null;
        Glide.with(context)
                .load(logoUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .into(holder.ivCompanyLogo);

        // Cập nhật trạng thái nút favorite
        updateFavoriteIcon(holder.ivFavorite, job.isTopJob());

        // --- Xử lý sự kiện click cho itemView (Dùng listener nếu có) ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJobClick(job, holder.getAdapterPosition());
            } else {
                // Cách cũ: Mở trực tiếp từ Adapter (ít linh hoạt hơn)
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra(JOB_DETAIL_KEY, job); // Job đã implement Serializable
                context.startActivity(intent);
            }
        });

        // --- Xử lý sự kiện click cho nút favorite (Dùng listener nếu có) ---
        holder.ivFavorite.setOnClickListener(v -> {
            boolean isNowFavorite = !job.isTopJob();
            job.setTopJob(isNowFavorite);
            updateFavoriteIcon(holder.ivFavorite, isNowFavorite);

            if (listener != null) {
                // Thông báo cho Activity/Fragment xử lý lưu trữ
                listener.onFavoriteToggle(job, holder.getAdapterPosition(), isNowFavorite);
            } else {
                // Cách cũ: Chỉ hiển thị Toast (không lưu trạng thái)
                Toast.makeText(context, isNowFavorite ? "Đã thêm yêu thích (cần lưu)" : "Đã xóa yêu thích (cần lưu)", Toast.LENGTH_SHORT).show();
                // TODO: Nếu không dùng listener, bạn CẦN thêm logic LƯU trạng thái favorite ở đây
            }
        });
    }

    // Hàm helper để cập nhật icon favorite
    private void updateFavoriteIcon(ImageView imageView, boolean isFavorite) {
        if (isFavorite) {
            imageView.setImageResource(R.drawable.ic_heart_filled_red); // Dùng icon đỏ
            imageView.clearColorFilter(); // Xóa filter nếu có
        } else {
            imageView.setImageResource(R.drawable.ic_favorite_border); // Dùng icon viền
            // Có thể thêm màu xám cho viền nếu icon gốc là trắng
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }


    @Override
    public int getItemCount() {
        return jobList == null ? 0 : jobList.size();
    }

    // ViewHolder (Giữ nguyên)
    public static class JobViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo, ivFavorite;
        TextView tvCompanyName, tvJobTitle, tvLocation, tvSalary, tvPostTime;

        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.ivCompanyLogo);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            tvCompanyName = itemView.findViewById(R.id.tvCompanyName);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSalary = itemView.findViewById(R.id.tvSalary);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
        }
    }

    // (Optional) Hàm để cập nhật dữ liệu từ bên ngoài
    public void updateJobList(List<Job> newJobList) {
        this.jobList = newJobList;
        notifyDataSetChanged();
    }
}