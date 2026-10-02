package client_files.Petrovich.Misc;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import client_files.ModuleManager;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;

public class AntiCrash extends Module {

    private final SliderSetting maxRadius = addSetting(new SliderSetting("Макс. радиус взрыва", 6.0f, 1.0f, 20.0f, 1.0f));

    public AntiCrash() {
        super("AntiCrash", "Блокирует огромные взрывы (анти-краш)", Category.MISC);
    }

    public static boolean shouldCancelExplosion(ClientboundExplodePacket packet) {
        AntiCrash antiCrash = ModuleManager.getInstance().get(AntiCrash.class);
        if (antiCrash == null || !antiCrash.isEnabled()) return false;
        return packet.radius() > antiCrash.maxRadius.getValue();
    }
}