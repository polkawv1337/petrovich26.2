package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RaycastUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    private RaycastUtils() {
    }

    public static BlockHitResult raycast(Player player, double range, Rotation angle, boolean includeFluids) {
        return raycast(player.getEyePosition(), range, angle, includeFluids);
    }

    public static BlockHitResult raycast(Vec3 vec, double range, Rotation angle, boolean includeFluids) {
        Entity entity = mc.getCameraEntity();

        if (entity == null) {
            return null;
        }

        Vec3 rotationVec = angle.toVector();
        Vec3 end = vec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);

        if (mc.level == null) {
            return null;
        }

        ClipContext.Fluid fluidHandling = includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext context = new ClipContext(vec, end, ClipContext.Block.OUTLINE, fluidHandling, entity);

        return mc.level.clip(context);
    }

    public static BlockHitResult raycast(Vec3 start, Vec3 end, ClipContext.Block shapeType) {
        return raycast(start, end, shapeType, mc.player);
    }

    public static BlockHitResult raycast(Vec3 start, Vec3 end, ClipContext.Block shapeType, Entity entity) {
        return mc.level.clip(new ClipContext(start, end, shapeType, ClipContext.Fluid.NONE, entity));
    }

    public static boolean rayTrace(Vec3 clientVec, double range, AABB box) {
        Vec3 cameraVec = mc.player.getEyePosition();
        return box.contains(cameraVec) || box.clip(cameraVec, cameraVec.add(clientVec.scale(range))).isPresent();
    }

    public static boolean rayTrace(Rotation clientVec, double range, AABB box) {
        Vec3 cameraVec = mc.player.getEyePosition();
        return box.contains(cameraVec) || box.clip(cameraVec, cameraVec.add(clientVec.toVector().scale(range))).isPresent();
    }

    public static BlockData rayTraceBlock(Player player, double distance, net.minecraft.world.level.block.Block block) {
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 direction = Vec3.directionFromRotation(player.getXRot() + (player.getXRot() - player.xRotO),
                player.getYRot() - (player.getYRot() - player.yRotO));
        Vec3 end = start.add(direction.scale(distance));

        BlockHitResult hit = player.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() != HitResult.Type.BLOCK || !mc.level.getBlockState(hit.getBlockPos()).is(block)) return null;

        return new BlockData(hit.getBlockPos());
    }

    public static BlockHitResult raycastBlock(net.minecraft.core.BlockPos targetPos, double maxDistance) {
        if (mc.player == null || mc.level == null) {
            return null;
        }

        return raycastBlock(mc.player, targetPos, maxDistance);
    }

    public static BlockHitResult raycastBlock(Rotation rotation, net.minecraft.core.BlockPos targetPos, double maxDistance) {
        if (mc.player == null || mc.level == null) {
            return null;
        }

        return raycastBlock(rotation, mc.player, targetPos, maxDistance);
    }

    public static BlockHitResult raycastBlock(Player player, net.minecraft.core.BlockPos targetPos, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB blockBox = new AABB(targetPos);

        var hitOpt = blockBox.clip(eyePos, endPos);

        if (hitOpt.isEmpty()) {
            return null;
        }

        Vec3 hitVec = hitOpt.get();

        Direction side = getHitSide(hitVec, targetPos);

        return new BlockHitResult(hitVec, side, targetPos, false);
    }

    public static BlockHitResult raycastBlock(Rotation rotation, Player player, net.minecraft.core.BlockPos targetPos, double maxDistance) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = Vec3.directionFromRotation(rotation.getPitch(), rotation.getYaw());
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB blockBox = new AABB(targetPos);

        var hitOpt = blockBox.clip(eyePos, endPos);

        if (hitOpt.isEmpty()) {
            return null;
        }

        Vec3 hitVec = hitOpt.get();

        Direction side = getHitSide(hitVec, targetPos);

        return new BlockHitResult(hitVec, side, targetPos, false);
    }

    public static BlockHitResult raycastBlock(float yaw, float pitch, net.minecraft.core.BlockPos targetPos, double maxDistance) {
        if (mc.player == null) {
            return null;
        }

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = getRotationVec(yaw, pitch);
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB blockBox = new AABB(targetPos);
        var hitOpt = blockBox.clip(eyePos, endPos);

        if (hitOpt.isEmpty()) {
            return null;
        }

        Vec3 hitVec = hitOpt.get();
        Direction side = getHitSide(hitVec, targetPos);

        return new BlockHitResult(hitVec, side, targetPos, false);
    }

    public static boolean isLookingAt(net.minecraft.core.BlockPos targetPos, double maxDistance) {
        return raycastBlock(targetPos, maxDistance) != null;
    }

    public static boolean canReachBlock(net.minecraft.core.BlockPos blockPos, double maxDistance) {
        if (mc.player == null) return false;

        Vec3 eyePos = mc.player.getEyePosition();
        AABB blockBox = new AABB(blockPos);
        Vec3 closestPoint = getClosestPointOnBox(eyePos, blockBox);
        double distanceSq = eyePos.distanceToSqr(closestPoint);

        return distanceSq <= maxDistance * maxDistance;
    }

    public static boolean canReachEntity(Entity entity, double maxDistance) {
        if (mc.player == null || entity == null) return false;

        Vec3 eyePos = mc.player.getEyePosition();
        AABB entityBox = entity.getBoundingBox();

        Vec3 closestPoint = getClosestPointOnBox(eyePos, entityBox);
        double distanceSq = eyePos.distanceToSqr(closestPoint);

        return distanceSq <= maxDistance * maxDistance;
    }

    public static boolean canReachBox(AABB box, double maxDistance) {
        if (mc.player == null) return false;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 closestPoint = getClosestPointOnBox(eyePos, box);
        double distanceSq = eyePos.distanceToSqr(closestPoint);

        return distanceSq <= maxDistance * maxDistance;
    }

    public static double getDistanceToBlock(net.minecraft.core.BlockPos blockPos) {
        if (mc.player == null) return Double.MAX_VALUE;

        Vec3 eyePos = mc.player.getEyePosition();
        AABB blockBox = new AABB(blockPos);

        Vec3 closestPoint = getClosestPointOnBox(eyePos, blockBox);
        return eyePos.distanceTo(closestPoint);
    }

    public static double getDistanceToEntity(Entity entity) {
        if (mc.player == null || entity == null) return Double.MAX_VALUE;

        Vec3 eyePos = mc.player.getEyePosition();
        AABB entityBox = entity.getBoundingBox();

        Vec3 closestPoint = getClosestPointOnBox(eyePos, entityBox);
        return eyePos.distanceTo(closestPoint);
    }

    public static boolean isLookingAtEntity(Entity entity, double maxDistance) {
        if (mc.player == null || entity == null) return false;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB entityBox = entity.getBoundingBox();

        return entityBox.clip(eyePos, endPos).isPresent() || entityBox.contains(eyePos);
    }

    public static boolean isLookingAtEntity(Rotation rotation, Entity entity, double maxDistance) {
        if (mc.player == null || entity == null) return false;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = Vec3.directionFromRotation(rotation.getPitch(), rotation.getYaw());
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB entityBox = entity.getBoundingBox();

        return entityBox.clip(eyePos, endPos).isPresent() || entityBox.contains(eyePos);
    }

    public static Vec3 raycastEntity(Entity entity, double maxDistance) {
        if (mc.player == null || entity == null) return null;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB entityBox = entity.getBoundingBox();

        return entityBox.clip(eyePos, endPos).orElse(null);
    }

    public static Vec3 raycastEntity(Rotation rotation, Entity entity, double maxDistance) {
        if (mc.player == null || entity == null) return null;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = Vec3.directionFromRotation(rotation.getPitch(), rotation.getYaw());
        Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));

        AABB entityBox = entity.getBoundingBox();

        return entityBox.clip(eyePos, endPos).orElse(null);
    }

    private static Vec3 getClosestPointOnBox(Vec3 point, AABB box) {
        double x = clamp(point.x, box.minX, box.maxX);
        double y = clamp(point.y, box.minY, box.maxY);
        double z = clamp(point.z, box.minZ, box.maxZ);
        return new Vec3(x, y, z);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Direction getHitSide(Vec3 hitVec, net.minecraft.core.BlockPos blockPos) {
        double dx = hitVec.x - (blockPos.getX() + 0.5);
        double dy = hitVec.y - (blockPos.getY() + 0.5);
        double dz = hitVec.z - (blockPos.getZ() + 0.5);

        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);
        double absDz = Math.abs(dz);

        if (absDx > absDy && absDx > absDz) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (absDy > absDz) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    private static Vec3 getRotationVec(float yaw, float pitch) {
        double radPitch = Math.toRadians(pitch);
        double radYaw = Math.toRadians(yaw);

        double cosP = Math.cos(-radPitch);
        double x = Math.sin(-radYaw - Math.PI) * cosP;
        double y = Math.sin(-radPitch);
        double z = Math.cos(-radYaw - Math.PI) * cosP;

        return new Vec3(x, y, z);
    }

    public record BlockData(net.minecraft.core.BlockPos pos) {
    }
}