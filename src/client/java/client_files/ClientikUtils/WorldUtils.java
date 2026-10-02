package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WorldUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    private WorldUtils() {
    }

    public static boolean isInWeb() {
        AABB box = mc.player.getBoundingBox();

        int minX = Mth.floor(box.minX);
        int minY = Mth.floor(box.minY);
        int minZ = Mth.floor(box.minZ);
        int maxX = Mth.floor(box.maxX);
        int maxY = Mth.floor(box.maxY);
        int maxZ = Mth.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = mc.level.getBlockState(pos);

                    if (state.is(Blocks.COBWEB)) {
                        if (state.getCollisionShape(mc.level, pos)
                                .toAabbs()
                                .stream()
                                .anyMatch(shape -> shape.move(pos.getX(), pos.getY(), pos.getZ()).intersects(box))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static Direction getBestVisibleFaceToObsidian(BlockPos pos, Player entity) {
        Direction bestFace = null;
        double bestDist = 0;

        for (Direction face : Direction.values()) {
            BlockPos sidePos = pos.relative(face);

            if (!mc.level.getBlockState(sidePos).isAir()) {
                continue;
            }

            Vec3 point = getFacePoint(pos, face);
            Rotation rot = new Rotation(mc.player.getEyePosition(), point);

            if (RaycastUtils.raycastBlock(rot, pos, 4.5F) == null) {
                continue;
            }

            double damage = CrystalDamageCalculator.getDamageOfGhostBlock(Vec3.atCenterOf(sidePos.above()), entity, sidePos);
            if (damage > bestDist) {
                bestDist = damage;
                bestFace = face;
            }
        }

        return bestFace;
    }

    public static Direction getBestVisibleFace(BlockPos pos, Player entity) {
        Vec3 eye = entity.getEyePosition();
        Direction bestFace = null;
        double bestDist = Double.MAX_VALUE;

        for (Direction face : Direction.values()) {
            Vec3 point = getFacePoint(pos, face);
            Rotation rot = new Rotation(mc.player.getEyePosition(), point);

            if (RaycastUtils.raycastBlock(rot, pos, 4.5F) == null) {
                continue;
            }

            double dist = eye.distanceToSqr(point);
            if (dist < bestDist) {
                bestDist = dist;
                bestFace = face;
            }
        }

        return bestFace;
    }

    public static Vec3 getFacePoint(BlockPos pos, Direction face) {
        return new Vec3(
                pos.getX() + 0.5 + face.getStepX() * 0.5001,
                pos.getY() + 0.5 + face.getStepY() * 0.5001,
                pos.getZ() + 0.5 + face.getStepZ() * 0.5001
        );
    }

    public static BlockPos findNearestPlaceableBlock() {
        Vec3 playerPos = mc.player.position();
        BlockPos feetBlock = mc.player.blockPosition();

        int placementY = feetBlock.getY() - 1;

        BlockPos[] candidates = new BlockPos[] {
                new BlockPos(feetBlock.getX() - 1, placementY, feetBlock.getZ()),
                new BlockPos(feetBlock.getX() + 1, placementY, feetBlock.getZ()),
                new BlockPos(feetBlock.getX(), placementY, feetBlock.getZ() - 1),
                new BlockPos(feetBlock.getX(), placementY, feetBlock.getZ() + 1)
        };

        AABB playerBox = mc.player.getBoundingBox();

        BlockPos nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (BlockPos candidate : candidates) {

            AABB blockBox = new AABB(candidate);

            if (playerBox.inflate(0.15f).intersects(blockBox)) {
                continue;
            }

            Vec3 blockCenter = Vec3.atCenterOf(candidate.above());
            double distance = playerPos.distanceTo(blockCenter);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }

        return nearest;
    }
}