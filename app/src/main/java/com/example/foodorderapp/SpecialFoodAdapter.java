package com.example.foodorderapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SpecialFoodAdapter extends RecyclerView.Adapter<SpecialFoodAdapter.SpecialViewHolder> {

    private List<SpecialFoodItem> specialList;

    public SpecialFoodAdapter(List<SpecialFoodItem> specialList) {
        this.specialList = specialList;
    }

    @NonNull
    @Override
    public SpecialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.special_food_item, parent, false); // Sử dụng layout riêng
        return new SpecialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecialViewHolder holder, int position) {
        SpecialFoodItem item = specialList.get(position);
        holder.specialImage.setImageResource(item.getImageResId());
        holder.specialTime.setText(item.getTime());
        holder.specialName.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return specialList.size();
    }

    static class SpecialViewHolder extends RecyclerView.ViewHolder {
        ImageView specialImage;
        TextView specialTime, specialName;

        public SpecialViewHolder(@NonNull View itemView) {
            super(itemView);
            specialImage = itemView.findViewById(R.id.special_image);
            specialTime = itemView.findViewById(R.id.special_time);
            specialName = itemView.findViewById(R.id.special_name);
        }
    }
}
