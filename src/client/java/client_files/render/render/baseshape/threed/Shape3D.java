package client_files.render.render.baseshape.threed;

import client_files.render.render.core.color.ColorRGBA;

public abstract class Shape3D {
    protected float x;
    protected float y;
    protected float z;
    protected ColorRGBA color;
    protected boolean throughWalls;

    public Shape3D(float x, float y, float z, ColorRGBA color, boolean throughWalls) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color != null ? color : ColorRGBA.WHITE;
        this.throughWalls = throughWalls;
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public float getZ() { return z; }
    public void setZ(float z) { this.z = z; }

    public ColorRGBA getColor() { return color; }
    public void setColor(ColorRGBA color) { this.color = color; }

    public boolean isThroughWalls() { return throughWalls; }
    public void setThroughWalls(boolean throughWalls) { this.throughWalls = throughWalls; }

    public abstract float[] localVertices();
}
