package com.example.foodorderapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodList;

    // Constructor
    public FoodAdapter(List<FoodItem> foodList) {
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout food_list_item.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.food_list_item, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        // Lấy item tại vị trí position
        FoodItem foodItem = foodList.get(position);

        // Gán dữ liệu vào các view
        holder.foodImage.setImageResource(foodItem.getImageResId());
        holder.foodName.setText(foodItem.getName());
        holder.foodPrice.setText(foodItem.getPrice());
        holder.foodRating.setText(foodItem.getRating());
        holder.foodTime.setText(foodItem.getTime());
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    // ViewHolder để giữ các view trong food_list_item.xml
    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView foodImage;
        TextView foodName, foodPrice, foodRating, foodTime;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.food_image);
            foodName = itemView.findViewById(R.id.food_name);
            foodPrice = itemView.findViewById(R.id.food_price);
            foodRating = itemView.findViewById(R.id.food_rating);
            foodTime = itemView.findViewById(R.id.food_time);
        }
    }
}