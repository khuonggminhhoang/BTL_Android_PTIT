package com.example.foodorderapp.model;

import androidx.annotation.DrawableRes;

public class LogoutItem implements SettingsItem {
    @DrawableRes private int iconRes;
    private String title;
    private Runnable action;

    public LogoutItem(@DrawableRes int iconRes, String title, Runnable action) {
        this.iconRes = iconRes; this.title = title; this.action = action;
    }
    public int getIconRes() { return iconRes; }
    public String getTitle() { return title; }
    public Runnable getAction() { return action; }
    @Override public int getType() { return VIEW_TYPE_LOGOUT; }
}