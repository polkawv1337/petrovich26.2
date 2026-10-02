package client_files.render.render.particle.effects;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.particle.ParticleProperties;
import org.joml.Vector3f;

public final class SparkParticle {
    private SparkParticle() {}

    public static ParticleProperties preset() {
        return new ParticleProperties(
                0.3f, 0.8f,
                0.05f, 0.15f,
                ColorRGBA.of(255, 200, 50, 255),
                ColorRGBA.of(255, 50, 0, 0),
                2.4f,
                new Vector3f(-2.0f, 1.0f, -2.0f),
                new Vector3f(2.0f, 4.0f, 2.0f)
        );
    }
}
