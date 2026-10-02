package client_files.render.render.baseshape.threed;

import client_files.render.render.core.color.ColorRGBA;

public class Sphere extends Shape3D {
    private float radius;
    private int latSegments;
    private int lonSegments;
    private boolean filled;

    public Sphere(float x, float y, float z, float radius, ColorRGBA color, boolean throughWalls) {
        this(x, y, z, radius, 16, 16, color, true, throughWalls);
    }

    public Sphere(float x, float y, float z, float radius, int latSegments, int lonSegments, ColorRGBA color, boolean throughWalls) {
        this(x, y, z, radius, latSegments, lonSegments, color, true, throughWalls);
    }

    public Sphere(float x, float y, float z, float radius, int latSegments, int lonSegments, ColorRGBA color, boolean filled, boolean throughWalls) {
        super(x, y, z, color, throughWalls);
        this.radius = radius;
        this.latSegments = Math.max(4, latSegments);
        this.lonSegments = Math.max(4, lonSegments);
        this.filled = filled;
    }

    public boolean isFilled() { return filled; }
    public void setFilled(boolean filled) { this.filled = filled; }

    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public int getLatSegments() { return latSegments; }
    public void setLatSegments(int latSegments) { this.latSegments = latSegments; }

    public int getLonSegments() { return lonSegments; }
    public void setLonSegments(int lonSegments) { this.lonSegments = lonSegments; }

    @Override
    public float[] localVertices() {
        int totalVertices = (latSegments + 1) * (lonSegments + 1);
        float[] vertices = new float[totalVertices * 3];
        int idx = 0;

        for (int i = 0; i <= latSegments; i++) {
            double theta = i * Math.PI / latSegments;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);

            for (int j = 0; j <= lonSegments; j++) {
                double phi = j * 2 * Math.PI / lonSegments;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);

                vertices[idx++] = (float) (radius * sinTheta * cosPhi);
                vertices[idx++] = (float) (radius * cosTheta);
                vertices[idx++] = (float) (radius * sinTheta * sinPhi);
            }
        }
        return vertices;
    }
}
