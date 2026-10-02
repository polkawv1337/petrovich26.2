package client_files.Petrovich.Movement;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import client_files.Petrovich.Combat.Aura;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class ElytraMotion extends Module {

    private final SliderSetting distance = addSetting(new SliderSetting("Дистанция", 1.5f, 1.0f, 3.0f, 0.1f));
    private final BooleanSetting autoFirework = addSetting(new BooleanSetting("АвтоФейрверк", false));

    private long lastFireworkMs;

    public ElytraMotion() {
        super("ElytraMotion", "Позволяет зависнуть на элитрах", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (!mc.player.isFallFlying()) {
            mc.player.setNoGravity(false);
            return;
        }

        if (Aura.target == null) {
            mc.player.setNoGravity(false);
            return;
        }

        boolean groundTarget = Aura.target.onGround();
        boolean aboutToHit = Aura.ticksToAttack > 0
                && Aura.ticksToAttack <= (groundTarget ? 4 : 2);

        double dist = mc.player.distanceTo(Aura.target);
        if (dist < distance.getValue() && !aboutToHit) {
            mc.player.setDeltaMovement(Vec3.ZERO);
            mc.player.setNoGravity(true);
        } else if (aboutToHit) {
            mc.player.setNoGravity(false);
            Vec3 v = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(v.x, Math.min(v.y, -0.12), v.z);
        } else {
            mc.player.setNoGravity(false);
        }

        if (autoFirework.getValue() && System.currentTimeMillis() - lastFireworkMs > 1000) {
            useFirework();
            lastFireworkMs = System.currentTimeMillis();
        }
    }

    private void useFirework() {
        if (mc.player == null || mc.gameMode == null) return;

        var connection = mc.getConnection();
        if (connection == null) return;

        if (mc.player.getMainHandItem().getItem() == Items.FIREWORK_ROCKET) {
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            return;
        }
        if (mc.player.getOffhandItem().getItem() == Items.FIREWORK_ROCKET) {
            mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
            return;
        }

        int slot = InventoryUtil.searchItemHotbar(Items.FIREWORK_ROCKET);
        if (slot == -1) return;

        int previousSlot = mc.player.getInventory().getSelectedSlot();
        connection.send(new ServerboundSetCarriedItemPacket(slot));
        mc.player.getInventory().setSelectedSlot(slot);
        mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
        mc.player.getInventory().setSelectedSlot(previousSlot);
        connection.send(new ServerboundSetCarriedItemPacket(previousSlot));
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setNoGravity(false);
        }
        super.onDisable();
    }
}