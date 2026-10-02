package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ModeSetting;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Movement.Speed;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class Velocity extends Module {

    private static final Minecraft mc = Minecraft.getInstance();

    private final ModeSetting mode = addSetting(new ModeSetting("Мод", "Обычный", "Обычный", "Грим"));
    private final BooleanSetting skipWithSpeed = addSetting((BooleanSetting) new BooleanSetting("Пропускать при активных спидах", true)
            .setVisible(() -> "Грим".equals(mode.getValue())));

    private int freezeMoves;
    private Vec3 savedVelocity;

    public Velocity() {
        super("Velocity", "Не позволяет разным условиям в мире откидывать вас", Category.COMBAT);
    }

    private boolean isGrim() {
        return "Грим".equals(mode.getValue());
    }

    private boolean onElytra() {
        if (mc.player == null) return false;
        if (mc.player.isFallFlying()) return true;
        return mc.player.getItemBySlot(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
    }

    private boolean skipFromSpeed() {
        if (!skipWithSpeed.getValue()) return false;
        Speed speed = ModuleManager.getInstance().get(Speed.class);
        return speed != null && speed.isEnabled();
    }

    @Override
    public void onTick() {
        if (mc.player == null) {
            freezeMoves = 0;
            savedVelocity = null;
            return;
        }
        if (!isGrim() || onElytra()) {
            freezeMoves = 0;
            savedVelocity = null;
            return;
        }
        if (freezeMoves > 0) {
            if (savedVelocity == null) {
                savedVelocity = mc.player.getDeltaMovement();
            }
            mc.player.setDeltaMovement(0.0, mc.player.getDeltaMovement().y, 0.0);
            freezeMoves--;
        } else if (savedVelocity != null) {
            mc.player.setDeltaMovement(savedVelocity);
            savedVelocity = null;
        }
    }

    public static boolean shouldCancelMotion(ClientboundSetEntityMotionPacket packet) {
        Velocity v = ModuleManager.getInstance().get(Velocity.class);
        if (v == null || !v.isEnabled()) return false;
        if (mc.player == null || v.onElytra()) return false;
        if (packet.id() != mc.player.getId()) return false;

        if (v.isGrim()) {
            if (v.skipFromSpeed()) return false;
            v.freezeMoves = 2;
        }
        return true;
    }

    public static boolean shouldCancelExplosion(ClientboundExplodePacket packet) {
        Velocity v = ModuleManager.getInstance().get(Velocity.class);
        if (v == null || !v.isEnabled()) return false;
        if (mc.player == null || v.onElytra()) return false;
        if (!v.isGrim()) return false;
        if (v.skipFromSpeed()) return false;
        v.freezeMoves = 2;
        return true;
    }

    @Override
    public void onDisable() {
        freezeMoves = 0;
        savedVelocity = null;
        super.onDisable();
    }
}