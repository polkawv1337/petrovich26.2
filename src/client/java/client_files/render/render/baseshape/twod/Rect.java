package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class Rect extends Shape2D {
    protected float width;
    protected float height;

    public Rect(float x, float y, float width, float height, ColorRGBA color) {
        super(x, y, color);
        this.width = width;
        this.height = height;
    }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    @Override
    public float[] localVertices() {
        return new float[]{
                0f, 0f,
                width, 0f,
                width, height,
                0f, height
        };
    }
}
