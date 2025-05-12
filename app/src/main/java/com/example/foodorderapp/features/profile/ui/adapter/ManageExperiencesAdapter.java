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

    // Interface để xử lý các sự kiện click từ Activity
    public interface OnExperienceManageClickListener {
        void onEditExperienceClicked(Experience experience, int position);
        void onDeleteExperienceClicked(Experience experience, int position);
        void onItemExperienceClicked(Experience experience, int position); // Để xem mô tả chi tiết
    }

    public ManageExperiencesAdapter(Context context, List<Experience> experienceList, OnExperienceManageClickListener listener) {
        this.context = context;
        this.experienceList = experienceList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_experience_manage, parent, false);
        return new ExperienceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        Experience experience = experienceList.get(position);
        if (experience == null) {
            return;
        }

        holder.tvJobTitle.setText(experience.getTitle() != null ? experience.getTitle() : "N/A");
        holder.tvCompanyName.setText(experience.getCompanyName() != null ? experience.getCompanyName() : "N/A");

        String startDateFormatted = formatDateString(experience.getStartDate(), "MMM yyyy");
        String endDateFormatted = (experience.getEndDate() != null && !experience.getEndDate().isEmpty()) ?
                formatDateString(experience.getEndDate(), "MMM yyyy") : "Hiện tại";
        holder.tvDuration.setText(String.format("%s - %s", startDateFormatted, endDateFormatted));

        // Hiển thị hoặc ẩn mô tả
        if (experience.getDescription() != null && !experience.getDescription().isEmpty()) {
            holder.tvDescription.setText(experience.getDescription());
            // Bạn có thể quyết định mặc định là GONE hay VISIBLE
            // holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Gán sự kiện click cho nút sửa
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditExperienceClicked(experience, holder.getAdapterPosition());
            }
        });

        // Gán sự kiện click cho nút xóa
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteExperienceClicked(experience, holder.getAdapterPosition());
            }
        });

        // Gán sự kiện click cho toàn bộ item (ví dụ để toggle mô tả)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemExperienceClicked(experience, holder.getAdapterPosition());
            }
            // Ví dụ: Toggle visibility của mô tả
            // if (holder.tvDescription.getVisibility() == View.VISIBLE) {
            //     holder.tvDescription.setVisibility(View.GONE);
            // } else {
            //     holder.tvDescription.setVisibility(View.VISIBLE);
            // }
        });
    }

    @Override
    public int getItemCount() {
        return experienceList == null ? 0 : experienceList.size();
    }

    public void updateExperiences(List<Experience> newExperiences) {
        this.experienceList.clear();
        if (newExperiences != null) {
            this.experienceList.addAll(newExperiences);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < experienceList.size()) {
            experienceList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, experienceList.size());
        }
    }


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

    // Hàm định dạng ngày (tương tự như trong ResumeMyInfoActivity)
    private String formatDateString(String dateString, String outputFormatPattern) {
        if (dateString == null || dateString.isEmpty()) return "N/A";
        try {
            SimpleDateFormat sdfFromApi;
            if (dateString.contains("T") && dateString.contains("Z")) { // ISO 8601
                sdfFromApi = new SimpleDateFormat(dateString.contains(".") ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" : "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                sdfFromApi.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else if (dateString.matches("\\d{4}-\\d{2}-\\d{2}")) { // yyyy-MM-dd
                sdfFromApi = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            } else {
                Log.w(TAG, "Unrecognized date format for: " + dateString);
                return dateString; // Trả về chuỗi gốc nếu không nhận dạng được
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdfFromApi.parse(dateString));
            SimpleDateFormat sdfOutput = new SimpleDateFormat(outputFormatPattern, Locale.getDefault());
            return sdfOutput.format(cal.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date string: '" + dateString + "'", e);
            return dateString;
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Error formatting date string (illegal argument): '" + dateString + "'", iae);
            return dateString;
        }
    }
}
