package com.example.foodorderapp.features.profile.ui.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Import TextView

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.profile.ui.adapter.MenuAdapter;
import com.example.foodorderapp.features.profile.ui.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị màn hình Hồ sơ người dùng.
 * Thay thế cho ProfileActivity.
 */
public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<Object> menuItems;

    // Các TextView để hiển thị thông tin người dùng
    private TextView tvName;
    private TextView tvRole;
    private TextView tvApplied;
    private TextView tvReviewed;
    private TextView tvContacted;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout cho fragment này
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các View từ layout của Fragment
        recyclerView = view.findViewById(R.id.rv_menu);
        tvName = view.findViewById(R.id.tv_name);
        tvRole = view.findViewById(R.id.tv_role);
        tvApplied = view.findViewById(R.id.tv_applied);
        tvReviewed = view.findViewById(R.id.tv_reviewed);
        tvContacted = view.findViewById(R.id.tv_contacted);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập dữ liệu người dùng (ví dụ)
        setupUserData();
    }

    /**
     * Thiết lập RecyclerView cho danh sách menu.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Sử dụng getContext() trong Fragment

        // Tạo danh sách menu (logic tương tự ProfileActivity cũ)
        menuItems = new ArrayList<>();
        // Sử dụng getString() từ Fragment để lấy chuỗi tài nguyên
        menuItems.add(getString(R.string.account_section_title)); // Ví dụ thêm tiêu đề section
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.personal_data), "ACCOUNT"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_edit, getString(R.string.resume_my_info), "ACCOUNT"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_agenda, getString(R.string.my_application), "ACCOUNT"));
        menuItems.add(getString(R.string.other_section_title)); // Ví dụ thêm tiêu đề section
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_preferences, getString(R.string.settings_title), "OTHER"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_help, getString(R.string.faq), "OTHER"));
        menuItems.add(new MenuItem(android.R.drawable.ic_menu_info_details, getString(R.string.privacy_policy), "OTHER"));

        // Khởi tạo Adapter với Context của Fragment
        adapter = new MenuAdapter(getContext(), menuItems); // MenuAdapter có thể cần Context, nếu vậy hãy truyền getContext()
        recyclerView.setAdapter(adapter);
    }

    /**
     * Thiết lập dữ liệu người dùng mẫu lên các TextView.
     */
    private void setupUserData() {
        // Dữ liệu mẫu, bạn nên lấy dữ liệu thực tế từ nguồn dữ liệu của mình
        tvName.setText("Hafidzzaki");
        tvRole.setText("Senior UI/UX Designer");
        tvApplied.setText("31");
        tvReviewed.setText("17");
        tvContacted.setText("5");

        // TODO: Thêm xử lý sự kiện cho các ImageView (avatar, camera icon) nếu cần
    }
}
