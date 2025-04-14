package com.example.foodorderapp.adapter; // Package của bạn

import android.content.Context;
import android.content.Intent; // Import Intent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Import Toast

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.example.foodorderapp.R;
import com.example.foodorderapp.activity.CategoryJobsActivity; // Import Activity mới
import com.example.foodorderapp.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
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
        Category category = categoryList.get(position);
        if (category == null) return;

        holder.tvCategoryName.setText(category.getName());
        holder.ivCategoryIcon.setImageResource(category.getIconResId());

        // --- Sửa đổi sự kiện click ---
        holder.itemView.setOnClickListener(v -> {
            // Lấy tên danh mục được click
            String categoryName = category.getName();

            // Tạo Intent để mở CategoryJobsActivity
            Intent intent = new Intent(context, CategoryJobsActivity.class);

            // Đặt tên danh mục vào Intent extra
            intent.putExtra(CategoryJobsActivity.EXTRA_CATEGORY_NAME, categoryName);

            // Khởi chạy Activity mới
            context.startActivity(intent);

            // Toast.makeText(context, "Clicked category: " + categoryName, Toast.LENGTH_SHORT).show(); // Debug
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
}
