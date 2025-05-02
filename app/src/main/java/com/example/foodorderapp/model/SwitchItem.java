package com.example.foodorderapp.model;

import androidx.annotation.DrawableRes;

public class SwitchItem implements SettingsItem {
    @DrawableRes private int iconRes;
    private String title;
    private boolean isChecked;
    private OnCheckedChangeListener action;

    public interface OnCheckedChangeListener { void onCheckedChanged(boolean isChecked); }

    public SwitchItem(@DrawableRes int iconRes, String title, boolean isChecked, OnCheckedChangeListener action) {
        this.iconRes = iconRes; this.title = title; this.isChecked = isChecked; this.action = action;
    }
    public int getIconRes() { return iconRes; }
    public String getTitle() { return title; }
    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
    public OnCheckedChangeListener getAction() { return action; }
    @Override public int getType() { return VIEW_TYPE_SWITCH; }
}
