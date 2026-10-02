package client_files.render.render.animation.spline;

import java.util.List;

public class BezierSpline extends Spline {
    public BezierSpline(List<SplinePoint> controlPoints) {
        super(controlPoints);
    }

    @Override
    public SplinePoint pointAt(float t) {
        float clampedT = Math.clamp(t, 0f, 1f);
        int n = controlPoints.size();
        if (n == 1) return controlPoints.getFirst();

        float[] x = new float[n];
        float[] y = new float[n];
        float[] z = new float[n];

        for (int i = 0; i < n; i++) {
            SplinePoint p = controlPoints.get(i);
            x[i] = p.x();
            y[i] = p.y();
            z[i] = p.z();
        }

        for (int r = 1; r < n; r++) {
            for (int i = 0; i < n - r; i++) {
                x[i] = (1f - clampedT) * x[i] + clampedT * x[i + 1];
                y[i] = (1f - clampedT) * y[i] + clampedT * y[i + 1];
                z[i] = (1f - clampedT) * z[i] + clampedT * z[i + 1];
            }
        }

        return new SplinePoint(x[0], y[0], z[0]);
    }
}
