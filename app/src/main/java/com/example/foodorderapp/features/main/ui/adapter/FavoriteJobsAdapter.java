package com.example.foodorderapp.features.main.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.Job;
import com.example.foodorderapp.config.Config; // Để lấy URL cơ sở nếu logo là tương đối

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteJobsAdapter extends RecyclerView.Adapter<FavoriteJobsAdapter.ViewHolder> implements Filterable {

    private List<Job> favoriteJobs; // Danh sách gốc
    private List<Job> favoriteJobsFiltered; // Danh sách đã lọc để hiển thị
    private final Context context;
    private final OnFavoriteClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnFavoriteClickListener {
        void onUnfavoriteClick(Job job, int position);
        void onItemClick(Job job, int position);
    }

    public FavoriteJobsAdapter(Context context, List<Job> favoriteJobs, OnFavoriteClickListener listener) {
        this.context = context;
        this.favoriteJobs = favoriteJobs != null ? new ArrayList<>(favoriteJobs) : new ArrayList<>();
        this.favoriteJobsFiltered = new ArrayList<>(this.favoriteJobs); // Khởi tạo danh sách lọc ban đầu
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_job, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Job job = favoriteJobsFiltered.get(position); // Luôn sử dụng danh sách đã lọc

        holder.tvJobTitle.setText(job.getTitle());
        String companyName = (job.getCompany() != null && job.getCompany().getName() != null) ? job.getCompany().getName() : "N/A";
        String location = (job.getLocation() != null) ? job.getLocation() : "N/A";
        String companyLocation = companyName + " • " + location;
        holder.tvCompanyLocation.setText(companyLocation);

        // Tải logo công ty bằng Glide
        String logoUrl = null;
        if (job.getCompany() != null && job.getCompany().getLogoUrl() != null && !job.getCompany().getLogoUrl().isEmpty()) {
            logoUrl = job.getCompany().getLogoUrl();
            // Kiểm tra xem URL là tương đối hay tuyệt đối
            if (!logoUrl.toLowerCase().startsWith("http://") && !logoUrl.toLowerCase().startsWith("https://")) {
                // Nối với URL cơ sở nếu là đường dẫn tương đối
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
                if (imageBaseUrl.endsWith("/")) {
                    imageBaseUrl = imageBaseUrl.substring(0, imageBaseUrl.length() - 1);
                }
                if (logoUrl.startsWith("/")) {
                    logoUrl = logoUrl.substring(1);
                }
                logoUrl = imageBaseUrl + "/" + logoUrl;
            }
        }

        Glide.with(context)
                .load(logoUrl) // Glide xử lý URL null bằng cách hiển thị lỗi/placeholder
                .placeholder(R.drawable.ic_company_logo_placeholder) // Nên có một placeholder chung
                .error(R.drawable.ic_company_logo_placeholder)       // Và một ảnh lỗi chung
                .circleCrop() // Nếu muốn ảnh tròn
                .into(holder.ivCompanyLogo);

        // Xử lý click bỏ yêu thích
        holder.ivFavoriteHeart.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition(); // Sử dụng getAdapterPosition() để an toàn
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onUnfavoriteClick(favoriteJobsFiltered.get(currentPosition), currentPosition);
                }
            }
        });

        // Xử lý click item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition(); // Sử dụng getAdapterPosition()
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(favoriteJobsFiltered.get(currentPosition), currentPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteJobsFiltered == null ? 0 : favoriteJobsFiltered.size();
    }

    // Cập nhật danh sách công việc
    public void updateData(List<Job> newFavoriteJobs) {
        this.favoriteJobs = newFavoriteJobs != null ? new ArrayList<>(newFavoriteJobs) : new ArrayList<>();
        this.favoriteJobsFiltered = new ArrayList<>(this.favoriteJobs); // Cập nhật lại cả danh sách lọc
        notifyDataSetChanged();
    }

    // Xóa công việc khỏi danh sách (cả danh sách gốc và danh sách đã lọc)
    public void removeItem(int positionInFilteredList) {
        if (positionInFilteredList >= 0 && positionInFilteredList < favoriteJobsFiltered.size()) {
            Job removedJob = favoriteJobsFiltered.remove(positionInFilteredList);
            // Cũng cần xóa khỏi danh sách gốc để đảm bảo tính nhất quán
            if (favoriteJobs != null) {
                favoriteJobs.remove(removedJob);
            }
            notifyItemRemoved(positionInFilteredList);
            // Cập nhật lại range để tránh lỗi IndexOutOfBoundsException
            notifyItemRangeChanged(positionInFilteredList, favoriteJobsFiltered.size());
        }
    }

    // ViewHolder cho item
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo;
        TextView tvJobTitle;
        TextView tvCompanyLocation;
        ImageView ivFavoriteHeart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.iv_company_logo);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title);
            tvCompanyLocation = itemView.findViewById(R.id.tv_company_location);
            ivFavoriteHeart = itemView.findViewById(R.id.iv_favorite_heart);
        }
    }

    // Bộ lọc danh sách công việc
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Job> filteredList = new ArrayList<>();
                String filterPattern = constraint == null ? "" : constraint.toString().toLowerCase(Locale.getDefault()).trim();

                if (filterPattern.isEmpty()) {
                    filteredList.addAll(favoriteJobs); // Nếu không có filter, hiển thị tất cả từ danh sách gốc
                } else {
                    for (Job job : favoriteJobs) { // Lọc từ danh sách gốc
                        boolean titleMatches = job.getTitle() != null && job.getTitle().toLowerCase(Locale.getDefault()).contains(filterPattern);
                        boolean companyMatches = job.getCompany() != null && job.getCompany().getName() != null && job.getCompany().getName().toLowerCase(Locale.getDefault()).contains(filterPattern);
                        if (titleMatches || companyMatches) {
                            filteredList.add(job);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                favoriteJobsFiltered.clear();
                if (results.values != null) {
                    favoriteJobsFiltered.addAll((List) results.values);
                }
                notifyDataSetChanged(); // Thông báo cho adapter về sự thay đổi dữ liệu
            }
        };
    }
}
