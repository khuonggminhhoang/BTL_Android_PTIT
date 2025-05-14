package com.example.foodorderapp.features.jobs.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderapp.R;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.core.model.JobCategory;
import com.example.foodorderapp.features.jobs.ui.activity.CategoryJobsActivity;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<JobCategory> categoryList;
    private static final String TAG = "CategoryAdapter";
    private int itemWidth = 0;
    private boolean itemWidthCalculated = false;
    private static final int NUM_CATEGORIES_TO_DISPLAY_EVENLY = 4;


    public CategoryAdapter(Context context, List<JobCategory> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.post(() -> {
            if (recyclerView.getWidth() > 0) {
                int recyclerViewWidth = recyclerView.getWidth();
                int paddingStart = recyclerView.getPaddingStart();
                int paddingEnd = recyclerView.getPaddingEnd();
                int effectiveWidth = recyclerViewWidth - paddingStart - paddingEnd;

                if (effectiveWidth > 0) {
                    if (categoryList != null && !categoryList.isEmpty()) {
                        int numToDivideBy = NUM_CATEGORIES_TO_DISPLAY_EVENLY;
                        if (categoryList.size() < NUM_CATEGORIES_TO_DISPLAY_EVENLY && categoryList.size() > 0) {
                            numToDivideBy = categoryList.size();
                        }
                        itemWidth = effectiveWidth / numToDivideBy;

                    } else {
                        itemWidth = effectiveWidth / NUM_CATEGORIES_TO_DISPLAY_EVENLY;
                    }

                    if (itemWidth > 0) {
                        itemWidthCalculated = true;
                        notifyDataSetChanged();
                        Log.d(TAG, "Calculated itemWidth: " + itemWidth + " for effectiveWidth: " + effectiveWidth);
                    } else {
                        Log.w(TAG, "Calculated itemWidth is not positive: " + itemWidth);
                    }
                } else {
                    Log.w(TAG, "Effective RecyclerView width is not positive: " + effectiveWidth);
                }
            } else {
                Log.w(TAG, "RecyclerView width is 0 in post(), cannot calculate item width.");
            }
        });
    }


    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (itemWidthCalculated && itemWidth > 0) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = itemWidth;
                holder.itemView.setLayoutParams(layoutParams);
            } else {
                layoutParams = new ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                holder.itemView.setLayoutParams(layoutParams);
            }
        }


        JobCategory category = categoryList.get(position);
        if (category == null) return;

        holder.tvCategoryName.setText(category.getName());

        if (category.getIconUrl() != null && !category.getIconUrl().isEmpty()) {
            String iconUrl = category.getIconUrl();
            if (!iconUrl.toLowerCase().startsWith("http://") && !iconUrl.toLowerCase().startsWith("https://")) {
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", "");
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
                    .placeholder(R.drawable.ic_category_placeholder)
                    .error(R.drawable.ic_default_category_icon)
                    .into(holder.ivCategoryIcon);
        } else {
            Log.d(TAG, "iconUrl is null or empty for category: " + category.getName() + ". Setting default icon.");
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_default_category_icon);
        }


        holder.itemView.setOnClickListener(v -> {
            int categoryId = category.getId();
            String categoryDisplayName = category.getName();

            if (categoryId <= 0) {
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
        itemWidthCalculated = false;
        notifyDataSetChanged();
    }
}
