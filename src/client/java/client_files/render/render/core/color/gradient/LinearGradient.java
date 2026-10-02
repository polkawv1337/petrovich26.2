package client_files.render.render.core.color.gradient;

import client_files.render.render.core.color.ColorRGBA;

import java.util.List;

public class LinearGradient extends Gradient {
    private final float angleDegrees;

    public LinearGradient(List<ColorStop> stops, float angleDegrees) {
        super(stops);
        this.angleDegrees = angleDegrees;
    }

    public static LinearGradient horizontal(ColorRGBA start, ColorRGBA end) {
        return new LinearGradient(List.of(ColorStop.of(0f, start), ColorStop.of(1f, end)), 0f);
    }

    public static LinearGradient vertical(ColorRGBA start, ColorRGBA end) {
        return new LinearGradient(List.of(ColorStop.of(0f, start), ColorStop.of(1f, end)), 90f);
    }

    public float getAngleDegrees() {
        return angleDegrees;
    }

    @Override
    public ColorRGBA colorAt(float t) {
        return interpolateBetweenStops(t);
    }

    public float projectPointToT(float x, float y, float width, float height) {
        if (width <= 0.0001f && height <= 0.0001f) return 0f;
        double rad = Math.toRadians(angleDegrees);
        float dirX = (float) Math.cos(rad);
        float dirY = (float) Math.sin(rad);

        float nx = (width > 0.0001f) ? (x / width) - 0.5f : 0f;
        float ny = (height > 0.0001f) ? (y / height) - 0.5f : 0f;

        float projection = nx * dirX + ny * dirY;
        return Math.clamp(projection + 0.5f, 0f, 1f);
    }
}
