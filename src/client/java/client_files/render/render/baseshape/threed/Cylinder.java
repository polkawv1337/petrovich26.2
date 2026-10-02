package client_files.render.render.baseshape.threed;

import client_files.render.render.core.color.ColorRGBA;

public class Cylinder extends Shape3D {
    private float radius;
    private float height;
    private int segments;

    public Cylinder(float x, float y, float z, float radius, float height, ColorRGBA color, boolean throughWalls) {
        this(x, y, z, radius, height, 16, color, throughWalls);
    }

    public Cylinder(float x, float y, float z, float radius, float height, int segments, ColorRGBA color, boolean throughWalls) {
        super(x, y, z, color, throughWalls);
        this.radius = radius;
        this.height = height;
        this.segments = Math.max(6, segments);
    }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public int getSegments() { return segments; }
    public void setSegments(int segments) { this.segments = Math.max(6, segments); }

    @Override
    public float[] localVertices() {

        float[] vertices = new float[segments * 2 * 3];
        double step = 2 * Math.PI / segments;
        int idx = 0;

        for (int i = 0; i < segments; i++) {
            double angle = i * step;
            float vx = (float) (Math.cos(angle) * radius);
            float vz = (float) (Math.sin(angle) * radius);

            vertices[idx++] = vx;
            vertices[idx++] = 0f;
            vertices[idx++] = vz;

            vertices[idx++] = vx;
            vertices[idx++] = height;
            vertices[idx++] = vz;
        }
        return vertices;
    }
}
