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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoriteJobsAdapter extends RecyclerView.Adapter<FavoriteJobsAdapter.ViewHolder> implements Filterable {

    private List<Job> favoriteJobs;
    private List<Job> favoriteJobsFiltered;
    private final Context context;
    private final OnFavoriteClickListener listener;

    // Interface xử lý sự kiện click
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

        // Tải logo công ty bằng Glide
        Glide.with(context)
                .load(job.getCompany().getLogoUrl())
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .circleCrop()
                .into(holder.ivCompanyLogo);

        // Xử lý click bỏ yêu thích
        holder.ivFavoriteHeart.setOnClickListener(v -> {
            if (listener != null) {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    listener.onUnfavoriteClick(favoriteJobsFiltered.get(currentPosition), currentPosition);
                }
            }
        });

        // Xử lý click item
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

    // Cập nhật danh sách công việc
    public void updateData(List<Job> newFavoriteJobs) {
        this.favoriteJobs = newFavoriteJobs != null ? new ArrayList<>(newFavoriteJobs) : new ArrayList<>();
        this.favoriteJobsFiltered = new ArrayList<>(this.favoriteJobs);
        notifyDataSetChanged();
    }

    // Xóa công việc khỏi danh sách
    public void removeItem(int position) {
        if (position >= 0 && position < favoriteJobsFiltered.size()) {
            Job removedJobFromFiltered = favoriteJobsFiltered.remove(position);
            favoriteJobs.remove(removedJobFromFiltered);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, favoriteJobsFiltered.size());
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