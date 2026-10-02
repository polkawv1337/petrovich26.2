package client_files.render.render.helper;

public final class MathHelper2D {
    private MathHelper2D() {}

    public static float[] rotate(float x, float y, float originX, float originY, float degrees) {
        if (Math.abs(degrees) < 0.0001f) {
            return new float[]{x, y};
        }
        double rad = Math.toRadians(degrees);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float dx = x - originX;
        float dy = y - originY;

        float rx = originX + (dx * cos - dy * sin);
        float ry = originY + (dx * sin + dy * cos);

        return new float[]{rx, ry};
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}
