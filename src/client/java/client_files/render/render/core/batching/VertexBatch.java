package client_files.render.render.core.batching;

import java.util.Arrays;

public final class VertexBatch {
    private static final int INITIAL_CAPACITY = 256;

    private float[] vertexData;

    private int[] colorData;
    private int vertexCount = 0;

    public VertexBatch() {
        this(INITIAL_CAPACITY);
    }

    public VertexBatch(int initialCapacity) {
        this.vertexData = new float[initialCapacity * 4];
        this.colorData = new int[initialCapacity];
    }

    public void reset() {
        vertexCount = 0;
    }

    public void pushVertex(float x, float y, float u, float v, int packedColor) {
        ensureCapacity(vertexCount + 1);

        int dataIndex = vertexCount * 4;
        vertexData[dataIndex] = x;
        vertexData[dataIndex + 1] = y;
        vertexData[dataIndex + 2] = u;
        vertexData[dataIndex + 3] = v;

        colorData[vertexCount] = packedColor;
        vertexCount++;
    }

    public int vertexCount() {
        return vertexCount;
    }

    public float[] vertexData() {
        return vertexData;
    }

    public int[] colorData() {
        return colorData;
    }

    private void ensureCapacity(int minVertices) {
        if (minVertices <= colorData.length) return;

        int newCapacity = Math.max(colorData.length * 2, minVertices);
        vertexData = Arrays.copyOf(vertexData, newCapacity * 4);
        colorData = Arrays.copyOf(colorData, newCapacity);
    }
}
