package client_files.render.render.baseshape.threed;

import client_files.render.render.core.color.ColorRGBA;

public class Line3D extends Shape3D {
    private float x2, y2, z2;
    private float thickness;

    public Line3D(float x1, float y1, float z1, float x2, float y2, float z2, float thickness, ColorRGBA color, boolean throughWalls) {
        super(x1, y1, z1, color, throughWalls);
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.thickness = thickness;
    }

    public float getX1() { return x; }
    public void setX1(float x1) { this.x = x1; }

    public float getY1() { return y; }
    public void setY1(float y1) { this.y = y1; }

    public float getZ1() { return z; }
    public void setZ1(float z1) { this.z = z1; }

    public float getX2() { return x2; }
    public void setX2(float x2) { this.x2 = x2; }

    public float getY2() { return y2; }
    public void setY2(float y2) { this.y2 = y2; }

    public float getZ2() { return z2; }
    public void setZ2(float z2) { this.z2 = z2; }

    public float getThickness() { return thickness; }
    public void setThickness(float thickness) { this.thickness = thickness; }

    @Override
    public float[] localVertices() {
        return new float[]{
                0f, 0f, 0f,
                x2 - x, y2 - y, z2 - z
        };
    }
}
