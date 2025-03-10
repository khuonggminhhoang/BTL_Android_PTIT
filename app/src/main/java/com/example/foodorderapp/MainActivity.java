package com.example.foodorderapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodItem> foodList;
    private RecyclerView specialsRecyclerView;
    private SpecialFoodAdapter specialFoodAdapter;
    private List<SpecialFoodItem> specialList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Tìm RecyclerView
        recyclerView = findViewById(R.id.recycler_view);

        // Khởi tạo danh sách món ăn
        foodList = new ArrayList<>();
        foodList.add(new FoodItem("Jollof rice and fishes", "$15", "4.5", "35-45 mins", R.drawable.jollof_rice));
        foodList.add(new FoodItem("Ogbono and meats", "$12", "4.2", "30-40 mins", R.drawable.ogbono));
        foodList.add(new FoodItem("Ice cream sundae", "$18", "5.0", "15-30 mins", R.drawable.ice_cream));
        foodList.add(new FoodItem("Spaghetti and veggies", "$15", "4.2", "30-40 mins", R.drawable.spaghetti));
        foodList.add(new FoodItem("Meats and veggies", "$20", "4.5", "30-40 mins", R.drawable.meats_veggies));

        // Khởi tạo adapter và gán vào RecyclerView
        foodAdapter = new FoodAdapter(foodList);
        recyclerView.setAdapter(foodAdapter);

        // Tìm RecyclerView
        specialsRecyclerView = findViewById(R.id.specials_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        specialsRecyclerView.setLayoutManager(layoutManager);
        // Khởi tạo danh sách món ăn đặc biệt
        specialList = new ArrayList<>();
        specialList.add(new SpecialFoodItem("20-30 mins", "Meat and assorted meat", R.drawable.food_meat));
        specialList.add(new SpecialFoodItem("20-30 mins", "Vegetarian food", R.drawable.vegetarian_food));
        specialList.add(new SpecialFoodItem("25-35 mins", "Beef meal with vegetables", R.drawable.beef_meal));

        // Khởi tạo adapter và gán vào RecyclerView
        specialFoodAdapter = new SpecialFoodAdapter(specialList);
        specialsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        specialsRecyclerView.setAdapter(specialFoodAdapter);
    }
}