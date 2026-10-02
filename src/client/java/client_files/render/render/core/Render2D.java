package client_files.render.render.core;

import client_files.render.render.baseshape.twod.*;
import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.gradient.Gradient;
import client_files.render.render.core.context.RenderContext;
import client_files.render.render.core.font.CustomFont;
import client_files.render.render.core.font.FontRenderer;
import client_files.render.render.core.font.TextAlign;
import client_files.render.render.core.font.TextStyle;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Render2D {
    private static final Logger LOGGER = LoggerFactory.getLogger(Render2D.class);
    private static final Set<Class<?>> WARNED_SHAPES = ConcurrentHashMap.newKeySet();
    private static boolean warnedQuad = false;
    private static boolean debugMode = false;

    private Render2D() {}

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void draw(Shape2D shape) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.draw outside of an active 2D RenderContext");
        }
        if (shape == null) return;

        float ox = shape.getX();
        float oy = shape.getY();

        if (shape instanceof RoundedRect rr) {
            drawRoundedRect(ox, oy, rr.getWidth(), rr.getHeight(),
                    rr.getRadiusTopLeft(), rr.getRadiusTopRight(),
                    rr.getRadiusBottomRight(), rr.getRadiusBottomLeft(),
                    shape.getColor());
            return;
        }

        if (shape instanceof Rect rect) {
            drawRect(ox, oy, rect.getWidth(), rect.getHeight(), shape.getColor());
            return;
        }

        if (WARNED_SHAPES.add(shape.getClass())) {
            LOGGER.warn("[Render] 2D shape '{}' has no immediate-mode renderer and will not be displayed until custom RenderPipeline batching is wired",
                    shape.getClass().getSimpleName());
        }
    }

    public static void drawRect(float x, float y, float width, float height, ColorRGBA color) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawRect outside of an active 2D RenderContext");
        }
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null && color != null) {
            int ix = Math.round(x);
            int iy = Math.round(y);
            int iw = Math.round(width);
            int ih = Math.round(height);
            extractor.fill(ix, iy, ix + iw, iy + ih, color.packed());
        }
    }

    public static void drawRect(float x, float y, float width, float height, Gradient gradient) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawRect outside of an active 2D RenderContext");
        }
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null && gradient != null) {
            ColorRGBA c1 = gradient.colorAt(0.0f);
            ColorRGBA c2 = gradient.colorAt(1.0f);
            int ix = Math.round(x);
            int iy = Math.round(y);
            int iw = Math.round(width);
            int ih = Math.round(height);
            extractor.fillGradient(ix, iy, ix + iw, iy + ih, c1.packed(), c2.packed());
        }
    }

    public static void drawGradientRect(float x, float y, float width, float height, Gradient gradient) {
        drawRect(x, y, width, height, gradient);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, ColorRGBA color) {
        drawRoundedRect(x, y, width, height, radius, radius, radius, radius, color);
    }

    public static void drawRoundedRect(float x, float y, float width, float height,
                                       float tl, float tr, float br, float bl, ColorRGBA color) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawRoundedRect outside of an active 2D RenderContext");
        }
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null && color != null) {
            SmoothRender.get().drawRoundedRect(extractor, x, y, width, height, tl, tr, br, bl, color);
        }
    }

    public static void drawCircle(float x, float y, float radius, ColorRGBA color) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawCircle outside of an active 2D RenderContext");
        }
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null && color != null) {
            SmoothRender.get().drawCircle(extractor, x, y, radius, color);
        }
    }

    public static void drawRing(float x, float y, float outerRadius, float innerRadius,
                                ColorRGBA outerColor, ColorRGBA innerColor) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawRing outside of an active 2D RenderContext");
        }
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null) {
            SmoothRender.get().drawRing(extractor, x, y, outerRadius, innerRadius, outerColor, innerColor);
        }
    }

    public static void drawEllipse(float x, float y, float rx, float ry, ColorRGBA color) {
        draw(new Ellipse(x, y, rx, ry, color));
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float thickness, ColorRGBA color) {
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null && color != null) {
            int t = Math.max(1, Math.round(thickness));
            int col = color.packed();
            if (Math.abs(y1 - y2) < 0.01f) {

                int minX = Math.round(Math.min(x1, x2));
                int maxX = Math.round(Math.max(x1, x2));
                int minY = Math.round(y1 - t / 2.0f);
                extractor.fill(minX, minY, maxX, minY + t, col);
            } else if (Math.abs(x1 - x2) < 0.01f) {

                int minX = Math.round(x1 - t / 2.0f);
                int minY = Math.round(Math.min(y1, y2));
                int maxY = Math.round(Math.max(y1, y2));
                extractor.fill(minX, minY, minX + t, maxY, col);
            } else {

                float dx = x2 - x1;
                float dy = y2 - y1;
                float length = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = (float) Math.atan2(dy, dx);
                float midX = (x1 + x2) * 0.5f;
                float midY = (y1 + y2) * 0.5f;

                extractor.pose().pushMatrix();
                extractor.pose().translate(midX, midY);
                extractor.pose().rotate(angle);
                int halfLen = Math.round(length * 0.5f);
                int halfT = Math.max(1, t / 2);
                extractor.fill(-halfLen, -halfT, halfLen, halfT, col);
                extractor.pose().popMatrix();
            }
        }
    }

    public static void drawTexture(Identifier texture, float x, float y, float width, float height) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawTexture outside of an active 2D RenderContext");
        }
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor != null && texture != null) {
            extractor.blit(texture, Math.round(x), Math.round(y), Math.round(width), Math.round(height), 0.0f, 0.0f, width, height);
        }
    }

    public static void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, ColorRGBA color) {
        draw(new Triangle(x1, y1, 0, 0, x2 - x1, y2 - y1, x3 - x1, y3 - y1, color));
    }

    public static void drawPolygon(float[] points, ColorRGBA color) {
        if (points == null || points.length < 2) return;
        draw(new Polygon(points[0], points[1], points, color));
    }

    public static void drawQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, ColorRGBA color) {
        if (!RenderContext.is2DActive()) {
            throw new IllegalStateException("Cannot call Render2D.drawQuad outside of an active 2D RenderContext");
        }
        if (!warnedQuad) {
            warnedQuad = true;
            LOGGER.warn("[Render] Render2D.drawQuad has no immediate-mode renderer and will not be displayed until custom RenderPipeline batching is wired");
        }
    }

    public static void drawText(String text, float x, float y, float size, ColorRGBA color) {
        FontRenderer.get().drawString(null, text, x, y, size, color, TextStyle.DEFAULT, TextAlign.LEFT);
    }

    public static void drawText(CustomFont font, String text, float x, float y, float size, ColorRGBA color, TextStyle style, TextAlign align) {
        FontRenderer.get().drawString(font, text, x, y, size, color, style, align);
    }
}
