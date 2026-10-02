package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ModeListSetting;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.ToggleSetting;
import client_files.Module;
import client_files.ModuleManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Interface extends Module {

    private static final String MODERN = "Modern";
    private static final String LEGACY = "Legacy";

    private final Map<Module, Boolean> savedStates = new HashMap<>();
    private final List<ToggleSetting> toggles = new ArrayList<>();
    private final List<Module> handled = new ArrayList<>();
    private final ModeSetting style = addSetting(new ModeSetting("Style", MODERN, LEGACY));
    private final ModeListSetting elements = addSetting(new ModeListSetting("Elements"));
    private boolean legacyActive = false;

    public Interface() {
        super("Interface", "Interface settings: HUD functions inside", Category.RENDER);
        elements.set("Watermark", true);
        elements.set("Coordinates", true);
        elements.set("Target HUD", true);
        elements.set("Potions", true);
        elements.set("Speed", true);
        elements.set("Armor", true);
        elements.set("Notifications", true);
        elements.set("Staff List", true);
        // Totem Counter находится в Legacy пакете и доступен только в legacy режиме
        addTarget("Watermark", Watermark.class);
        addTarget("TPS", Tps.class);
        addTarget("Speed", Bps.class);
        addTarget("Coordinates", Coords.class);
        addTarget("Potions", Potions.class);
        addTarget("Target HUD", TargetHud.class);
        addTarget("Staff List", StaffList.class);
        addTarget("Hotkeys", HotKeys.class);
        addTarget("Armor", ArmorHud.class);
        addTarget("Music", Music.class);
        // TotemHud находится в Legacy пакете и используется только в legacy режиме
        setEnabled(true);
    }

    @Override
    public boolean isDraggable() {
        return true; // Interface должен быть перемещаемым
    }

    private void addTarget(String label, Class<? extends Module> clazz) {
        Module module = ModuleManager.getInstance().get(clazz);
        if (module != null) {
            ToggleSetting setting = new ToggleSetting(label, module);
            addSetting(setting);
            toggles.add(setting);
            handled.add(module);
        }
    }

    public void sync() {
        for (ToggleSetting toggle : toggles) {
            toggle.sync();
        }
    }

    public static boolean handles(Module module) {
        return module instanceof Watermark
                || module instanceof Tps
                || module instanceof Bps
                || module instanceof Coords
                || module instanceof Potions
                || module instanceof TargetHud
                || module instanceof StaffList
                || module instanceof HotKeys
                || module instanceof ArmorHud
                || module instanceof Music;
    }

    public static boolean legacyActive() {
        Module module = ModuleManager.getInstance().get(Interface.class);
        return module instanceof Interface iface && iface.isEnabled() && iface.legacyActive;
    }

    @Override
    public void onTick() {
        boolean wantLegacy = isEnabled() && style.getValue().equals(LEGACY);
        if (wantLegacy && !legacyActive) {
            legacyActive = true;
            savedStates.clear();
            for (Module module : handled) {
                savedStates.put(module, module.isEnabled());
                if (module.isEnabled()) {
                    module.setEnabled(false);
                }
            }
        } else if (!wantLegacy && legacyActive) {
            legacyActive = false;
            for (Map.Entry<Module, Boolean> entry : savedStates.entrySet()) {
                if (Boolean.TRUE.equals(entry.getValue())) {
                    entry.getKey().setEnabled(true);
                }
            }
            savedStates.clear();
        } else if (wantLegacy) {
            for (Module module : handled) {
                if (module.isEnabled()) {
                    savedStates.put(module, false);
                    module.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        if (!legacyActive) {
            return;
        }
        if (elements.isEnabled("Watermark")) {
            client_files.Petrovich.Render.Legacy.WatermarkElement.render(graphics, (int) 4f, (int) 4f);
        }
        if (elements.isEnabled("Coordinates")) {
            client_files.Petrovich.Render.Legacy.CoordsElement.render(graphics, (int) 4f, (int) 24f);
        }
        if (elements.isEnabled("Speed")) {
            client_files.Petrovich.Render.Legacy.SpeedPill.render(graphics);
        }
        if (elements.isEnabled("Hotkeys")) {
            client_files.Petrovich.Render.Legacy.BindsElement.render(graphics, (int) 100f, (int) 50f);
        }
        if (elements.isEnabled("Potions")) {
            client_files.Petrovich.Render.Legacy.PotionsElement.render(graphics);
        }
        if (elements.isEnabled("Staff List")) {
            client_files.Petrovich.Render.Legacy.StaffListElement.render(graphics);
        }
        if (elements.isEnabled("Target HUD")) {
            client_files.Petrovich.Render.Legacy.TargetHud.render(graphics);
        }
        if (elements.isEnabled("Armor")) {
            client_files.Petrovich.Render.Legacy.ArmorHudElement.render(graphics);
        }
        if (elements.isEnabled("Notifications")) {
            client_files.Petrovich.Render.Legacy.NotificationsElement.render(graphics);
        }
        // Totem Counter находится в Legacy пакете и доступен только в legacy режиме
    }
}