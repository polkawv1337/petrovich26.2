package client_files.render.render.particle;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.ColorUtils;
import net.minecraft.resources.Identifier;

public class Particle {
    private float x, y, z;
    private float velX, velY, velZ;
    private float ageSeconds;
    private final float lifetimeSeconds;
    private final ColorRGBA startColor;
    private final ColorRGBA endColor;
    private final float size;

    private final float gravity;

    private final float drag;

    private final float bounce;
    private boolean onGround = false;
    private Identifier texture;

    public Particle(float x, float y, float z,
                    float velX, float velY, float velZ,
                    float lifetimeSeconds,
                    ColorRGBA startColor, ColorRGBA endColor,
                    float size) {
        this(x, y, z, velX, velY, velZ, lifetimeSeconds, startColor, endColor, size, 0.6f, 0.6f, 0.0f, null);
    }

    public Particle(float x, float y, float z,
                    float velX, float velY, float velZ,
                    float lifetimeSeconds,
                    ColorRGBA startColor, ColorRGBA endColor,
                    float size, float gravity, float drag, float bounce) {
        this(x, y, z, velX, velY, velZ, lifetimeSeconds, startColor, endColor, size, gravity, drag, bounce, null);
    }

    public Particle(float x, float y, float z,
                    float velX, float velY, float velZ,
                    float lifetimeSeconds,
                    ColorRGBA startColor, ColorRGBA endColor,
                    float size, float gravity, float drag, float bounce,
                    Identifier texture) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
        this.lifetimeSeconds = Math.max(0.01f, lifetimeSeconds);
        this.startColor = startColor != null ? startColor : ColorRGBA.WHITE;
        this.endColor = endColor != null ? endColor : startColor;
        this.size = Math.max(0.01f, size);
        this.gravity = gravity;
        this.drag = Math.max(0.0f, drag);
        this.bounce = Math.clamp(bounce, 0.0f, 0.9f);
        this.ageSeconds = 0f;
        this.texture = texture;
    }

    public Identifier getTexture() { return texture; }
    public void setTexture(Identifier texture) { this.texture = texture; }

    public boolean isAlive() {
        return ageSeconds < lifetimeSeconds;
    }

    public void tick(float deltaSeconds, float globalGravity) {
        float g = this.gravity;

        if (Math.abs(globalGravity - 9.8f) < 0.01f) {

        } else {
            g = globalGravity;
        }
        float dt = Math.min(deltaSeconds, 0.05f);
        velY -= g * dt;
        if (drag > 0.0f) {
            float f = Math.max(0.0f, 1.0f - drag * dt);
            velX *= f;
            velY *= f;
            velZ *= f;
        }
        x += velX * dt;
        y += velY * dt;
        z += velZ * dt;
        ageSeconds += dt;

        collideWithGround();
        if (y < -64.0f) ageSeconds = lifetimeSeconds;
    }

    private void collideWithGround() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc == null || mc.level == null) return;
            net.minecraft.core.BlockPos below = new net.minecraft.core.BlockPos(
                    (int) Math.floor(x), (int) Math.floor(y - 0.05f), (int) Math.floor(z));
            net.minecraft.world.level.block.state.BlockState state = mc.level.getBlockState(below);
            boolean solid;
            try {
                solid = state.isSolid();
            } catch (Throwable t) {
                solid = !state.isAir();
            }
            if (!solid) {
                onGround = false;
                return;
            }

            float topY = below.getY() + 1.0f + 0.02f;
            if (y <= topY && velY <= 0.0f) {
                y = topY;
                onGround = true;
                if (bounce > 0.01f) {
                    velY = -velY * bounce;
                    velX *= 0.7f;
                    velZ *= 0.7f;
                    if (Math.abs(velY) < 0.15f) {
                        velY = 0.0f;
                        ageSeconds += 0.05f;
                    }
                } else {
                    velX = 0.0f;
                    velY = 0.0f;
                    velZ = 0.0f;
                    ageSeconds += 0.06f;
                }
            }
        } catch (Throwable ignored) {}
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public float getSize() { return size; }
    public float getAgeSeconds() { return ageSeconds; }
    public float getLifetimeSeconds() { return lifetimeSeconds; }
    public float getGravity() { return gravity; }
    public boolean isOnGround() { return onGround; }

    public ColorRGBA currentColor() {
        float t = Math.clamp(ageSeconds / lifetimeSeconds, 0f, 1f);
        return ColorUtils.lerp(startColor, endColor, t);
    }

    public ColorRGBA getColor() {
        return currentColor();
    }
}
