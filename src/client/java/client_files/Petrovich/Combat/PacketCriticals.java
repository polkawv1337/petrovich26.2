package client_files.Petrovich.Combat;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ModeSetting;
import client_files.Module;
import client_files.ModuleManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

public class PacketCriticals extends Module {

    private final ModeSetting mode = addSetting(new ModeSetting("Режим", "ReallyWorld", "ReallyWorld", "Grim 1.17+"));

    public PacketCriticals() {
        super("PacketCriticals", "Всегда даёт критический удар", Category.COMBAT);
    }

    private boolean isReallyWorld() {
        return "ReallyWorld".equals(mode.getValue());
    }

    public static void handlePreAttack(Player player, Entity target) {
        PacketCriticals pm = ModuleManager.getInstance().get(PacketCriticals.class);
        if (pm == null || !pm.isEnabled()) return;
        if (target == null || !target.isAlive() || target instanceof EndCrystal) return;
        pm.sendGrimCrit();
    }

    private void sendGrimCrit() {
        LocalPlayer player = mc.player;
        if (player == null || mc.getConnection() == null) return;
        if (isReallyWorld() && inWeb(player)) return;
        if (player.onGround()) return;
        double y = player.getY();
        if (y == Math.floor(y)) return;

        double offset = 1e-7 + Math.random() * (1e-6 - 1e-7);
        mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(
                player.getX(), y - offset, player.getZ(), player.getYRot(), player.getXRot(), false, false));
    }

    private static boolean inWeb(LocalPlayer player) {
        var box = player.getBoundingBox();
        int minX = net.minecraft.util.Mth.floor(box.minX);
        int minY = net.minecraft.util.Mth.floor(box.minY);
        int minZ = net.minecraft.util.Mth.floor(box.minZ);
        int maxX = net.minecraft.util.Mth.floor(box.maxX);
        int maxY = net.minecraft.util.Mth.floor(box.maxY);
        int maxZ = net.minecraft.util.Mth.floor(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (player.level().getBlockState(new net.minecraft.core.BlockPos(x, y, z)).is(net.minecraft.world.level.block.Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}