package com.example.foodorderapp.features.jobs.ui.adapter; // Sử dụng package của bạn

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Thêm import này
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList; // Giữ nguyên là ArrayList để dễ dàng thêm/xóa
    public static final String JOB_DETAIL_KEY = "JOB_DETAIL";

    public interface OnJobInteractionListener {
        void onJobClick(Job job, int position);
        void onFavoriteToggle(Job job, int position, boolean isNowFavorite);
    }
    private OnJobInteractionListener listener;

    // Constructor có thể nhận thêm listener
    public JobAdapter(Context context, List<Job> jobList) {
        this(context, jobList, null);
    }

    public JobAdapter(Context context, List<Job> jobList, OnJobInteractionListener listener) {
        this.context = context;
        // Khởi tạo jobList nếu nó là null để tránh NullPointerException
        this.jobList = (jobList == null) ? new ArrayList<>() : jobList;
        this.listener = listener;
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
        if (job == null) return;

        Company company = job.getCompany();
        holder.tvCompanyName.setText(company != null ? company.getName() : "N/A");
        holder.tvJobTitle.setText(job.getTitle());
        holder.tvLocation.setText(job.getLocation());

        String salary = "N/A";
        if (job.getSalaryMin() != null && job.getSalaryMax() != null && job.getSalaryPeriod() != null) {
            salary = job.getSalaryMin() + " - " + job.getSalaryMax() + " / " + job.getSalaryPeriod();
        } else if (job.getSalaryMin() != null && job.getSalaryPeriod() != null) {
            salary = job.getSalaryMin() + " / " + job.getSalaryPeriod();
        }
        holder.tvSalary.setText(salary);

        String createdAt = job.getCreatedAt();
        holder.tvPostTime.setText(formatDate(createdAt));

        String logoUrl = company != null ? company.getLogoUrl() : null;
        Glide.with(context)
                .load(logoUrl)
                .placeholder(R.mipmap.ic_launcher) // Ảnh placeholder mặc định
                .error(R.mipmap.ic_launcher_round) // Ảnh lỗi mặc định
                .into(holder.ivCompanyLogo);

        updateFavoriteIcon(holder.ivFavorite, job.isTopJob()); // Giả sử isTopJob là trạng thái yêu thích

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJobClick(job, holder.getAdapterPosition());
            } else {
                Intent intent = new Intent(context, JobDetailActivity.class);
                intent.putExtra(JOB_DETAIL_KEY, job);
                context.startActivity(intent);
            }
        });

        holder.ivFavorite.setOnClickListener(v -> {
            boolean isNowFavorite = !job.isTopJob();
            job.setTopJob(isNowFavorite); // Cập nhật trạng thái trong model
            updateFavoriteIcon(holder.ivFavorite, isNowFavorite);

            if (listener != null) {
                listener.onFavoriteToggle(job, holder.getAdapterPosition(), isNowFavorite);
            } else {
                // TODO: Xử lý lưu trạng thái yêu thích vào SharedPreferences hoặc Database nếu không dùng listener
                Toast.makeText(context, isNowFavorite ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon(ImageView imageView, boolean isFavorite) {
        if (isFavorite) {
            imageView.setImageResource(R.drawable.ic_heart_filled_red);
            imageView.clearColorFilter();
        } else {
            imageView.setImageResource(R.drawable.ic_favorite_border);
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "N/A";
        // Thử các định dạng phổ biến mà API có thể trả về
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // ISO 8601 với milliseconds và Z timezone
                "yyyy-MM-dd'T'HH:mm:ss'Z'",    // ISO 8601 không có milliseconds và Z timezone
                "yyyy-MM-dd HH:mm:ss",         // Định dạng phổ biến khác
                "yyyy-MM-dd"                   // Chỉ ngày
        };

        for (String fmt : formats) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat(fmt, Locale.US);
                // Quan trọng: Đặt TimeZone là UTC nếu API trả về thời gian UTC (có 'Z')
                if (fmt.endsWith("'Z'")) {
                    isoFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }
                Date date = isoFormat.parse(isoDate);
                if (date != null) {
                    // Chuyển đổi sang định dạng hiển thị mong muốn (ví dụ: "dd/MM/yyyy" hoặc "MMM dd, yyyy")
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                }
            } catch (ParseException ignored) {
                // Thử định dạng tiếp theo nếu parse lỗi
            }
        }
        // Nếu không parse được với các định dạng đã biết, trả về chuỗi gốc hoặc một giá trị mặc định
        return isoDate; // Hoặc "Unknown date"
    }


    @Override
    public int getItemCount() {
        return jobList == null ? 0 : jobList.size();
    }

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

    // --- CÁC PHƯƠNG THỨC MỚI CHO PHÂN TRANG ---

    /**
     * Thêm danh sách công việc mới vào cuối danh sách hiện tại.
     * @param newJobs Danh sách công việc mới cần thêm.
     */
    public void addJobs(List<Job> newJobs) {
        if (newJobs != null) {
            int startPosition = jobList.size();
            jobList.addAll(newJobs);
            notifyItemRangeInserted(startPosition, newJobs.size());
        }
    }

    /**
     * Xóa tất cả công việc khỏi danh sách.
     */
    public void clearJobs() {
        if (jobList != null) {
            jobList.clear();
            notifyDataSetChanged();
        }
    }

    /**
     * Cập nhật toàn bộ danh sách công việc.
     * Dùng khi tải lại dữ liệu từ đầu hoặc áp dụng bộ lọc.
     * @param newJobList Danh sách công việc mới.
     */
    public void updateJobList(List<Job> newJobList) {
        this.jobList = (newJobList == null) ? new ArrayList<>() : newJobList;
        notifyDataSetChanged();
    }
}
