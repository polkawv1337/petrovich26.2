package client_files.render.render.core;

import client_files.render.render.baseshape.threed.*;
import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.context.RenderContext;
import client_files.render.render.helper.MatrixUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Render3D {
    private static final Logger LOGGER = LoggerFactory.getLogger(Render3D.class);
    private static final Set<Class<?>> WARNED_SHAPES = ConcurrentHashMap.newKeySet();

    private Render3D() {}

    public static Vector2f worldToScreen(double worldX, double worldY, double worldZ) {
        return MatrixUtils.worldToScreen(worldX, worldY, worldZ);
    }

    public static void draw(Shape3D shape) {
        if (!RenderContext.is3DActive()) {
            throw new IllegalStateException("Cannot call Render3D.draw outside of an active 3D RenderContext");
        }
        if (shape == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.levelRenderer == null) return;

        try (var coll = mc.levelRenderer.collectPerFrameRenderThreadGizmos()) {
            if (shape instanceof Box box) {
                AABB aabb = new AABB(
                        box.getX(), box.getY(), box.getZ(),
                        box.getX() + box.getWidth(), box.getY() + box.getHeight(), box.getZ() + box.getDepth()
                );
                ColorRGBA col = box.getColor();
                int strokeColor = col.packed();
                GizmoStyle style;
                if (box.isFilled()) {
                    int fillAlpha = Math.round(col.a() * 0.35f);
                    int fillColor = (fillAlpha << 24) | (strokeColor & 0x00FFFFFF);
                    style = GizmoStyle.strokeAndFill(strokeColor, 2.0f, fillColor);
                } else {
                    style = GizmoStyle.stroke(strokeColor, 2.0f);
                }
                var prop = Gizmos.cuboid(aabb, style);
                if (box.isThroughWalls()) {
                    prop.setAlwaysOnTop();
                }
            } else if (shape instanceof Line3D line) {
                Vec3 start = new Vec3(line.getX(), line.getY(), line.getZ());
                float[] v = line.localVertices();
                Vec3 end = new Vec3(line.getX() + v[3], line.getY() + v[4], line.getZ() + v[5]);
                var prop = Gizmos.line(start, end, line.getColor().packed(), line.getThickness());
                if (line.isThroughWalls()) {
                    prop.setAlwaysOnTop();
                }
            } else if (shape instanceof Sphere sphere) {
                Vec3 center = new Vec3(sphere.getX(), sphere.getY(), sphere.getZ());
                GizmoStyle style = sphere.isFilled()
                        ? GizmoStyle.fill(sphere.getColor().packed())
                        : GizmoStyle.stroke(sphere.getColor().packed(), 2.0f);
                var prop = Gizmos.circle(center, sphere.getRadius(), style);
                if (sphere.isThroughWalls()) {
                    prop.setAlwaysOnTop();
                }
            } else if (shape instanceof Cylinder cylinder) {
                Vec3 bottom = new Vec3(cylinder.getX(), cylinder.getY(), cylinder.getZ());
                Vec3 top = new Vec3(cylinder.getX(), cylinder.getY() + cylinder.getHeight(), cylinder.getZ());
                GizmoStyle style = GizmoStyle.stroke(cylinder.getColor().packed(), 2.0f);
                var p1 = Gizmos.circle(bottom, cylinder.getRadius(), style);
                var p2 = Gizmos.circle(top, cylinder.getRadius(), style);
                if (cylinder.isThroughWalls()) {
                    p1.setAlwaysOnTop();
                    p2.setAlwaysOnTop();
                }
            } else {
                if (WARNED_SHAPES.add(shape.getClass())) {
                    LOGGER.warn("[Render] 3D shape '{}' has no active pipeline renderer and will not be displayed",
                            shape.getClass().getSimpleName());
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("[Render] Failed to draw 3D shape: {}", t.getMessage());
        }
    }

    public static void drawBox(float x, float y, float z, float width, float height, float depth, ColorRGBA color, boolean filled, boolean throughWalls) {
        draw(new Box(x, y, z, width, height, depth, color, filled, throughWalls));
    }

    public static void drawBox(Box box) {
        draw(box);
    }

    public static void drawLine3D(float x1, float y1, float z1, float x2, float y2, float z2, float thickness, ColorRGBA color, boolean throughWalls) {
        draw(new Line3D(x1, y1, z1, x2, y2, z2, thickness, color, throughWalls));
    }

    public static void drawSphere(float x, float y, float z, float radius, ColorRGBA color, boolean throughWalls) {
        draw(new Sphere(x, y, z, radius, color, throughWalls));
    }

    public static void drawCylinder(float x, float y, float z, float radius, float height, ColorRGBA color, boolean throughWalls) {
        draw(new Cylinder(x, y, z, radius, height, color, throughWalls));
    }

    public static void drawModel(Model model) {
        draw(model);
    }
}
