package com.example.foodorderapp.features.jobs.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Import Glide
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config; // Import Config
import com.example.foodorderapp.core.model.JobCategory;
import com.example.foodorderapp.features.jobs.ui.activity.CategoryJobsActivity;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<JobCategory> categoryList;
    private static final String TAG = "CategoryAdapter"; // Tag for logging

    public CategoryAdapter(Context context, List<JobCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        JobCategory category = categoryList.get(position);
        if (category == null) return;

        holder.tvCategoryName.setText(category.getName());

        // Load icon bằng Glide nếu có iconUrl
        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            String iconUrl = category.getIconUrl();
            // Kiểm tra xem URL là tương đối hay tuyệt đối
            if (!iconUrl.toLowerCase().startsWith("http://") && !iconUrl.toLowerCase().startsWith("https://")) {
                // Nối với base URL của server nếu là đường dẫn tương đối
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", ""); // Bỏ phần /api/v1
                if (imageBaseUrl.endsWith("/")) {
                    imageBaseUrl = imageBaseUrl.substring(0, imageBaseUrl.length() - 1);
                }
                if (iconUrl.startsWith("/")) {
                    iconUrl = iconUrl.substring(1);
                }
                iconUrl = imageBaseUrl + "/" + iconUrl;
                Log.d(TAG, "Constructed icon URL for " + category.getName() + ": " + iconUrl);
            } else {
                Log.d(TAG, "Using absolute icon URL for " + category.getName() + ": " + iconUrl);
            }

            Glide.with(context)
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_category_placeholder) // Tạo drawable này hoặc dùng drawable có sẵn
                    .error(R.drawable.ic_default_category_icon)       // Tạo drawable này hoặc dùng drawable có sẵn
                    .into(holder.ivCategoryIcon);
        } else {
            // Nếu không có iconUrl, set một icon mặc định
            Log.d(TAG, "iconUrl is null or empty for category: " + category.getName() + ". Setting default icon.");
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_default_category_icon); // Tạo drawable này
        }


        holder.itemView.setOnClickListener(v -> {
            int categoryId = category.getId();
            String categoryDisplayName = category.getName();

            if (categoryId <= 0) { // Kiểm tra ID hợp lệ trước khi gửi
                Toast.makeText(context, "Invalid category ID.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Attempted to open category with invalid ID: " + categoryId);
                return;
            }

            Intent intent = new Intent(context, CategoryJobsActivity.class);
            intent.putExtra(CategoryJobsActivity.EXTRA_CATEGORY_ID, categoryId);
            intent.putExtra(CategoryJobsActivity.EXTRA_CATEGORY_DISPLAY_NAME, categoryDisplayName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categoryList == null ? 0 : categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCategoryIcon;
        TextView tvCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }

    public void updateData(List<JobCategory> newCategoryList) {
        this.categoryList = newCategoryList;
        notifyDataSetChanged();
    }
}
