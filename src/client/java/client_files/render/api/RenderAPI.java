package client_files.render.api;

import client_files.render.render.baseshape.twod.Shape2D;
import client_files.render.render.baseshape.threed.Box;
import client_files.render.render.baseshape.threed.Model;
import client_files.render.render.baseshape.threed.Shape3D;
import client_files.render.render.core.Render2D;
import client_files.render.render.core.Render3D;
import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.gradient.Gradient;
import client_files.render.render.core.font.CustomFont;
import client_files.render.render.core.font.TextAlign;
import client_files.render.render.core.font.TextStyle;
import client_files.render.render.particle.ParticleSystem;
import org.joml.Vector2f;

public final class RenderAPI {
    private static RenderAPI instance;

    private final RenderConfig config;
    private final AssetApi assets = new AssetApi();
    private final ParticleApi particles = new ParticleApi();
    private final AnimationApi animations = new AnimationApi();
    private final EventApi events = new EventApi();

    private RenderAPI(RenderConfig config) {
        this.config = config;
        ParticleSystem.get().setMaxParticles(config.maxParticles());
        Render2D.setDebugMode(config.debugMode());
    }

    public static synchronized void init(RenderConfig config) {
        if (instance != null) return;
        instance = new RenderAPI(config == null ? RenderConfig.DEFAULT : config);
    }

    public static synchronized void shutdown() {
        instance = null;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static RenderAPI get() {
        RenderAPI api = instance;
        if (api == null) throw new ApiNotInitializedException();
        return api;
    }

    public RenderConfig config() {
        return config;
    }

    public AssetApi assets() {
        return assets;
    }

    public ParticleApi particles() {
        return particles;
    }

    public AnimationApi animations() {
        return animations;
    }

    public EventApi events() {
        return events;
    }

    public void draw(Shape2D shape) { Render2D.draw(shape); }
    public void drawRect(float x, float y, float w, float h, ColorRGBA color) { Render2D.drawRect(x, y, w, h, color); }
    public void drawRect(float x, float y, float w, float h, Gradient gradient) { Render2D.drawRect(x, y, w, h, gradient); }
    public void drawRoundedRect(float x, float y, float w, float h, float radius, ColorRGBA color) { Render2D.drawRoundedRect(x, y, w, h, radius, color); }
    public void drawRoundedRect(float x, float y, float w, float h, float tl, float tr, float br, float bl, ColorRGBA color) { Render2D.drawRoundedRect(x, y, w, h, tl, tr, br, bl, color); }
    public void drawCircle(float x, float y, float radius, ColorRGBA color) { Render2D.drawCircle(x, y, radius, color); }
    public void drawRing(float x, float y, float outerRadius, float innerRadius, ColorRGBA outerColor, ColorRGBA innerColor) { Render2D.drawRing(x, y, outerRadius, innerRadius, outerColor, innerColor); }
    public void drawEllipse(float x, float y, float rx, float ry, ColorRGBA color) { Render2D.drawEllipse(x, y, rx, ry, color); }
    public void drawLine(float x1, float y1, float x2, float y2, float thickness, ColorRGBA color) { Render2D.drawLine(x1, y1, x2, y2, thickness, color); }
    public void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, ColorRGBA color) { Render2D.drawTriangle(x1, y1, x2, y2, x3, y3, color); }
    public void drawPolygon(float[] points, ColorRGBA color) { Render2D.drawPolygon(points, color); }
    public void drawQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, ColorRGBA color) { Render2D.drawQuad(x0, y0, x1, y1, x2, y2, x3, y3, color); }
    public void drawText(String text, float x, float y, float size, ColorRGBA color) { Render2D.drawText(text, x, y, size, color); }
    public void drawText(CustomFont font, String text, float x, float y, float size, ColorRGBA color, TextStyle style, TextAlign align) { Render2D.drawText(font, text, x, y, size, color, style, align); }

    public void draw(Shape3D shape) { Render3D.draw(shape); }
    public void drawBox(float x, float y, float z, float w, float h, float d, ColorRGBA color, boolean filled, boolean throughWalls) { Render3D.drawBox(x, y, z, w, h, d, color, filled, throughWalls); }
    public void drawBox(Box box) { Render3D.drawBox(box); }
    public void drawLine3D(float x1, float y1, float z1, float x2, float y2, float z2, float thickness, ColorRGBA color, boolean throughWalls) { Render3D.drawLine3D(x1, y1, z1, x2, y2, z2, thickness, color, throughWalls); }
    public void drawSphere(float x, float y, float z, float radius, ColorRGBA color, boolean throughWalls) { Render3D.drawSphere(x, y, z, radius, color, throughWalls); }
    public void drawCylinder(float x, float y, float z, float radius, float height, ColorRGBA color, boolean throughWalls) { Render3D.drawCylinder(x, y, z, radius, height, color, throughWalls); }
    public void drawModel(Model model) { Render3D.drawModel(model); }
    public Vector2f worldToScreen(double worldX, double worldY, double worldZ) { return Render3D.worldToScreen(worldX, worldY, worldZ); }
}