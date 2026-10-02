package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class Circle extends Shape2D {
    private float radius;
    private int segments;

    public Circle(float x, float y, float radius, ColorRGBA color) {
        this(x, y, radius, 32, color);
    }

    public Circle(float x, float y, float radius, int segments, ColorRGBA color) {
        super(x, y, color);
        this.radius = radius;
        this.segments = Math.max(8, segments);
    }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public int getSegments() { return segments; }
    public void setSegments(int segments) { this.segments = Math.max(8, segments); }

    @Override
    public float[] localVertices() {
        float[] vertices = new float[segments * 2];
        double angleStep = (2 * Math.PI) / segments;
        for (int i = 0; i < segments; i++) {
            double angle = i * angleStep;
            vertices[i * 2] = (float) (Math.cos(angle) * radius);
            vertices[i * 2 + 1] = (float) (Math.sin(angle) * radius);
        }
        return vertices;
    }
}
