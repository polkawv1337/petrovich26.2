package client_files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ClickGuiScreen;
import client_files.ClientikUtils.Config;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.HudEditorScreen;
import client_files.ClientikUtils.Theme;
import client_files.Petrovich.Combat.AimBow;
import client_files.Petrovich.Combat.Aura;
import client_files.Petrovich.Combat.AutoArmor;
import client_files.Petrovich.Combat.AutoExplosion;
import client_files.Petrovich.Combat.AutoMace;
import client_files.Petrovich.Combat.AutoPotion;
import client_files.Petrovich.Combat.AutoTotem;
import client_files.Petrovich.Combat.CrystalOptimizer;
import client_files.Petrovich.Combat.CrystalAura;
import client_files.Petrovich.Combat.AutoWeb;
import client_files.Petrovich.Combat.ElytraLock;
import client_files.Petrovich.Combat.ElytraTarget;
import client_files.Petrovich.Combat.PacketCriticals;
import client_files.Petrovich.Combat.TriggerBot;
import client_files.Petrovich.Combat.Velocity;
import client_files.Petrovich.Misc.AntiCrash;
import client_files.Petrovich.Misc.ServerHelper;
import client_files.Petrovich.Misc.UseTracker;
import client_files.Petrovich.Movement.AirStuck;
import client_files.Petrovich.Movement.AutoSprint;
import client_files.Petrovich.Movement.DragonFly;
import client_files.Petrovich.Movement.ElytraBooster;
import client_files.Petrovich.Movement.ElytraMotion;
import client_files.Petrovich.Movement.InventoryMove;
import client_files.Petrovich.Movement.NoJumpDelay;
import client_files.Petrovich.Movement.NoSlow;
import client_files.Petrovich.Movement.NoWeb;
import client_files.Petrovich.Movement.Speed;
import client_files.Petrovich.Movement.Timer;
import client_files.Petrovich.Movement.TridenBoost;
import client_files.Petrovich.Player.Assistant;
import client_files.Petrovich.Player.AutoKitFarm;
import client_files.Petrovich.Player.AutoTool;
import client_files.Petrovich.Player.ClickSetting;
import client_files.Petrovich.Player.ElytraHelper;
import client_files.Petrovich.Player.FreeCamera;
import client_files.Petrovich.Player.FriendHelper;
import client_files.Petrovich.Player.NoDelay;
import client_files.Petrovich.Render.*;
import net.minecraft.client.Minecraft;

public class ModuleManager {
    private static ModuleManager instance;
    private static boolean ready;

    private final List<Module> modules = new ArrayList<>();

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    public static boolean isReady() {
        return ready;
    }

    private ModuleManager() {
        instance = this;
        Theme.load();
        registerModules();
        InventoryMove inventoryMove = get(InventoryMove.class);
        if (inventoryMove != null) {
            inventoryMove.setEnabled(true);
        }
        Config.load(modules);
        HudEditor.loadOffsets(modules);
        Interface iface = get(Interface.class);
        if (iface != null) {
            iface.sync();
        }
        ready = true;
    }

    private void registerModules() {
        modules.add(new Aura());
        modules.add(new Velocity());
        modules.add(new TriggerBot());
        modules.add(new PacketCriticals());
        modules.add(new AimBow());
        modules.add(new AutoTotem());
        modules.add(new AutoArmor());
        modules.add(new AutoPotion());
        modules.add(new ElytraLock());
        modules.add(new ElytraTarget());
        modules.add(new CrystalOptimizer());
        modules.add(new CrystalAura());
        modules.add(new AutoWeb());
        modules.add(new AutoExplosion());
        modules.add(new AutoMace());

        modules.add(new Speed());
        modules.add(new NoSlow());
        modules.add(new Timer());
        modules.add(new AutoSprint());
        modules.add(new TridenBoost());
        modules.add(new NoWeb());
        modules.add(new NoJumpDelay());
        modules.add(new DragonFly());
        modules.add(new InventoryMove());
        modules.add(new ElytraBooster());
        modules.add(new ElytraMotion());
        modules.add(new AirStuck());

        modules.add(new NoDelay());
        modules.add(new FriendHelper());
        modules.add(new AutoKitFarm());
        modules.add(new ClickSetting());
        modules.add(new ElytraHelper());
        modules.add(new Assistant());
        modules.add(new AutoTool());
        modules.add(new FreeCamera());

        modules.add(new UseTracker());
        modules.add(new ServerHelper());
        modules.add(new AntiCrash());

        modules.add(new Watermark());
        modules.add(new Tps());
        modules.add(new TargetHud());
        modules.add(new Potions());
        modules.add(new HotKeys());
        modules.add(new Coords());
        modules.add(new Bps());
        modules.add(new ArmorHud());
        modules.add(new Music());
        modules.add(new StaffList());
        modules.add(new ClickGui());
        modules.add(new Interface());
        modules.add(new FullBright());
        modules.add(new ChinaHat());
        modules.add(new Arrows());
        modules.add(new Donut());
    }

    public List<Module> getModules() {
        return modules;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> clazz) {
        Optional<Module> found = modules.stream().filter(m -> m.getClass() == clazz).findFirst();
        return found.map(m -> (T) m).orElse(null);
    }

    public List<Module> getByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void onUpdate() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onUpdate();
            }
        }
    }

    public void onPacketReceive(Object packet) {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onPacketReceive(packet);
            }
        }
    }

    public void onKeyPress(int key, int action) {
        if (action != 1) return;
        Minecraft mc = Minecraft.getInstance();

        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onKeyPress(key, action);
            }
        }

        if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_F8) {
            if (mc.gui.screen() == null) {
                mc.setScreenAndShow(new HudEditorScreen());
                return;
            }
        }

        for (Module module : modules) {
            if (module instanceof ClickGui) {
                if (module.getKey() == key || key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) {
                    mc.setScreenAndShow(new ClickGuiScreen());
                    return;
                }
            } else if (module.getKey() == key) {
                module.toggle();
            }
        }
    }

    public boolean onScroll(int direction) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() != null) {
            return false;
        }
        boolean handled = false;
        for (Module module : modules) {
            if (module.getScrollBind() == direction) {
                module.toggle();
                handled = true;
            }
        }
        return handled;
    }
}