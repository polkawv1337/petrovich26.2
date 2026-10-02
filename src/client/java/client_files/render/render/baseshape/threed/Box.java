package client_files.render.render.baseshape.threed;

import client_files.render.render.core.color.ColorRGBA;
import net.minecraft.world.phys.AABB;

public class Box extends Shape3D {
    private float width;
    private float height;
    private float depth;
    private boolean filled;

    public Box(float x, float y, float z, float width, float height, float depth, ColorRGBA color, boolean filled, boolean throughWalls) {
        super(x, y, z, color, throughWalls);
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.filled = filled;
    }

    public static Box fromEntityBounds(AABB bounds, ColorRGBA color) {
        return fromEntityBounds(bounds, color, true, false);
    }

    public static Box fromEntityBounds(AABB bounds, ColorRGBA color, boolean filled, boolean throughWalls) {
        float x = (float) bounds.minX;
        float y = (float) bounds.minY;
        float z = (float) bounds.minZ;
        float w = (float) (bounds.maxX - bounds.minX);
        float h = (float) (bounds.maxY - bounds.minY);
        float d = (float) (bounds.maxZ - bounds.minZ);
        return new Box(x, y, z, w, h, d, color, filled, throughWalls);
    }

    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getDepth() { return depth; }
    public void setDepth(float depth) { this.depth = depth; }

    public boolean isFilled() { return filled; }
    public void setFilled(boolean filled) { this.filled = filled; }

    @Override
    public float[] localVertices() {
        return new float[]{
                0f, 0f, 0f,
                width, 0f, 0f,
                width, height, 0f,
                0f, height, 0f,
                0f, 0f, depth,
                width, 0f, depth,
                width, height, depth,
                0f, height, depth
        };
    }
}
