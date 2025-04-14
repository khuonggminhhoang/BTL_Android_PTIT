package com.example.foodorderapp.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.foodorderapp.adapter.MenuAdapter;
import com.example.foodorderapp.R;
import com.example.foodorderapp.model.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;
public class ProfileActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<Object> menuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.rv_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Tạo danh sách menu
        menuItems = new ArrayList<>();
        menuItems.add("ACCOUNT");
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.personal_data), "ACCOUNT"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_edit, getString(R.string.resume_my_info), "ACCOUNT"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_agenda, getString(R.string.my_application), "ACCOUNT"));
        menuItems.add("OTHER");
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_preferences, getString(R.string.settings), "OTHER"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_help, getString(R.string.faq), "OTHER"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.privacy_policy), "OTHER"));

        adapter = new MenuAdapter(this, menuItems);
        recyclerView.setAdapter(adapter);

        // Thiết lập BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);

//        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
//            switch (item.getItemId()) {
//                case R.id.nav_home:
//                    // Chuyển đến Home
//                    return true;
//                case R.id.nav_favorites:
//                    // Chuyển đến Favorites
//                    return true;
//                case R.id.nav_messages:
//                    // Chuyển đến Messages
//                    return true;
//                case R.id.nav_profile:
//                    return true;
//            }
//            return false;
//        });

        // Thiết lập thông tin người dùng
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvRole = findViewById(R.id.tv_role);
        TextView tvApplied = findViewById(R.id.tv_applied);
        TextView tvReviewed = findViewById(R.id.tv_reviewed);
        TextView tvContacted = findViewById(R.id.tv_contacted);

        tvName.setText("Hafidzzaki");
        tvRole.setText("Senior UI/UX Designer");
        tvApplied.setText("31");
        tvReviewed.setText("17");
        tvContacted.setText("5");
    }
}