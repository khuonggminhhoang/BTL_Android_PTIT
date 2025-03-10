package com.example.foodorderapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView foodList;
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Khởi tạo RecyclerView
        foodList = findViewById(R.id.food_list);
        foodList.setLayoutManager(new LinearLayoutManager(this));

        // Tạo dữ liệu mẫu
        List<FoodItem> foodItems = new ArrayList<>();
        foodItems.add(new FoodItem("Jollof rice and fishes", "$15", "4.5", "35-45 mins", R.drawable.food1));
        foodItems.add(new FoodItem("Ogbono and meats", "$12", "4.2", "30-40 mins", R.drawable.food2));
        foodItems.add(new FoodItem("Ice cream sundae", "$18", "5.0", "15-30 mins", R.drawable.food3));
        foodItems.add(new FoodItem("Spaghetti and veggies", "$15", "4.2", "30-40 mins", R.drawable.food4));
        foodItems.add(new FoodItem("Meats and veggies", "$20", "4.5", "30-40 mins", R.drawable.food5));

        // Thiết lập Adapter
        foodAdapter = new FoodAdapter(foodItems);
        foodList.setAdapter(foodAdapter);
    }
}