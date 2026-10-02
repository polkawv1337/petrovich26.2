package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class Line2D extends Shape2D {
    private float x2;
    private float y2;
    private float thickness;

    public Line2D(float x1, float y1, float x2, float y2, float thickness, ColorRGBA color) {
        super(x1, y1, color);
        this.x2 = x2;
        this.y2 = y2;
        this.thickness = Math.max(0.1f, thickness);
    }

    public float getX1() { return x; }
    public void setX1(float x1) { this.x = x1; }

    public float getY1() { return y; }
    public void setY1(float y1) { this.y = y1; }

    public float getX2() { return x2; }
    public void setX2(float x2) { this.x2 = x2; }

    public float getY2() { return y2; }
    public void setY2(float y2) { this.y2 = y2; }

    public float getThickness() { return thickness; }
    public void setThickness(float thickness) { this.thickness = thickness; }

    @Override
    public float[] localVertices() {
        float dx = x2 - x;
        float dy = y2 - y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) {
            return new float[]{0, 0, 0, 0, 0, 0, 0, 0};
        }
        float nx = -dy / len * (thickness * 0.5f);
        float ny = dx / len * (thickness * 0.5f);

        return new float[]{
                nx, ny,
                dx + nx, dy + ny,
                dx - nx, dy - ny,
                -nx, -ny
        };
    }
}
