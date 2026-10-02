package client_files.render.render.animation.spline;

public record SplinePoint(float x, float y, float z) {
    public static SplinePoint of(float x, float y, float z) {
        return new SplinePoint(x, y, z);
    }

    public static SplinePoint of2D(float x, float y) {
        return new SplinePoint(x, y, 0f);
    }
}
