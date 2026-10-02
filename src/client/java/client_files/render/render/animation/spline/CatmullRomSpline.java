package client_files.render.render.animation.spline;

import java.util.List;

public class CatmullRomSpline extends Spline {
    public CatmullRomSpline(List<SplinePoint> controlPoints) {
        super(controlPoints);
    }

    @Override
    public SplinePoint pointAt(float t) {
        float clampedT = Math.clamp(t, 0f, 1f);
        int numPoints = controlPoints.size();
        if (numPoints == 1) return controlPoints.getFirst();
        if (numPoints == 2) {
            SplinePoint p0 = controlPoints.get(0);
            SplinePoint p1 = controlPoints.get(1);
            return new SplinePoint(
                    p0.x() + (p1.x() - p0.x()) * clampedT,
                    p0.y() + (p1.y() - p0.y()) * clampedT,
                    p0.z() + (p1.z() - p0.z()) * clampedT
            );
        }

        int numSegments = numPoints - 1;
        float scaledT = clampedT * numSegments;
        int segIndex = Math.min((int) scaledT, numSegments - 1);
        float localT = scaledT - segIndex;

        SplinePoint p0 = controlPoints.get(Math.max(0, segIndex - 1));
        SplinePoint p1 = controlPoints.get(segIndex);
        SplinePoint p2 = controlPoints.get(Math.min(numPoints - 1, segIndex + 1));
        SplinePoint p3 = controlPoints.get(Math.min(numPoints - 1, segIndex + 2));

        return new SplinePoint(
                evalCatmullRom(p0.x(), p1.x(), p2.x(), p3.x(), localT),
                evalCatmullRom(p0.y(), p1.y(), p2.y(), p3.y(), localT),
                evalCatmullRom(p0.z(), p1.z(), p2.z(), p3.z(), localT)
        );
    }

    private static float evalCatmullRom(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        return 0.5f * (
                (2f * p1) +
                (-p0 + p2) * t +
                (2f * p0 - 5f * p1 + 4f * p2 - p3) * t2 +
                (-p0 + 3f * p1 - 3f * p2 + p3) * t3
        );
    }
}
