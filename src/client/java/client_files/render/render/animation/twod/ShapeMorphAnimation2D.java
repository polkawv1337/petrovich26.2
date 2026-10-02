package client_files.render.render.animation.twod;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.baseshape.twod.Polygon;
import client_files.render.render.baseshape.twod.Shape2D;

import java.util.Objects;

public class ShapeMorphAnimation2D extends Animation2D {
    private final float[] startVertices;
    private final float[] targetVertices;
    private final AnimationCurve curve;
    private final Polygon outputPolygon;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public ShapeMorphAnimation2D(Shape2D startShape, Shape2D targetShape, Polygon outputPolygon, AnimationCurve curve) {
        super(outputPolygon);
        Objects.requireNonNull(startShape, "startShape cannot be null");
        Objects.requireNonNull(targetShape, "targetShape cannot be null");
        this.outputPolygon = Objects.requireNonNull(outputPolygon, "outputPolygon cannot be null");
        this.curve = Objects.requireNonNull(curve, "curve cannot be null");

        this.startVertices = startShape.localVertices();
        this.targetVertices = targetShape.localVertices();

        if (startVertices.length != targetVertices.length) {
            throw new IllegalArgumentException("Morphing requires equal vertex counts: " +
                    startVertices.length + " vs " + targetVertices.length);
        }
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

        float[] current = new float[startVertices.length];
        for (int i = 0; i < startVertices.length; i++) {
            current[i] = startVertices[i] + (targetVertices[i] - startVertices[i]) * eased;
        }
        outputPolygon.setPoints(current);

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
            outputPolygon.setPoints(targetVertices.clone());
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
