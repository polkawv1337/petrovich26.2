package client_files.render.api;

import client_files.render.render.particle.Particle;
import client_files.render.render.particle.ParticleEmitter;
import client_files.render.render.particle.ParticleSystem;
import client_files.render.render.particle.effects.GlowParticle;
import client_files.render.render.particle.effects.SmokeParticle;
import client_files.render.render.particle.effects.SparkParticle;

import java.util.Random;

public final class ParticleApi {
    private final Random random = new Random();

    ParticleApi() {}

    public void spawn(Particle particle) {
        ParticleSystem.get().spawn(particle);
    }

    public void addEmitter(ParticleEmitter emitter) {
        ParticleSystem.get().addEmitter(emitter);
    }

    public void removeEmitter(ParticleEmitter emitter) {
        ParticleSystem.get().removeEmitter(emitter);
    }

    public void smokeAt(float x, float y, float z) {
        spawn(SmokeParticle.preset().spawn(x, y, z, random));
    }

    public void glowAt(float x, float y, float z) {
        spawn(GlowParticle.preset().spawn(x, y, z, random));
    }

    public void sparkBurst(float x, float y, float z, int count) {
        for (int i = 0; i < count; i++) {
            spawn(SparkParticle.preset().spawn(x, y, z, random));
        }
    }
}
