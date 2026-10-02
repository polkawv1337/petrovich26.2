package client_files.render.render.particle;

import client_files.render.render.animation.AnimationTicker;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class ParticleSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParticleSystem.class);
    private static final ParticleSystem INSTANCE = new ParticleSystem();

    private final AnimationTicker ticker = new AnimationTicker();
    private final List<Particle> particles = new ArrayList<>();
    private final List<ParticleEmitter> emitters = new ArrayList<>();
    private int maxParticles = 512;
    private boolean maxLogged = false;

    private ParticleSystem() {}

    public static ParticleSystem get() {
        return INSTANCE;
    }

    public void setMaxParticles(int max) {
        this.maxParticles = Math.max(64, max);
    }

    public void spawn(Particle particle) {
        if (particle == null) return;
        if (particles.size() >= maxParticles) {
            if (!maxLogged) {
                LOGGER.warn("[Render] Max particle limit reached ({}), dropping spawns", maxParticles);
                maxLogged = true;
            }
            return;
        }
        particles.add(particle);
    }

    public List<Particle> getParticles() {
        return new ArrayList<>(particles);
    }

    public List<Particle> getParticlesDirect() {
        return particles;
    }

    public void addEmitter(ParticleEmitter emitter) {
        if (emitter != null && !emitters.contains(emitter)) {
            emitters.add(emitter);
        }
    }

    public void removeEmitter(ParticleEmitter emitter) {
        emitters.remove(emitter);
    }

    public void tick() {
        float dt = ticker.consumeDeltaSeconds();

        for (int i = 0; i < emitters.size(); i++) {
            emitters.get(i).tick(dt, this);
        }

        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).tick(dt, 9.8f);
        }

        particles.removeIf(p -> !p.isAlive());
    }

    public void render(GuiGraphicsExtractor extractor, DeltaTracker deltaTracker) {
        if (particles.isEmpty() || extractor == null) return;

        ParticleRenderer renderer = ParticleRenderer.get();
        for (int i = 0; i < particles.size(); i++) {
            renderer.renderParticle(particles.get(i), extractor);
        }
    }
}
