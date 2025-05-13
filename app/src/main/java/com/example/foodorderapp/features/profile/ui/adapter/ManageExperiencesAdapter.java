package com.example.foodorderapp.features.profile.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.R;
import com.example.foodorderapp.core.model.Experience;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.util.Log;

public class ManageExperiencesAdapter extends RecyclerView.Adapter<ManageExperiencesAdapter.ExperienceViewHolder> {

    private static final String TAG = "ManageExpAdapter";
    private List<Experience> experienceList;
    private Context context;
    private OnExperienceManageClickListener listener;

    // Giao diện xử lý sự kiện click
    public interface OnExperienceManageClickListener {
        void onEditExperienceClicked(Experience experience, int position);
        void onDeleteExperienceClicked(Experience experience, int position);
        void onItemExperienceClicked(Experience experience, int position);
    }

    public ManageExperiencesAdapter(Context context, List<Experience> experienceList, OnExperienceManageClickListener listener) {
        this.context = context;
        this.experienceList = experienceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho item kinh nghiệm
        View view = LayoutInflater.from(context).inflate(R.layout.item_experience_manage, parent, false);
        return new ExperienceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        Experience experience = experienceList.get(position);
        if (experience == null) return;

        // Gán dữ liệu cho view
        holder.tvJobTitle.setText(experience.getTitle() != null ? experience.getTitle() : "N/A");
        holder.tvCompanyName.setText(experience.getCompanyName() != null ? experience.getCompanyName() : "N/A");
        String startDateFormatted = formatDateString(experience.getStartDate(), "MMM yyyy");
        String endDateFormatted = (experience.getEndDate() != null && !experience.getEndDate().isEmpty()) ?
                formatDateString(experience.getEndDate(), "MMM yyyy") : "Hiện tại";
        holder.tvDuration.setText(String.format("%s - %s", startDateFormatted, endDateFormatted));

        // Hiển thị hoặc ẩn mô tả
        if (experience.getDescription() != null && !experience.getDescription().isEmpty()) {
            holder.tvDescription.setText(experience.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Gán sự kiện click
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditExperienceClicked(experience, holder.getAdapterPosition());
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteExperienceClicked(experience, holder.getAdapterPosition());
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemExperienceClicked(experience, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return experienceList == null ? 0 : experienceList.size();
    }

    // Cập nhật danh sách kinh nghiệm
    public void updateExperiences(List<Experience> newExperiences) {
        this.experienceList.clear();
        if (newExperiences != null) this.experienceList.addAll(newExperiences);
        notifyDataSetChanged();
    }

    // Xóa item khỏi danh sách
    public void removeItem(int position) {
        if (position >= 0 && position < experienceList.size()) {
            experienceList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, experienceList.size());
        }
    }

    // ViewHolder cho item kinh nghiệm
    static class ExperienceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCompanyIcon, ivEdit, ivDelete;
        TextView tvJobTitle, tvCompanyName, tvDuration, tvDescription;

        ExperienceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCompanyIcon = itemView.findViewById(R.id.iv_company_icon_exp_manage_item);
            tvJobTitle = itemView.findViewById(R.id.tv_job_title_exp_manage_item);
            tvCompanyName = itemView.findViewById(R.id.tv_company_name_exp_manage_item);
            tvDuration = itemView.findViewById(R.id.tv_duration_exp_manage_item);
            ivEdit = itemView.findViewById(R.id.iv_edit_experience_manage_item);
            ivDelete = itemView.findViewById(R.id.iv_delete_experience_manage_item);
            tvDescription = itemView.findViewById(R.id.tv_experience_description_manage_item);
        }
    }

    // Định dạng chuỗi ngày tháng
    private String formatDateString(String dateString, String outputFormatPattern) {
        if (dateString == null || dateString.isEmpty()) return "N/A";
        try {
            SimpleDateFormat sdfFromApi;
            if (dateString.contains("T") && dateString.contains("Z")) {
                sdfFromApi = new SimpleDateFormat(dateString.contains(".") ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdfFromApi.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) {
                sdfFromApi = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            } else {
                Log.w(TAG, "Định dạng ngày không nhận diện: " + dateString);
                return dateString;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdfFromApi.parse(dateString));
            SimpleDateFormat sdfOutput = new SimpleDateFormat(outputFormatPattern, Locale.getDefault());
            return sdfOutput.format(cal.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi phân tích ngày: '" + dateString + "'", e);
            return dateString;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Lỗi định dạng ngày: '" + dateString + "'", iae);
            return dateString;
        }
    }
}