package client_files.render.render.particle;

import client_files.render.render.core.Render3D;
import client_files.render.render.core.color.ColorRGBA;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;

public final class ParticleRenderer {
    private static final ParticleRenderer INSTANCE = new ParticleRenderer();

    public static final Identifier SPARK_TEX =
            Identifier.fromNamespaceAndPath("petrovich_26_2", "testassets/texture/spark.png");
    public static final Identifier BLOOM_TEX =
            Identifier.fromNamespaceAndPath("petrovich_26_2", "testassets/texture/bloom.png");

    private ParticleRenderer() {}

    public static ParticleRenderer get() {
        return INSTANCE;
    }

    public void renderParticle(Particle particle, GuiGraphicsExtractor extractor) {
        if (particle == null || !particle.isAlive() || extractor == null) return;

        Vector2f screenPos = Render3D.worldToScreen(particle.getX(), particle.getY(), particle.getZ());
        if (screenPos == null) return;

        Identifier tex = particle.getTexture();
        if (tex == null) {
            tex = SPARK_TEX;
        }

        int texW = 1000;
        int texH = 1000;
        if (tex.getPath().contains("bloom")) {
            texW = 64;
            texH = 64;
        }

        float rad = Math.max(2.0f, particle.getSize() * 18.0f);
        int d = Math.max(1, Math.round(rad * 2.0f));
        int ix = Math.round(screenPos.x - rad);
        int iy = Math.round(screenPos.y - rad);
        ColorRGBA col = particle.getColor();

        extractor.blit(
                RenderPipelines.GUI_TEXTURED,
                tex,
                ix, iy,
                0.0f, 0.0f,
                d, d,
                texW, texH,
                texW, texH,
                col.packed()
        );
    }
}
