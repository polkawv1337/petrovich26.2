package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class Ellipse extends Shape2D {
    private float radiusX;
    private float radiusY;
    private int segments;

    public Ellipse(float x, float y, float radiusX, float radiusY, ColorRGBA color) {
        this(x, y, radiusX, radiusY, 32, color);
    }

    public Ellipse(float x, float y, float radiusX, float radiusY, int segments, ColorRGBA color) {
        super(x, y, color);
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.segments = Math.max(8, segments);
    }

    public float getRadiusX() { return radiusX; }
    public void setRadiusX(float radiusX) { this.radiusX = radiusX; }

    public float getRadiusY() { return radiusY; }
    public void setRadiusY(float radiusY) { this.radiusY = radiusY; }

    public int getSegments() { return segments; }
    public void setSegments(int segments) { this.segments = Math.max(8, segments); }

    @Override
    public float[] localVertices() {
        float[] vertices = new float[segments * 2];
        double angleStep = (2 * Math.PI) / segments;
        for (int i = 0; i < segments; i++) {
            double angle = i * angleStep;
            vertices[i * 2] = (float) (Math.cos(angle) * radiusX);
            vertices[i * 2 + 1] = (float) (Math.sin(angle) * radiusY);
        }
        return vertices;
    }
}
