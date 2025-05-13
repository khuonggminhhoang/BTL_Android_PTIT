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
import com.example.foodorderapp.core.model.Skill;
import java.util.List;

public class ManageSkillsAdapter extends RecyclerView.Adapter<ManageSkillsAdapter.SkillViewHolder> {

    private List<Skill> skillList;
    private Context context;
    private OnSkillManageClickListener listener;

    // Giao diện xử lý sự kiện click
    public interface OnSkillManageClickListener {
        void onEditSkillClicked(Skill skill, int position);
        void onDeleteSkillClicked(Skill skill, int position);
    }

    public ManageSkillsAdapter(Context context, List<Skill> skillList, OnSkillManageClickListener listener) {
        this.context = context;
        this.skillList = skillList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SkillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho item kỹ năng
        View view = LayoutInflater.from(context).inflate(R.layout.item_skill_manage, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        Skill skill = skillList.get(position);
        if (skill == null) return;

        // Gán dữ liệu cho view
        String skillName = skill.getName() != null ? skill.getName() : "N/A";
        String rawLevel = skill.getLevel();
        String displayLevel = rawLevel;
        if (rawLevel != null) {
            switch (rawLevel.toUpperCase()) {
                case "SKILL_LEVEL.BEGINNER":
                    displayLevel = "Beginner";
                    break;
                case "SKILL_LEVEL.INTERMEDIATE":
                    displayLevel = "Intermediate";
                    break;
                case "SKILL_LEVEL.ADVANCE":
                    displayLevel = "Advance";
                    break;
                case "SKILL_LEVEL.EXPERT":
                    displayLevel = "Expert";
                    break;
            }
        } else {
            displayLevel = "N/A";
        }
        holder.tvSkillNameLevel.setText(String.format("%s - %s", skillName, displayLevel));

        // Gán sự kiện click
        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditSkillClicked(skill, holder.getAdapterPosition());
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteSkillClicked(skill, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return skillList == null ? 0 : skillList.size();
    }

    // Cập nhật danh sách kỹ năng
    public void updateSkills(List<Skill> newSkills) {
        this.skillList.clear();
        if (newSkills != null) this.skillList.addAll(newSkills);
        notifyDataSetChanged();
    }

    // Xóa item khỏi danh sách
    public void removeItem(int position) {
        if (position >= 0 && position < skillList.size()) {
            skillList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, skillList.size());
        }
    }

    // ViewHolder cho item kỹ năng
    static class SkillViewHolder extends RecyclerView.ViewHolder {
        TextView tvSkillNameLevel;
        ImageView ivEdit, ivDelete;

        SkillViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSkillNameLevel = itemView.findViewById(R.id.tv_skill_name_level_manage_item);
            ivEdit = itemView.findViewById(R.id.iv_edit_skill_manage_item);
            ivDelete = itemView.findViewById(R.id.iv_delete_skill_manage_item);
        }
    }
}