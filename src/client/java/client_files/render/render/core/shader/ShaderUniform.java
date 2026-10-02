package client_files.render.render.core.shader;

import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.Objects;

public final class ShaderUniform {
    public enum Type {
        FLOAT,
        VEC2,
        VEC3,
        VEC4,
        INT,
        MATRIX4F
    }

    private final String name;
    private final Type type;
    private float[] floatValues;
    private int intValue;
    private Matrix4f matrixValue;

    public ShaderUniform(String name, Type type) {
        this.name = Objects.requireNonNull(name, "uniform name cannot be null");
        this.type = Objects.requireNonNull(type, "uniform type cannot be null");
    }

    public String name() { return name; }
    public Type type() { return type; }

    public void set(float value) {
        this.floatValues = new float[]{value};
    }

    public void set(float v0, float v1) {
        this.floatValues = new float[]{v0, v1};
    }

    public void set(float v0, float v1, float v2) {
        this.floatValues = new float[]{v0, v1, v2};
    }

    public void set(float v0, float v1, float v2, float v3) {
        this.floatValues = new float[]{v0, v1, v2, v3};
    }

    public void set(int value) {
        this.intValue = value;
    }

    public void set(Matrix4f matrix) {
        this.matrixValue = matrix;
    }

    public float[] floatValues() { return floatValues; }
    public int intValue() { return intValue; }
    public Matrix4f matrixValue() { return matrixValue; }

    @Override
    public String toString() {
        return "ShaderUniform[name=" + name + ", type=" + type + "]";
    }
}
