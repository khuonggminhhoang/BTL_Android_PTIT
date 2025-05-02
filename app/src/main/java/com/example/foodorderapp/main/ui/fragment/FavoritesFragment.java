package com.example.foodorderapp.main.ui.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.foodorderapp.R;

// Fragment cho màn hình Yêu thích (cơ bản)
public class FavoritesFragment extends Fragment {

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout cho fragment này
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    // TODO: Thêm logic cho màn hình Yêu thích (ví dụ: hiển thị danh sách công việc đã thích)
}
