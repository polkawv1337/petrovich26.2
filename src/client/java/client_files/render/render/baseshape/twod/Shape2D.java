package client_files.render.render.baseshape.twod;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.gradient.Gradient;

public abstract class Shape2D {
    protected float x;
    protected float y;
    protected ColorRGBA color;
    protected Gradient gradient;
    protected float rotationDegrees;

    public Shape2D(float x, float y, ColorRGBA color) {
        this.x = x;
        this.y = y;
        this.color = color != null ? color : ColorRGBA.WHITE;
        this.rotationDegrees = 0f;
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }

    public float getY() { return y; }
    public void setY(float y) { this.y = y; }

    public ColorRGBA getColor() { return color; }
    public void setColor(ColorRGBA color) { this.color = color; }

    public Gradient getGradient() { return gradient; }
    public void setGradient(Gradient gradient) { this.gradient = gradient; }

    public float getRotationDegrees() { return rotationDegrees; }
    public void setRotationDegrees(float rotationDegrees) { this.rotationDegrees = rotationDegrees; }

    public abstract float[] localVertices();
}
