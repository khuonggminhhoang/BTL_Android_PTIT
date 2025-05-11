package com.example.foodorderapp.features.main.ui.adapter; // Hoặc package của bạn

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
// ... các import khác ...
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <<< THÊM IMPORT GLIDE
import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.Job; // Import model Job đã sửa

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteJobsAdapter extends RecyclerView.Adapter<FavoriteJobsAdapter.ViewHolder> implements Filterable {

    private List<Job> favoriteJobs;
    private List<Job> favoriteJobsFiltered;
    private final Context context;
    private final OnFavoriteClickListener listener;

    // Interface giữ nguyên
    public interface OnFavoriteClickListener {
        void onUnfavoriteClick(Job job, int position);
        void onItemClick(Job job, int position);
    }

    public FavoriteJobsAdapter(Context context, List<Job> favoriteJobs, OnFavoriteClickListener listener) {
        this.context = context;
        this.favoriteJobs = favoriteJobs != null ? favoriteJobs : new ArrayList<>();
        this.favoriteJobsFiltered = new ArrayList<>(this.favoriteJobs);
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
        Job job = favoriteJobsFiltered.get(position);

        holder.tvJobTitle.setText(job.getTitle());
        String companyLocation = job.getCompany().getName() + " • " + job.getLocation();
        holder.tvCompanyLocation.setText(companyLocation);

        // --- Load Logo bằng Glide từ URL ---
        Glide.with(context)
                .load(job.getCompany().getLogoUrl()) // <<< SỬ DỤNG URL
                .placeholder(R.mipmap.ic_launcher) // Ảnh mặc định khi đang tải
                .error(R.mipmap.ic_launcher)      // Ảnh mặc định khi lỗi URL hoặc không có mạng
                .circleCrop() // Tùy chọn: Bo tròn ảnh logo
                .into(holder.ivCompanyLogo);

        // Xử lý click (giữ nguyên, nhưng giờ có thể dựa vào job.getId() trong listener)
        holder.ivFavoriteHeart.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onUnfavoriteClick(favoriteJobsFiltered.get(currentPosition), currentPosition);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
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

    // Hàm cập nhật data (giữ nguyên)
    public void updateData(List<Job> newFavoriteJobs) {
        this.favoriteJobs = newFavoriteJobs != null ? new ArrayList<>(newFavoriteJobs) : new ArrayList<>();
        this.favoriteJobsFiltered = new ArrayList<>(this.favoriteJobs);
        notifyDataSetChanged();
    }

    // Hàm xóa item (nên dựa vào ID để xóa khỏi list gốc)
    public void removeItem(int position) {
        if (position >= 0 && position < favoriteJobsFiltered.size()) {
            Job removedJobFromFiltered = favoriteJobsFiltered.remove(position);
            // Xóa khỏi list gốc dựa trên equals (đã override bằng ID)
            favoriteJobs.remove(removedJobFromFiltered);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, favoriteJobsFiltered.size());
        }
    }

    // ViewHolder (Kiểm tra lại ID tv_job_title)
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyLogo;
        TextView tvJobTitle;
        TextView tvCompanyLocation;
        ImageView ivFavoriteHeart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyLogo = itemView.findViewById(R.id.iv_company_logo);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title); // <<< ĐẢM BẢO ID NÀY ĐÚNG
            tvCompanyLocation = itemView.findViewById(R.id.tv_company_location);
            ivFavoriteHeart = itemView.findViewById(R.id.iv_favorite_heart);
        }
    }

    // Filter (giữ nguyên logic, chỉ cần đảm bảo getter đúng)
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // ... (logic filter giữ nguyên) ...
                List<Job> filteredList = new ArrayList<>();
                String filterPattern = constraint == null ? "" : constraint.toString().toLowerCase(Locale.getDefault()).trim();

                if (filterPattern.isEmpty()) {
                    filteredList.addAll(favoriteJobs);
                } else {
                    for (Job job : favoriteJobs) {
                        if (job.getTitle().toLowerCase(Locale.getDefault()).contains(filterPattern) ||
                                job.getCompany().getName().toLowerCase(Locale.getDefault()).contains(filterPattern)) {
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
                notifyDataSetChanged();
            }
        };
    }
}