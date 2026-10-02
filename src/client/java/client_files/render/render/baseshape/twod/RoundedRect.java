package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;

public class RoundedRect extends Rect {
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;

    public RoundedRect(float x, float y, float width, float height, float cornerRadius, ColorRGBA color) {
        this(x, y, width, height, cornerRadius, cornerRadius, cornerRadius, cornerRadius, color);
    }

    public RoundedRect(float x, float y, float width, float height,
                       float radiusTopLeft, float radiusTopRight,
                       float radiusBottomRight, float radiusBottomLeft, ColorRGBA color) {
        super(x, y, width, height, color);
        this.radiusTopLeft = radiusTopLeft;
        this.radiusTopRight = radiusTopRight;
        this.radiusBottomRight = radiusBottomRight;
        this.radiusBottomLeft = radiusBottomLeft;
    }

    public float getRadiusTopLeft() { return radiusTopLeft; }
    public void setRadiusTopLeft(float radiusTopLeft) { this.radiusTopLeft = radiusTopLeft; }

    public float getRadiusTopRight() { return radiusTopRight; }
    public void setRadiusTopRight(float radiusTopRight) { this.radiusTopRight = radiusTopRight; }

    public float getRadiusBottomRight() { return radiusBottomRight; }
    public void setRadiusBottomRight(float radiusBottomRight) { this.radiusBottomRight = radiusBottomRight; }

    public float getRadiusBottomLeft() { return radiusBottomLeft; }
    public void setRadiusBottomLeft(float radiusBottomLeft) { this.radiusBottomLeft = radiusBottomLeft; }

    public void setUniformRadius(float radius) {
        this.radiusTopLeft = radius;
        this.radiusTopRight = radius;
        this.radiusBottomRight = radius;
        this.radiusBottomLeft = radius;
    }
}
