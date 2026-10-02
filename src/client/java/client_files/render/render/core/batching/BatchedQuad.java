package client_files.render.render.core.batching;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.gradient.Gradient;
import client_files.render.render.core.color.gradient.LinearGradient;
import client_files.render.render.core.color.gradient.RadialGradient;

public final class BatchedQuad {
    private final float[] positions = new float[8];
    private final float[] uvs = new float[8];
    private final int[] colors = new int[4];

    public BatchedQuad() {
        setUVs(0f, 0f, 1f, 0f, 1f, 1f, 0f, 1f);
        setColor(ColorRGBA.WHITE);
    }

    public BatchedQuad setPositions(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
        positions[0] = x0; positions[1] = y0;
        positions[2] = x1; positions[3] = y1;
        positions[4] = x2; positions[5] = y2;
        positions[6] = x3; positions[7] = y3;
        return this;
    }

    public BatchedQuad setRect(float x, float y, float width, float height) {
        return setPositions(x, y, x + width, y, x + width, y + height, x, y + height);
    }

    public BatchedQuad setUVs(float u0, float v0, float u1, float v1, float u2, float v2, float u3, float v3) {
        uvs[0] = u0; uvs[1] = v0;
        uvs[2] = u1; uvs[3] = v1;
        uvs[4] = u2; uvs[5] = v2;
        uvs[6] = u3; uvs[7] = v3;
        return this;
    }

    public BatchedQuad setColor(ColorRGBA color) {
        int packed = color != null ? color.packed() : 0xFFFFFFFF;
        colors[0] = packed;
        colors[1] = packed;
        colors[2] = packed;
        colors[3] = packed;
        return this;
    }

    public BatchedQuad setColors(ColorRGBA c0, ColorRGBA c1, ColorRGBA c2, ColorRGBA c3) {
        colors[0] = c0.packed();
        colors[1] = c1.packed();
        colors[2] = c2.packed();
        colors[3] = c3.packed();
        return this;
    }

    public BatchedQuad setGradient(Gradient gradient, float width, float height) {
        if (gradient instanceof LinearGradient lg) {
            colors[0] = gradient.colorAt(lg.projectPointToT(0, 0, width, height)).packed();
            colors[1] = gradient.colorAt(lg.projectPointToT(width, 0, width, height)).packed();
            colors[2] = gradient.colorAt(lg.projectPointToT(width, height, width, height)).packed();
            colors[3] = gradient.colorAt(lg.projectPointToT(0, height, width, height)).packed();
        } else if (gradient instanceof RadialGradient rg) {
            colors[0] = gradient.colorAt(rg.projectPointToT(0f, 0f)).packed();
            colors[1] = gradient.colorAt(rg.projectPointToT(1f, 0f)).packed();
            colors[2] = gradient.colorAt(rg.projectPointToT(1f, 1f)).packed();
            colors[3] = gradient.colorAt(rg.projectPointToT(0f, 1f)).packed();
        } else if (gradient != null) {
            colors[0] = gradient.colorAt(0f).packed();
            colors[1] = gradient.colorAt(0.33f).packed();
            colors[2] = gradient.colorAt(0.66f).packed();
            colors[3] = gradient.colorAt(1f).packed();
        }
        return this;
    }

    public void pushTo(VertexBatch batch) {

        batch.pushVertex(positions[0], positions[1], uvs[0], uvs[1], colors[0]);
        batch.pushVertex(positions[2], positions[3], uvs[2], uvs[3], colors[1]);
        batch.pushVertex(positions[4], positions[5], uvs[4], uvs[5], colors[2]);

        batch.pushVertex(positions[0], positions[0 + 1], uvs[0], uvs[1], colors[0]);
        batch.pushVertex(positions[4], positions[5], uvs[4], uvs[5], colors[2]);
        batch.pushVertex(positions[6], positions[7], uvs[6], uvs[7], colors[3]);
    }
}
