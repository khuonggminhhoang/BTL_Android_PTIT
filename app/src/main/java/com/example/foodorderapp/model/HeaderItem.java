package com.example.foodorderapp.model;

public class HeaderItem implements SettingsItem {
    private String title;
    public HeaderItem(String title) { this.title = title; }
    public String getTitle() { return title; }
    @Override public int getType() { return VIEW_TYPE_HEADER; }
}
