package client_files.render.render.animation.threed;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.animation.spline.Spline;
import client_files.render.render.animation.spline.SplinePoint;
import client_files.render.render.baseshape.threed.Shape3D;

import java.util.Objects;

public class PathAnimation3D extends Animation3D {
    private final Spline spline;
    private final AnimationCurve curve;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public PathAnimation3D(Shape3D target, Spline spline, AnimationCurve curve) {
        super(target);
        this.spline = Objects.requireNonNull(spline, "spline cannot be null");
        this.curve = Objects.requireNonNull(curve, "curve cannot be null");
    }

    @Override
    public void tick(float deltaSeconds) {
        if (state == AnimationState.FINISHED || state == AnimationState.PAUSED) return;
        if (state == AnimationState.PENDING) {
            state = AnimationState.RUNNING;
            notifyStart();
        }

        elapsed += deltaSeconds;
        float duration = curve.getDurationSeconds();
        float rawProgress = Math.clamp(elapsed / duration, 0f, 1f);
        float eased = curve.getInterpolator().apply(rawProgress);

        SplinePoint p = spline.pointAt(eased);
        if (target != null) {
            target.setX(p.x());
            target.setY(p.y());
            target.setZ(p.z());
        }

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
            SplinePoint end = spline.pointAt(1f);
            if (target != null) {
                target.setX(end.x());
                target.setY(end.y());
                target.setZ(end.z());
            }
            state = AnimationState.FINISHED;
            notifyFinish();
        }
    }

    @Override
    public AnimationState state() { return state; }
    @Override
    public void pause() { if (state == AnimationState.RUNNING) state = AnimationState.PAUSED; }
    @Override
    public void resume() { if (state == AnimationState.PAUSED) state = AnimationState.RUNNING; }
    @Override
    public void cancel() { state = AnimationState.FINISHED; notifyFinish(); }
}
