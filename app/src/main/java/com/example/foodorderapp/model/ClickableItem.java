package com.example.foodorderapp.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class ClickableItem implements SettingsItem {
    @DrawableRes private int iconRes;
    private String title;
    @Nullable private String subtitle;
    private Runnable action;

    public ClickableItem(@DrawableRes int iconRes, String title, @Nullable String subtitle, Runnable action) {
        this.iconRes = iconRes; this.title = title; this.subtitle = subtitle; this.action = action;
    }
    public int getIconRes() { return iconRes; }
    public String getTitle() { return title; }
    @Nullable public String getSubtitle() { return subtitle; }
    public Runnable getAction() { return action; }
    @Override public int getType() { return VIEW_TYPE_CLICKABLE; }
}