package client_files.render.render.baseshape.threed;

import client_files.render.render.core.color.ColorRGBA;

public class Model extends Shape3D {
    private float[] vertexPositions;
    private float[] normals;
    private int[] indices;

    public Model(float x, float y, float z, float[] vertexPositions, float[] normals, int[] indices, ColorRGBA color, boolean throughWalls) {
        super(x, y, z, color, throughWalls);
        this.vertexPositions = vertexPositions != null ? vertexPositions : new float[0];
        this.normals = normals != null ? normals : new float[0];
        this.indices = indices != null ? indices : new int[0];
    }

    public float[] getVertexPositions() { return vertexPositions; }
    public void setVertexPositions(float[] vertexPositions) { this.vertexPositions = vertexPositions; }

    public float[] getNormals() { return normals; }
    public void setNormals(float[] normals) { this.normals = normals; }

    public int[] getIndices() { return indices; }
    public void setIndices(int[] indices) { this.indices = indices; }

    @Override
    public float[] localVertices() {
        return vertexPositions;
    }
}
