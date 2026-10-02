package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MultipointUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    private MultipointUtils() {
    }

    public static Vec3 getNearestPoint(Entity entity, double expand) {
        return getNearestPoint(entity, expand, 0.1);
    }

    public static Vec3 getNearestPoint(Entity entity, double expand, double step) {
        AABB box = entity.getBoundingBox();
        Vec3 bestVec = null;
        double closestDistance = Double.MAX_VALUE;

        for (double x = box.minX + expand; x <= box.maxX - expand; x += step) {
            for (double y = box.minY + expand; y <= box.maxY - expand; y += step) {
                for (double z = box.minZ + expand; z <= box.maxZ - expand; z += step) {
                    Vec3 sample = new Vec3(x, y, z);
                    double dist = mc.player.getEyePosition().distanceTo(sample);
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        bestVec = sample;
                    }
                }
            }
        }
        return bestVec;
    }

    public static Vec3 getNearestPoint(AABB box, double expand) {
        Vec3 eye = mc.player.getEyePosition();
        double x = Math.max(box.minX + expand, Math.min(eye.x, box.maxX - expand));
        double y = Math.max(box.minY + expand, Math.min(eye.y, box.maxY - expand));
        double z = Math.max(box.minZ + expand, Math.min(eye.z, box.maxZ - expand));
        return new Vec3(x, y, z);
    }
}