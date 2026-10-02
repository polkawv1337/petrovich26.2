package client_files.render.render.animation.twod;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.baseshape.twod.Rect;
import client_files.render.render.baseshape.twod.Shape2D;

public class TransformAnimation2D extends Animation2D {
    private final float startX, targetX;
    private final float startY, targetY;
    private final float startRot, targetRot;
    private final float startW, targetW;
    private final float startH, targetH;
    private final AnimationCurve curve;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public TransformAnimation2D(Shape2D target, float targetX, float targetY, float targetRot, AnimationCurve curve) {
        this(target, targetX, targetY, targetRot,
                target instanceof Rect r ? r.getWidth() : 0f,
                target instanceof Rect r ? r.getHeight() : 0f,
                curve);
    }

    public TransformAnimation2D(Shape2D target, float targetX, float targetY, float targetRot, float targetW, float targetH, AnimationCurve curve) {
        super(target);
        this.startX = target.getX();
        this.startY = target.getY();
        this.startRot = target.getRotationDegrees();
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetRot = targetRot;

        if (target instanceof Rect r) {
            this.startW = r.getWidth();
            this.startH = r.getHeight();
        } else {
            this.startW = 0f;
            this.startH = 0f;
        }
        this.targetW = targetW;
        this.targetH = targetH;
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

        target.setX(startX + (targetX - startX) * eased);
        target.setY(startY + (targetY - startY) * eased);
        target.setRotationDegrees(startRot + (targetRot - startRot) * eased);

        if (target instanceof Rect r && (startW != targetW || startH != targetH)) {
            r.setWidth(startW + (targetW - startW) * eased);
            r.setHeight(startH + (targetH - startH) * eased);
        }

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
            target.setX(targetX);
            target.setY(targetY);
            target.setRotationDegrees(targetRot);
            if (target instanceof Rect r) {
                r.setWidth(targetW);
                r.setHeight(targetH);
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
