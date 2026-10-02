package client_files.render.render.particle.effects;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.particle.ParticleProperties;
import org.joml.Vector3f;

public final class SmokeParticle {
    private SmokeParticle() {}

    public static ParticleProperties preset() {
        return new ParticleProperties(
                1.5f, 3.0f,
                0.2f, 0.6f,
                ColorRGBA.of(180, 180, 180, 180),
                ColorRGBA.of(120, 120, 120, 0),
                -0.2f,
                new Vector3f(-0.2f, 0.3f, -0.2f),
                new Vector3f(0.2f, 0.8f, 0.2f)
        );
    }
}
