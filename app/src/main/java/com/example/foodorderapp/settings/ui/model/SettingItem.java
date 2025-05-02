package com.example.foodorderapp.settings.ui.model;

public class SettingItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_NAVIGATION = 1;
    public static final int TYPE_SWITCH = 2;
    public static final int TYPE_ACTION = 3;

    private int itemType;
    private int iconResId; // Resource ID for icon (0 if no icon)
    private String title;
    private String subtitle; // Can be null
    private boolean isSwitchOn; // Only used for TYPE_SWITCH
    private String actionKey; // Optional: Unique key to identify actions reliably

    // Constructor for Header
    public SettingItem(String title) {
        this.itemType = TYPE_HEADER;
        this.title = title;
        this.iconResId = 0; // No icon for header
    }

    // Constructor for Navigation/Action
    public SettingItem(int itemType, int iconResId, String title, String subtitle, String actionKey) {
        if (itemType != TYPE_NAVIGATION && itemType != TYPE_ACTION) {
            throw new IllegalArgumentException("Item type must be NAVIGATION or ACTION for this constructor");
        }
        this.itemType = itemType;
        this.iconResId = iconResId;
        this.title = title;
        this.subtitle = subtitle;
        this.actionKey = actionKey;
    }

    // Constructor for Switch
    public SettingItem(int iconResId, String title, boolean isSwitchOn, String actionKey) {
        this.itemType = TYPE_SWITCH;
        this.iconResId = iconResId;
        this.title = title;
        this.isSwitchOn = isSwitchOn;
        this.actionKey = actionKey;
    }

    // --- Getters ---
    public int getItemType() { return itemType; }
    public int getIconResId() { return iconResId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public boolean isSwitchOn() { return isSwitchOn; }
    public String getActionKey() { return actionKey; }

    // --- Setter ---
    public void setSwitchOn(boolean switchOn) { isSwitchOn = switchOn; }
}
