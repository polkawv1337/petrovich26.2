package client_files.render.render.animation.spline;

import java.util.Collections;
import java.util.List;

public abstract class Spline {
    protected final List<SplinePoint> controlPoints;

    public Spline(List<SplinePoint> controlPoints) {
        if (controlPoints == null || controlPoints.isEmpty()) {
            throw new IllegalArgumentException("Spline requires at least one control point");
        }
        this.controlPoints = Collections.unmodifiableList(controlPoints);
    }

    public List<SplinePoint> getControlPoints() {
        return controlPoints;
    }

    public abstract SplinePoint pointAt(float t);
}
