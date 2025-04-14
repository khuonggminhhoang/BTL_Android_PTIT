package com.example.foodorderapp.adapter; // Sử dụng package của bạn

import android.content.Context;
import android.content.Intent; // Import Intent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Import Toast để debug (tùy chọn)

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.foodorderapp.R; // Đảm bảo import đúng R
import com.example.foodorderapp.activity.JobDetailActivity; // Import JobDetailActivity
import com.example.foodorderapp.model.Job;

// Adapter cho RecyclerView hiển thị công việc
public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    public static final String JOB_DETAIL_KEY = "JOB_DETAIL"; // Key để gửi dữ liệu qua Intent

    // Constructor
    public JobAdapter(Context context, List<Job> jobList) {
        this.context = context;
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job, parent, false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobList.get(position);
        if (job == null) return; // Kiểm tra null đề phòng

        holder.tvCompanyName.setText(job.getCompanyName());
        holder.tvJobTitle.setText(job.getJobTitle());
        holder.tvLocation.setText(job.getLocation());
        holder.tvSalary.setText(job.getSalary());
        holder.tvPostTime.setText(job.getPostTime());
        holder.ivCompanyLogo.setImageResource(job.getCompanyLogoResId());

        if (job.isFavorite()) {
            holder.ivFavorite.setImageResource(R.drawable.ic_heart_filled);
            holder.ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.red), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
            holder.ivFavorite.setColorFilter(ContextCompat.getColor(context, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // --- Thêm xử lý sự kiện click cho itemView ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, JobDetailActivity.class);
            // Truyền toàn bộ đối tượng Job (đã implement Serializable)
            intent.putExtra(JOB_DETAIL_KEY, job);
            context.startActivity(intent);
            // Toast.makeText(context, "Clicked: " + job.getJobTitle(), Toast.LENGTH_SHORT).show(); // Debug
        });

        // --- Thêm xử lý sự kiện click cho nút favorite (nếu muốn thay đổi trạng thái ngay tại đây) ---
        holder.ivFavorite.setOnClickListener(v -> {
            // TODO: Xử lý logic thay đổi trạng thái favorite (ví dụ: cập nhật trong database/shared preferences và thay đổi icon)
            // Ví dụ đơn giản: Đảo ngược trạng thái và cập nhật lại item
            job.setFavorite(!job.isFavorite());
            notifyItemChanged(position); // Cập nhật lại item view này
            Toast.makeText(context, "Favorite clicked: " + job.getJobTitle(), Toast.LENGTH_SHORT).show(); // Debug
        });
    }

    @Override
    public int getItemCount() {
        return jobList == null ? 0 : jobList.size();
    }

    // ViewHolder
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
}