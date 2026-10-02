package client_files.ClientikUtils;

import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PredictUtils {

    private PredictUtils() {
    }

    public static Vec3 predict(LivingEntity target, double ticksAhead) {
        if (target.isFallFlying()) {
            return predictElytra(target, Math.max(1, (int) Math.ceil(ticksAhead)));
        }

        double dispX = target.getX() - target.xo;
        double dispY = target.getY() - target.yo;
        double dispZ = target.getZ() - target.zo;

        if (Math.hypot(dispX, dispZ) * 20 <= 1 && dispY <= 1) {
            return target.position().add(0, target.getBbHeight() / 2, 0);
        }

        Vec3 forward = Vec3.directionFromRotation(
                target.getXRot() + (target.getXRot() - target.xRotO),
                target.getYRot() + (target.getYRot() - target.yRotO))
                .scale(new Vec3(dispX, dispY, dispZ).length() * ticksAhead);

        Vec3 vec3d = Vec3.directionFromRotation(
                target.getXRot() + (target.getXRot() - target.xRotO),
                target.getYRot() + (target.getYRot() - target.yRotO));
        float f = target.getXRot() * 0.017453292F;
        double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
        double e = forward.horizontalDistance();
        boolean bl = target.getDeltaMovement().y <= 0.0;
        double g = bl && target.hasEffect(MobEffects.SLOW_FALLING)
                ? Math.min(target.getGravity(), 0.01)
                : target.getGravity();
        double h = Mth.square(Math.cos((double) f));
        forward = forward.add(0.0, g * (-1.0 + h * 0.75), 0.0);

        double i;
        if (forward.y < 0.0 && d > 0.0) {
            i = forward.y * -0.1 * h;
            forward = forward.add(vec3d.x * i / d, i, vec3d.z * i / d);
        }

        if (f < 0.0F && d > 0.0) {
            i = e * (double) (-Mth.sin(f)) * 0.04;
            forward = forward.add(-vec3d.x * i / d, i * 2.2f, -vec3d.z * i / d);
        }

        if (d > 0.0) {
            forward = forward.add((vec3d.x / d * e - forward.x) * 0.1, 0.0, (vec3d.z / d * e - forward.z) * 0.1);
        }

        return target.position().add(forward);
    }

    public static Vec3 predictElytra(LivingEntity target, int ticks) {
        if (ticks <= 0) {
            return target.position();
        }

        Vec3 pos = target.position();
        Vec3 vel = target.getDeltaMovement();

        for (int i = 0; i < ticks; i++) {
            vel = elytraTick(target, vel);
            pos = pos.add(vel);
        }

        return pos;
    }

    private static Vec3 elytraTick(LivingEntity target, Vec3 velocity) {
        double gravity = target.getGravity();

        Vec3 look = Vec3.directionFromRotation(target.getXRot(), target.getYRot());
        float pitchRad = target.getXRot() * ((float) Math.PI / 180f);
        double g = Math.sqrt(look.x * look.x + look.z * look.z);
        double horizontalSpeed = velocity.horizontalDistance();
        double lookLen = look.length();
        float cos = Mth.cos(pitchRad);
        cos = (float) (cos * (cos * Math.min(1.0, lookLen / 0.4)));

        Vec3 e = velocity.add(0.0, gravity * (-1.0 + cos * 0.75), 0.0);

        double k;
        if (e.y < 0.0 && g > 0.0) {
            k = e.y * -0.1 * cos;
            e = e.add(look.x * k / g, k, look.z * k / g);
        }
        if (pitchRad < 0.0f && g > 0.0) {
            k = horizontalSpeed * (-Mth.sin(pitchRad)) * 0.04;
            e = e.add(-look.x * k / g, k * 3.2, -look.z * k / g);
        }
        if (g > 0.0) {
            e = e.add((look.x / g * horizontalSpeed - e.x) * 0.1, 0.0, (look.z / g * horizontalSpeed - e.z) * 0.1);
        }

        return e.multiply(0.99, 0.98, 0.99);
    }
}