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

    // Interface để xử lý các sự kiện click từ Activity
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_skill_manage, parent, false);
        return new SkillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SkillViewHolder holder, int position) {
        Skill skill = skillList.get(position);
        if (skill == null) {
            return;
        }

        String skillName = skill.getName() != null ? skill.getName() : "N/A";
        String rawLevel = skill.getLevel();
        String displayLevel = rawLevel; // Mặc định hiển thị giá trị gốc

        if (rawLevel != null) {
            if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.BEGINNER")) {
                displayLevel = "Beginner";
            } else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.INTERMEDIATE")) {
                displayLevel = "Intermediate";
            } else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.ADVANCE")) {
                displayLevel = "Advance";
            } else if (rawLevel.equalsIgnoreCase("SKILL_LEVEL.EXPERT")) {
                displayLevel = "Expert";
            }
        } else {
            displayLevel = "N/A";
        }

        holder.tvSkillNameLevel.setText(String.format("%s - %s", skillName, displayLevel));

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditSkillClicked(skill, holder.getAdapterPosition());
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteSkillClicked(skill, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return skillList == null ? 0 : skillList.size();
    }

    public void updateSkills(List<Skill> newSkills) {
        this.skillList.clear();
        if (newSkills != null) {
            this.skillList.addAll(newSkills);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < skillList.size()) {
            skillList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, skillList.size());
        }
    }

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
