package com.example.foodorderapp.features.jobs.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderapp.R;
import com.example.foodorderapp.features.jobs.ui.model.SearchHistory;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder> {

    private Context context;
    private List<SearchHistory> historyList;
    private OnSearchHistoryClickListener listener;

    // Interface để xử lý click
    public interface OnSearchHistoryClickListener {
        void onHistoryItemClick(SearchHistory item);
    }

    public SearchHistoryAdapter(Context context, List<SearchHistory> historyList, OnSearchHistoryClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_history, parent, false);
        return new SearchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        SearchHistory item = historyList.get(position);
        if (item == null) return;

        holder.tvTerm.setText(item.getTerm());
        holder.tvCount.setText(String.valueOf(item.getCount()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHistoryItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTerm;
        TextView tvCount;
        ImageView ivArrow;

        public SearchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTerm = itemView.findViewById(R.id.search_history_term);
            tvCount = itemView.findViewById(R.id.search_history_count);
            ivArrow = itemView.findViewById(R.id.search_history_arrow);
        }
    }
}
