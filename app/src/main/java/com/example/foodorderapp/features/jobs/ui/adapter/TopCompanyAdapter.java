package com.example.foodorderapp.features.jobs.ui.adapter; // Hoặc package phù hợp

import android.content.Context;
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
import com.example.foodorderapp.core.model.Company;

import java.util.List;

public class TopCompanyAdapter extends RecyclerView.Adapter<TopCompanyAdapter.TopCompanyViewHolder> {

    private Context context;
    private List<Company> companyList;

    public TopCompanyAdapter(Context context, List<Company> companyList) {
        this.context = context;
        this.companyList = companyList;
    }

    @NonNull
    @Override
    public TopCompanyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_company, parent, false);
        return new TopCompanyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopCompanyViewHolder holder, int position) {
        Company company = companyList.get(position);
        if (company == null) return;

        holder.tvTopCompanyName.setText(company.getName());

        // Model Company hiện tại không có trường jobsCount.
        // Nếu API trả về, bạn có thể hiển thị nó. Nếu không, ẩn hoặc đặt placeholder.
        // holder.tvTopCompanyJobsCount.setText(String.valueOf(company.getJobsCount()) + " Jobs");
        holder.tvTopCompanyJobsCount.setVisibility(View.GONE); // Ẩn nếu không có dữ liệu

        String logoUrl = company.getLogoUrl();
        if (logoUrl != null && !logoUrl.isEmpty()) {
            if (!logoUrl.toLowerCase().startsWith("http")) {
                // Giả sử logoUrl là đường dẫn tương đối, cần nối với base URL
                String imageBaseUrl = Config.BE_URL.replace("/api/v1", ""); // Hoặc URL gốc của server ảnh
                logoUrl = imageBaseUrl + (logoUrl.startsWith("/") ? "" : "/") + logoUrl;
            }
            Glide.with(context)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_company_logo_placeholder) // Thay bằng placeholder phù hợp
                    .error(R.drawable.ic_company_logo_placeholder) // Thay bằng ảnh lỗi phù hợp
                    .into(holder.ivTopCompanyLogo);
        } else {
            holder.ivTopCompanyLogo.setImageResource(R.drawable.ic_company_logo_placeholder); // Ảnh mặc định
        }

        holder.itemView.setOnClickListener(v -> {
            // Xử lý sự kiện click vào item công ty nếu cần
            Toast.makeText(context, "Clicked company: " + company.getName(), Toast.LENGTH_SHORT).show();
            // Ví dụ: Mở trang chi tiết công ty (nếu có)
            // Intent intent = new Intent(context, CompanyDetailActivity.class);
            // intent.putExtra("COMPANY_ID", company.getId());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return companyList == null ? 0 : companyList.size();
    }

    public void updateData(List<Company> newCompanies) {
        this.companyList.clear();
        if (newCompanies != null) {
            this.companyList.addAll(newCompanies);
        }
        notifyDataSetChanged();
    }

    static class TopCompanyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTopCompanyLogo;
        TextView tvTopCompanyName;
        TextView tvTopCompanyJobsCount;

        TopCompanyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTopCompanyLogo = itemView.findViewById(R.id.ivTopCompanyLogo);
            tvTopCompanyName = itemView.findViewById(R.id.tvTopCompanyName);
            tvTopCompanyJobsCount = itemView.findViewById(R.id.tvTopCompanyJobsCount);
        }
    }
}
