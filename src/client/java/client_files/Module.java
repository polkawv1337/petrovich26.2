package client_files;

import java.util.ArrayList;
import java.util.List;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.Config;
import client_files.ClientikUtils.Notifier;
import client_files.ClientikUtils.Setting;
import client_files.ClientikUtils.render.RRender;
import net.minecraft.client.Minecraft;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private int key = -1;
    private int scrollBind = 0;
    private float hudX = 4;
    private float hudY = 4;
    private boolean enabled;
    private final List<Setting> settings = new ArrayList<>();

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getScrollBind() {
        return scrollBind;
    }

    public void setScrollBind(int scrollBind) {
        this.scrollBind = scrollBind;
    }

    public float getHudX() {
        return hudX;
    }

    public float getHudY() {
        return hudY;
    }

    public void setHud(float x, float y) {
        this.hudX = x;
        this.hudY = y;
    }

    public boolean isDraggable() {
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
        if (Minecraft.getInstance().player != null) {
            Notifier.push(name + (enabled ? "  ВКЛ" : "  ВЫКЛ"),
                    enabled ? RRender.accent() : 0xFF808A9E);
        }
        Config.save();
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    protected <T extends Setting> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onUpdate() {
    }

    public void onPacketReceive(Object packet) {
    }

    public void onRender(net.minecraft.client.gui.GuiGraphicsExtractor graphics) {
    }

    public void onKeyPress(int key, int action) {
    }
}
