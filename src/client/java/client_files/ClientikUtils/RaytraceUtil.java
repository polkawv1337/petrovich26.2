package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RaytraceUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    private RaytraceUtil() {
    }

    public static BlockHitResult raycast(Vec3 start, Vec3 end, ClipContext.Block shapeType, Entity entity) {
        return mc.level.clip(new ClipContext(start, end, shapeType, ClipContext.Fluid.NONE, entity));
    }

    public static boolean rayTrace(Vec3 clientVec, double range, net.minecraft.world.phys.AABB box) {
        Vec3 cameraVec = mc.player.getEyePosition();
        if (box.contains(cameraVec)) return true;
        return box.clip(cameraVec, cameraVec.add(clientVec.scale(range))).isPresent();
    }
}