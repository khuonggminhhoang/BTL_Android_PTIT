package com.example.foodorderapp.features.main.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.main.ui.fragment.FavoritesFragment;
import com.example.foodorderapp.features.main.ui.fragment.HomeFragment;
import com.example.foodorderapp.features.main.ui.fragment.NotificationFragment;
import com.example.foodorderapp.features.profile.ui.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // Mấy cái tên để gọi fragment
    private static final String TAG_HOME = "HOME_FRAGMENT";
    private static final String TAG_FAVORITES = "FAVORITES_FRAGMENT";
    private static final String TAG_CHAT = "CHAT_FRAGMENT"; // Đổi thành NOTIFICATIONS á
    private static final String TAG_PROFILE = "PROFILE_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Mới mở app thì cho HomeFragment vào
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), TAG_HOME);
        }

        setupBottomNavigation(); // Gọi hàm cài đặt thanh điều hướng dưới
    }

    /**
     * Cài đặt thanh điều hướng ở dưới.
     * Bấm vào thì nó chuyển fragment.
     */
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String tag = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                    tag = TAG_HOME;
                } else if (itemId == R.id.navigation_favorites) {
                    selectedFragment = new FavoritesFragment();
                    tag = TAG_FAVORITES;
                } else if (itemId == R.id.navigation_chat) {
                    selectedFragment = new NotificationFragment();
                    tag = TAG_CHAT;
                } else if (itemId == R.id.navigation_profile) {
                    selectedFragment = new ProfileFragment();
                    tag = TAG_PROFILE;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, tag); // Tải fragment đã chọn
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Hàm này để đổi fragment.
     * Nó sẽ thay fragment cũ bằng fragment mới.
     *
     * @param fragment Fragment muốn hiện.
     * @param tag      Tên của fragment đó.
     */
    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Xem fragment có sẵn chưa
        Fragment existingFragment = fragmentManager.findFragmentByTag(tag);

        if (existingFragment == null) {
            // Nếu chưa có thì thay bằng cái mới
            fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        } else {
            // Có rồi thì dùng lại cho đỡ tốn
            fragmentTransaction.replace(R.id.fragment_container, existingFragment, tag);
        }

        fragmentTransaction.commit();
    }
}
