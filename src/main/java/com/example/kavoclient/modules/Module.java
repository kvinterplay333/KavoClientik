package com.example.kavoclient.modules;

import com.example.kavoclient.settings.Setting;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category {
        COMBAT("Бой"),
        VISUAL("Визуал"),
        MOVEMENT("Движение"),
        PLAYER("Игрок"),
        PRANK("Разное");

        private final String label;
        Category(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private final String name;
    private final Category category;
    private boolean enabled;
    private int keyBind;
    private final List<Setting> settings = new ArrayList<>();
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, Category category, int defaultKey) {
        this.name = name;
        this.category = category;
        this.keyBind = defaultKey;
    }

    public void addSetting(Setting setting) {
        this.settings.add(setting);
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int keyBind) { this.keyBind = keyBind; }
    public List<Setting> getSettings() { return settings; }
}
