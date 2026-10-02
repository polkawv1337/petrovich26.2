package client_files.ClientikUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DirectionUtil {

    private DirectionUtil() {
    }

    public static Direction getFaceDirection(Player player, BlockPos targetPos) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);

        double cx = targetPos.getX() + 0.5;
        double cy = targetPos.getY() + 0.5;
        double cz = targetPos.getZ() + 0.5;

        double minX = targetPos.getX();
        double minY = targetPos.getY();
        double minZ = targetPos.getZ();
        double maxX = targetPos.getX() + 1.0;
        double maxY = targetPos.getY() + 1.0;
        double maxZ = targetPos.getZ() + 1.0;

        Direction closestFace = null;
        double closestT = Double.MAX_VALUE;

        double t = tryHitPlaneY(eyePos, lookVec, maxY, minX, maxX, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.UP;
        }

        t = tryHitPlaneY(eyePos, lookVec, minY, minX, maxX, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.DOWN;
        }

        t = tryHitPlaneZ(eyePos, lookVec, minZ, minX, maxX, minY, maxY);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.NORTH;
        }

        t = tryHitPlaneZ(eyePos, lookVec, maxZ, minX, maxX, minY, maxY);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.SOUTH;
        }

        t = tryHitPlaneX(eyePos, lookVec, minX, minY, maxY, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.WEST;
        }

        t = tryHitPlaneX(eyePos, lookVec, maxX, minY, maxY, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.EAST;
        }

        if (closestFace == null) {
            closestFace = getFallbackDirection(eyePos, cx, cy, cz);
        }

        return closestFace;
    }

    public static Direction getFaceDirection(Rotation rotation, Player player, BlockPos targetPos) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = Vec3.directionFromRotation(rotation.getPitch(), rotation.getYaw());

        double cx = targetPos.getX() + 0.5;
        double cy = targetPos.getY() + 0.5;
        double cz = targetPos.getZ() + 0.5;

        double minX = targetPos.getX();
        double minY = targetPos.getY();
        double minZ = targetPos.getZ();
        double maxX = targetPos.getX() + 1.0;
        double maxY = targetPos.getY() + 1.0;
        double maxZ = targetPos.getZ() + 1.0;

        Direction closestFace = null;
        double closestT = Double.MAX_VALUE;

        double t = tryHitPlaneY(eyePos, lookVec, maxY, minX, maxX, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.UP;
        }

        t = tryHitPlaneY(eyePos, lookVec, minY, minX, maxX, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.DOWN;
        }

        t = tryHitPlaneZ(eyePos, lookVec, minZ, minX, maxX, minY, maxY);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.NORTH;
        }

        t = tryHitPlaneZ(eyePos, lookVec, maxZ, minX, maxX, minY, maxY);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.SOUTH;
        }

        t = tryHitPlaneX(eyePos, lookVec, minX, minY, maxY, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.WEST;
        }

        t = tryHitPlaneX(eyePos, lookVec, maxX, minY, maxY, minZ, maxZ);
        if (t > 0 && t < closestT) {
            closestT = t;
            closestFace = Direction.EAST;
        }

        if (closestFace == null) {
            closestFace = getFallbackDirection(eyePos, cx, cy, cz);
        }

        return closestFace;
    }

    private static double tryHitPlaneY(Vec3 eye, Vec3 dir, double yVal,
                                       double minX, double maxX,
                                       double minZ, double maxZ) {
        if (dir.y == 0) return -1;
        double t = (yVal - eye.y) / dir.y;
        if (t <= 0) return -1;
        double hitX = eye.x + t * dir.x;
        double hitZ = eye.z + t * dir.z;
        if (hitX >= minX && hitX <= maxX && hitZ >= minZ && hitZ <= maxZ) {
            return t;
        }
        return -1;
    }

    private static double tryHitPlaneZ(Vec3 eye, Vec3 dir, double zVal,
                                       double minX, double maxX,
                                       double minY, double maxY) {
        if (dir.z == 0) return -1;
        double t = (zVal - eye.z) / dir.z;
        if (t <= 0) return -1;
        double hitX = eye.x + t * dir.x;
        double hitY = eye.y + t * dir.y;
        if (hitX >= minX && hitX <= maxX && hitY >= minY && hitY <= maxY) {
            return t;
        }
        return -1;
    }

    private static double tryHitPlaneX(Vec3 eye, Vec3 dir, double xVal,
                                       double minY, double maxY,
                                       double minZ, double maxZ) {
        if (dir.x == 0) return -1;
        double t = (xVal - eye.x) / dir.x;
        if (t <= 0) return -1;
        double hitY = eye.y + t * dir.y;
        double hitZ = eye.z + t * dir.z;
        if (hitY >= minY && hitY <= maxY && hitZ >= minZ && hitZ <= maxZ) {
            return t;
        }
        return -1;
    }

    private static Direction getFallbackDirection(Vec3 eye, double cx, double cy, double cz) {
        double dx = eye.x - cx;
        double dy = eye.y - cy;
        double dz = eye.z - cz;

        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);

        if (ay >= ax && ay >= az) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else if (ax >= az) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}