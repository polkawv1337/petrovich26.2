package client_files.render.render.particle;

import org.joml.Vector3f;

import java.util.Random;

public class ParticleEmitter {
    private final Vector3f position = new Vector3f();
    private ParticleProperties properties;
    private float particlesPerSecond;
    private float spawnAccumulator = 0f;
    private final Random random = new Random();

    public ParticleEmitter(Vector3f position, ParticleProperties properties, float particlesPerSecond) {
        if (position != null) {
            this.position.set(position);
        }
        this.properties = properties;
        this.particlesPerSecond = Math.max(0f, particlesPerSecond);
    }

    public Vector3f getPosition() { return position; }
    public ParticleProperties getProperties() { return properties; }
    public void setProperties(ParticleProperties properties) { this.properties = properties; }
    public float getParticlesPerSecond() { return particlesPerSecond; }
    public void setParticlesPerSecond(float rate) { this.particlesPerSecond = Math.max(0f, rate); }

    public void tick(float deltaSeconds, ParticleSystem system) {
        if (properties == null || system == null || particlesPerSecond <= 0f) return;

        spawnAccumulator += particlesPerSecond * deltaSeconds;
        int countToSpawn = (int) Math.floor(spawnAccumulator);
        spawnAccumulator -= countToSpawn;

        for (int i = 0; i < countToSpawn; i++) {
            system.spawn(properties.spawn(position.x, position.y, position.z, random));
        }
    }

    public void burst(int count, ParticleSystem system) {
        if (properties == null || system == null || count <= 0) return;
        for (int i = 0; i < count; i++) {
            system.spawn(properties.spawn(position.x, position.y, position.z, random));
        }
    }
}
