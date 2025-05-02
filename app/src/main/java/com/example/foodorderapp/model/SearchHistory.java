package com.example.foodorderapp.model;

public class SearchHistory {
    private String term;
    private int count;

    public SearchHistory(String term, int count) {
        this.term = term;
        this.count = count;
    }

    public String getTerm() {
        return term;
    }

    public int getCount() {
        return count;
    }
}