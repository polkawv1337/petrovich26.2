package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class Triangle extends Shape2D {
    private float x1, y1;
    private float x2, y2;
    private float x3, y3;

    public Triangle(float x, float y, float x1, float y1, float x2, float y2, float x3, float y3, ColorRGBA color) {
        super(x, y, color);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
        this.x3 = x3; this.y3 = y3;
    }

    public float getX1() { return x1; }
    public void setX1(float x1) { this.x1 = x1; }
    public float getY1() { return y1; }
    public void setY1(float y1) { this.y1 = y1; }

    public float getX2() { return x2; }
    public void setX2(float x2) { this.x2 = x2; }
    public float getY2() { return y2; }
    public void setY2(float y2) { this.y2 = y2; }

    public float getX3() { return x3; }
    public void setX3(float x3) { this.x3 = x3; }
    public float getY3() { return y3; }
    public void setY3(float y3) { this.y3 = y3; }

    @Override
    public float[] localVertices() {
        return new float[]{
                x1, y1,
                x2, y2,
                x3, y3
        };
    }
}
