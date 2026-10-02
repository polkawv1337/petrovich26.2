package client_files.Petrovich.Combat;

import client_files.ClientikUtils.Category;
import client_files.Module;
import client_files.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

public class ElytraLock extends Module {

    private static final int ELYTRA_THORNS_VELOCITY_TICKS = 8;
    private static final byte THORNS_STATUS = 33;
    private static final int ENEMY_LOCK_TICKS = 10;

    private static int elytraThornsVelocityTicks = 0;
    private static boolean thornsHit = false;

    private static int enemyLockId = -1;
    private static int enemyLockTicks = 0;

    public ElytraLock() {
        super("ElytraLock", "Отменяет откидывание от шипов и кнобек на элитрах врага", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }

    private void reset() {
        elytraThornsVelocityTicks = 0;
        thornsHit = false;
        enemyLockId = -1;
        enemyLockTicks = 0;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            reset();
            return;
        }
        if (!mc.player.isFallFlying()) {
            elytraThornsVelocityTicks = 0;
        }
        if (elytraThornsVelocityTicks > 0) {
            elytraThornsVelocityTicks--;
        }

        if (enemyLockId != -1) {
            if (enemyLockTicks > 0) {
                enemyLockTicks--;
            } else {
                Entity e = mc.level.getEntity(enemyLockId);
                if (e == null || !(e instanceof net.minecraft.world.entity.LivingEntity le) || !le.isFallFlying()) {
                    enemyLockId = -1;
                }
            }
        }
    }

    public static void onPlayerAttack(Entity target) {
        if (!isModuleEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!(target instanceof Player p)) return;
        if (p == mc.player || !p.isFallFlying()) return;
        if (!hasThorns(p)) return;

        enemyLockId = p.getId();
        enemyLockTicks = ENEMY_LOCK_TICKS;
    }

    private static boolean hasThorns(Player p) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = p.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            try {
                if (stack.getEnchantments().keySet().stream().anyMatch(h -> h.is(Enchantments.THORNS))) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public static boolean shouldCancelEntityStatus(ClientboundEntityEventPacket packet) {
        if (!isModuleEnabled()) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            thornsHit = false;
            return false;
        }

        if (packet.getEventId() == THORNS_STATUS
                && packet.getEntity(mc.level) == mc.player) {
            thornsHit = true;
            if (mc.player.isFallFlying()) {
                elytraThornsVelocityTicks = ELYTRA_THORNS_VELOCITY_TICKS;
            }
            return true;
        }
        return false;
    }

    public static boolean shouldCancelEntityMotion(ClientboundSetEntityMotionPacket packet) {
        if (!isModuleEnabled()) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            thornsHit = false;
            enemyLockId = -1;
            return false;
        }

        if (enemyLockId != -1 && packet.id() == enemyLockId) {
            Entity e = mc.level.getEntity(packet.id());
            if (e instanceof net.minecraft.world.entity.LivingEntity le && le.isFallFlying() && enemyLockTicks > 0) {
                return true;
            }
            enemyLockId = -1;
        }

        if (packet.id() != mc.player.getId()) return false;

        if (thornsHit) {
            thornsHit = false;
            return true;
        }
        if (shouldCancelElytraThornsVelocity()) {
            elytraThornsVelocityTicks = 0;
            return true;
        }
        return false;
    }

    private static boolean shouldCancelElytraThornsVelocity() {
        Minecraft mc = Minecraft.getInstance();
        return elytraThornsVelocityTicks > 0 && mc.player != null && mc.player.isFallFlying();
    }

    private static boolean isModuleEnabled() {
        return ModuleManager.getInstance().get(ElytraLock.class).isEnabled();
    }
}