package client_files.render.render.particle.effects;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.particle.ParticleProperties;
import org.joml.Vector3f;

public final class GlowParticle {
    private GlowParticle() {}

    public static ParticleProperties preset() {
        return new ParticleProperties(
                0.4f, 1.0f,
                0.1f, 0.3f,
                ColorRGBA.of(255, 240, 150, 255),
                ColorRGBA.of(255, 120, 50, 0),
                0f,
                new Vector3f(-0.1f, -0.1f, -0.1f),
                new Vector3f(0.1f, 0.1f, 0.1f)
        );
    }
}
