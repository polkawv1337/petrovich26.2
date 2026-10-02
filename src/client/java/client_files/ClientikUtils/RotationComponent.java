package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RotationComponent {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Rotation lastRotation;

    private RotationComponent() {
    }

    public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;
        float forward = 1F;
        if (moveForward < 0F) forward = -0.5F;
        if (moveForward > 0F) forward = 0.5F;
        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;
        return Math.toRadians(rotationYaw);
    }

    public static Vec2Tuple fixMovement(float forward, float strafe, float yaw) {
        if (forward == 0 && strafe == 0) {
            return new Vec2Tuple(forward, strafe);
        }

        double targetAngle = Mth.wrapDegrees(Math.toDegrees(direction(yaw, forward, strafe)));

        float bestForward = 0, bestStrafe = 0;
        float smallestDifference = Float.MAX_VALUE;

        for (float testForward = -1F; testForward <= 1F; testForward++) {
            for (float testStrafe = -1F; testStrafe <= 1F; testStrafe++) {
                if (testForward == 0 && testStrafe == 0) continue;

                double testAngle = Mth.wrapDegrees(Math.toDegrees(direction(yaw, testForward, testStrafe)));
                float difference = Math.abs(Mth.wrapDegrees((float) (targetAngle - testAngle)));

                if (difference < smallestDifference) {
                    smallestDifference = difference;
                    bestForward = testForward;
                    bestStrafe = testStrafe;
                }
            }
        }

        return new Vec2Tuple(bestForward, bestStrafe);
    }

    public static void update(Rotation targetRotation, float yawSpeed, float pitchSpeed, float yawReturnSpeed,
            float pitchReturnSpeed, int timeout, int priority, boolean clientRotation) {
        if (!clientRotation) {
            spoofRotation(targetRotation);
            return;
        }
        updateRotation(targetRotation, yawSpeed, pitchSpeed, true);
    }

    public static void spoofRotation(Rotation targetRotation) {
        LocalPlayer player = mc.player;
        if (player == null || player.connection == null) return;
        lastRotation = targetRotation;
        player.connection.send(new ServerboundMovePlayerPacket.Rot(
                targetRotation.getYaw(), targetRotation.getPitch(), player.onGround(), player.horizontalCollision));
    }

    public static void spoofLastRotation() {
        if (lastRotation != null) {
            spoofRotation(lastRotation);
        }
    }

    public static Rotation getLastRotation() {
        return lastRotation;
    }

    public static void resetLastRotation() {
        lastRotation = null;
    }

    public static Vec3 getSpoofedLookVector() {
        LocalPlayer player = mc.player;
        if (player == null) return Vec3.ZERO;
        if (lastRotation == null) return player.getViewVector(1.0F);
        return lastRotation.toVector();
    }

    private static boolean updateRotation(Rotation targetRotation, float yawSpeed, float pitchSpeed,
            boolean clientRotation) {
        LocalPlayer player = mc.player;
        if (player == null) return false;

        float yawDelta = Mth.wrapDegrees(targetRotation.getYaw() - player.getYRot());
        float pitchDelta = targetRotation.getPitch() - player.getXRot();

        float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed);
        float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed);

        float yaw = player.getYRot();
        yaw += GCDFixer.getFixRotate(Mth.clamp(yawDelta, -clampedYaw, clampedYaw));
        player.setYRot(yaw);
        player.setXRot(Mth.clamp(
                player.getXRot() + GCDFixer.getFixRotate(Mth.clamp(pitchDelta, -clampedPitch, clampedPitch)),
                -90F, 90F));

        if (clientRotation) {
            player.yHeadRot = player.getYRot();
            player.yBodyRot = player.getYRot();
        }
        return true;
    }

    public static final class Vec2Tuple {
        private final float forward;
        private final float strafe;

        public Vec2Tuple(float forward, float strafe) {
            this.forward = forward;
            this.strafe = strafe;
        }

        public float forward() {
            return forward;
        }

        public float strafe() {
            return strafe;
        }
    }
}