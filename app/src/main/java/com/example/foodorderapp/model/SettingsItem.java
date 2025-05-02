package com.example.foodorderapp.model;

public interface SettingsItem {
    int getType();
    int VIEW_TYPE_HEADER = 0;
    int VIEW_TYPE_CLICKABLE = 1;
    int VIEW_TYPE_SWITCH = 2;
    int VIEW_TYPE_LOGOUT = 3;
}
