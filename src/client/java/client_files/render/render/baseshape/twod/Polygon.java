package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class Polygon extends Shape2D {
    private float[] points;

    public Polygon(float x, float y, float[] points, ColorRGBA color) {
        super(x, y, color);
        this.points = points != null ? points : new float[0];
    }

    public float[] getPoints() { return points; }
    public void setPoints(float[] points) { this.points = points != null ? points : new float[0]; }

    @Override
    public float[] localVertices() {
        int numPoints = points.length / 2;
        if (numPoints < 3) return new float[0];

        int numTriangles = numPoints - 2;
        float[] fan = new float[numTriangles * 6];
        int idx = 0;

        float ox = points[0];
        float oy = points[1];

        for (int i = 1; i < numPoints - 1; i++) {
            fan[idx++] = ox;
            fan[idx++] = oy;
            fan[idx++] = points[i * 2];
            fan[idx++] = points[i * 2 + 1];
            fan[idx++] = points[i * 2 + 2];
            fan[idx++] = points[i * 2 + 3];
        }

        return fan;
    }
}
