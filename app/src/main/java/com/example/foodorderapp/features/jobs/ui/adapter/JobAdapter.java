package com.example.foodorderapp.features.jobs.ui.adapter;

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
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.jobs.ui.activity.JobDetailActivity;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.core.model.Company;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

    private Context context;
    private List<Job> jobList;
    public static final String JOB_DETAIL_KEY = "JOB_DETAIL";

    public interface OnJobInteractionListener {
        void onJobClick(Job job, int position);
        void onFavoriteToggle(Job job, int position, boolean isNowFavorite);
    }
    private OnJobInteractionListener listener;

    public JobAdapter(Context context, List<Job> jobList, OnJobInteractionListener listener) {
        this.context = context;
        this.jobList = (jobList == null) ? new ArrayList<>() : jobList;
        this.listener = listener;
    }

    // Constructor cũ (nếu vẫn cần thiết ở đâu đó, nhưng nên dùng constructor có listener)
    public JobAdapter(Context context, List<Job> jobList) {
        this(context, jobList, null);
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

        String logoUrl = null;
        if (company != null && company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
            logoUrl = company.getLogoUrl();
            if (!logoUrl.toLowerCase().startsWith("http")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                logoUrl = imageBaseUrl + (logoUrl.startsWith("/") ? "" : "/") + logoUrl;
            }
        }

        Glide.with(context)
                .load(logoUrl) // Nếu logoUrl null, Glide sẽ tự xử lý (có thể hiển thị placeholder/error)
                .placeholder(R.drawable.ic_company_logo_placeholder)
                .error(R.drawable.ic_company_logo_placeholder)
                .into(holder.ivCompanyLogo);

        updateFavoriteIcon(holder.ivFavorite, job.isFavorite());

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
            if (listener != null) {
                boolean isNowFavorite = !job.isFavorite();
                // Không thay đổi job.setFavorite(isNowFavorite) ở đây.
                // HomeFragment sẽ xử lý việc này sau khi API call thành công.
                listener.onFavoriteToggle(job, holder.getAdapterPosition(), isNowFavorite);
            } else {
                Toast.makeText(context, "Favorite listener not set", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon(ImageView imageView, boolean isFavorite) {
        if (isFavorite) {
            imageView.setImageResource(R.drawable.ic_heart_filled_red);
            imageView.clearColorFilter(); // Xóa tint nếu icon đã có màu đỏ
        } else {
            imageView.setImageResource(R.drawable.ic_favorite_border); // Icon viền
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.grey), android.graphics.PorterDuff.Mode.SRC_IN); // Màu xám cho viền
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null) return "N/A";
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String fmt : formats) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat(fmt, Locale.US);
                if (fmt.endsWith("'Z'")) {
                    isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date date = isoFormat.parse(isoDate);
                if (date != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
                }
            } catch (ParseException ignored) {
            }
        }
        return isoDate;
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

    public void addJobs(List<Job> newJobs) {
        if (newJobs != null) {
            int startPosition = jobList.size();
            jobList.addAll(newJobs);
            notifyItemRangeInserted(startPosition, newJobs.size());
        }
    }

    public void clearJobs() {
        if (jobList != null) {
            jobList.clear();
            notifyDataSetChanged();
        }
    }

    public void updateJobList(List<Job> newJobList) {
        this.jobList = (newJobList == null) ? new ArrayList<>() : newJobList;
        notifyDataSetChanged();
    }

    // Phương thức để cập nhật một item cụ thể (ví dụ sau khi thay đổi trạng thái favorite)
    public void updateJobItem(int position, Job job) {
        if (position >= 0 && position < jobList.size()) {
            jobList.set(position, job);
            notifyItemChanged(position);
        }
    }
}
