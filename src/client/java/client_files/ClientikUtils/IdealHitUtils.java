package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import project.petrovich_26_2.mixin.client.ItemInHandRendererAccessor;

import java.util.List;

public class IdealHitUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    private IdealHitUtils() {
    }

    private static boolean isInWeb() {
        Player player = mc.player;
        if (player == null || mc.level == null) return false;
        var box = player.getBoundingBox();
        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (mc.level.getBlockState(new net.minecraft.core.BlockPos(x, y, z)).is(Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean cooldownIsReached(boolean toSprinting) {
        if (mc.player == null) return false;
        float cooldown = 0;

        var hand = mc.player.getMainHandItem();
        if (hand.isEmpty()) cooldown = 0.9f;
        if (hand.is(ItemTags.SWORDS)) cooldown = 0.52f;
        if (toSprinting) cooldown -= 0.15f;

        ItemInHandRendererAccessor accessor = (ItemInHandRendererAccessor) mc.gameRenderer.itemInHandRenderer;
        return accessor.petrovich$getMainHandHeight() > cooldown;
    }

    public static boolean canAIFall() {
        if (mc.player == null || mc.level == null) return false;
        return (getBlock(0, 3, 0) == Blocks.AIR
                && getBlock(0, 2, 0) == Blocks.AIR
                && getBlock(0, 1, 0) == Blocks.AIR);
    }

    public static boolean canCritical() {
        Player player = mc.player;
        if (player == null) return false;

        double effectiveJumpHeight = player.maxUpStep();
        Vec3 jumpVec = new Vec3(0, effectiveJumpHeight, 0);
        Vec3 allowedMovement = Entity.collideBoundingBox(player, jumpVec, player.getBoundingBox(), player.level(), List.of());

        boolean airborneDescending = !player.onGround() && player.getDeltaMovement().y < 0;

        boolean notCrit = player.isInLava()
                || player.onClimbable()
                || player.isUnderWater()
                || player.hasEffect(MobEffects.LEVITATION)
                || player.hasEffect(MobEffects.SLOW_FALLING)
                || player.hasEffect(MobEffects.BLINDNESS)
                || isInWeb()
                || player.isPassenger()
                || player.getAbilities().flying
                || (allowedMovement.y < effectiveJumpHeight - 0.5 && player.onGround())
                || (player.onGround() && !mc.options.keyJump.isDown()
                        && !client_files.Petrovich.Combat.Aura.onlySpaceActive());

        return player.isFallFlying() || notCrit || airborneDescending;
    }

    public static Block getBlock(double x, double y, double z) {
        return mc.level.getBlockState(mc.player.blockPosition().offset((int) x, (int) y, (int) z)).getBlock();
    }

    public static boolean findFall(float fallDistance) {
        if (mc.player == null) return false;
        Vec3 rotationVec = mc.player.getViewVector(1.0F);
        double tempVelocityX = mc.player.getDeltaMovement().x;
        double tempVelocityY = mc.player.getDeltaMovement().y;
        double tempVelocityZ = mc.player.getDeltaMovement().z;

        float n = Mth.cos(mc.player.getXRot() * 0.017453292f);
        n = (float) (n * n * Math.min(rotationVec.length() / 0.4, 1.0));

        Vec3 vec3d = new Vec3(tempVelocityX, tempVelocityY, tempVelocityZ).add(0.0, 0.08 * (-1.0 + n * 0.75), 0.0);
        tempVelocityY = vec3d.y * 0.9800000190734863;

        return tempVelocityY < fallDistance;
    }
}