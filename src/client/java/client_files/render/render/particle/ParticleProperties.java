package client_files.render.render.particle;

import client_files.render.render.core.color.ColorRGBA;
import org.joml.Vector3f;

import java.util.Random;

public record ParticleProperties(
        float minLifetime, float maxLifetime,
        float minSize, float maxSize,
        ColorRGBA startColor, ColorRGBA endColor,
        float gravity,
        Vector3f minVelocity, Vector3f maxVelocity
) {
    public Particle spawn(float x, float y, float z, Random random) {
        float rLife = random.nextFloat();
        float lifetime = minLifetime + (maxLifetime - minLifetime) * rLife;

        float rSize = random.nextFloat();
        float size = minSize + (maxSize - minSize) * rSize;

        float vx = minVelocity.x + (maxVelocity.x - minVelocity.x) * random.nextFloat();
        float vy = minVelocity.y + (maxVelocity.y - minVelocity.y) * random.nextFloat();
        float vz = minVelocity.z + (maxVelocity.z - minVelocity.z) * random.nextFloat();

        float g = Math.clamp(gravity, -1.0f, 3.0f);
        float bounce = g > 1.5f ? 0.35f : 0.0f;
        return new Particle(x, y, z, vx, vy, vz, lifetime, startColor, endColor, size, g, 0.8f, bounce);
    }
}
