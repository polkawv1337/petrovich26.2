package client_files.render.render.core.color.gradient;

import client_files.render.render.core.color.ColorRGBA;

import java.util.List;

public class RadialGradient extends Gradient {
    private final float centerX;
    private final float centerY;
    private final float radius;

    public RadialGradient(List<ColorStop> stops, float centerX, float centerY, float radius) {
        super(stops);
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = Math.max(0.0001f, radius);
    }

    public static RadialGradient simple(ColorRGBA centerColor, ColorRGBA edgeColor) {
        return new RadialGradient(List.of(ColorStop.of(0f, centerColor), ColorStop.of(1f, edgeColor)), 0.5f, 0.5f, 0.5f);
    }

    public float getCenterX() { return centerX; }
    public float getCenterY() { return centerY; }
    public float getRadius() { return radius; }

    @Override
    public ColorRGBA colorAt(float t) {
        return interpolateBetweenStops(t);
    }

    public float projectPointToT(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return Math.clamp(dist / radius, 0f, 1f);
    }
}
