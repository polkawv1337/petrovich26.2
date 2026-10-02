package client_files.render.render.animation.threed;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.animation.spline.Spline;
import client_files.render.render.animation.spline.SplinePoint;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CameraAnimation extends Animation {
    private final Vector3f position = new Vector3f();
    private final Quaternionf orientation = new Quaternionf();
    private final Spline path;
    private final AnimationCurve curve;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public CameraAnimation(Spline path, AnimationCurve curve) {
        this.path = path;
        this.curve = curve;
    }

    public Vector3f getPosition() { return position; }
    public Quaternionf getOrientation() { return orientation; }

    public Matrix4f currentViewMatrix() {
        return new Matrix4f()
                .rotate(orientation)
                .translate(-position.x, -position.y, -position.z);
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

        if (path != null) {
            SplinePoint p = path.pointAt(eased);
            position.set(p.x(), p.y(), p.z());
        }

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
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
