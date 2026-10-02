package client_files.render.render.animation.threed;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.baseshape.threed.Shape3D;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TransformAnimation3D extends Animation3D {
    private final Vector3f startPos = new Vector3f();
    private final Vector3f targetPos;
    private final Quaternionf startRot = new Quaternionf();
    private final Quaternionf targetRot;
    private final AnimationCurve curve;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public TransformAnimation3D(Shape3D target, Vector3f targetPos, Quaternionf targetRot, AnimationCurve curve) {
        super(target);
        if (target != null) {
            this.startPos.set(target.getX(), target.getY(), target.getZ());
        }
        this.targetPos = targetPos != null ? new Vector3f(targetPos) : new Vector3f(startPos);
        this.targetRot = targetRot != null ? new Quaternionf(targetRot) : new Quaternionf();
        this.curve = curve;
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

        if (target != null) {
            target.setX(startPos.x + (targetPos.x - startPos.x) * eased);
            target.setY(startPos.y + (targetPos.y - startPos.y) * eased);
            target.setZ(startPos.z + (targetPos.z - startPos.z) * eased);
        }

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
            if (target != null) {
                target.setX(targetPos.x);
                target.setY(targetPos.y);
                target.setZ(targetPos.z);
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
