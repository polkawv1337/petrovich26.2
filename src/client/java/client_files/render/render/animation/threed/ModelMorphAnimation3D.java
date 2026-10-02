package client_files.render.render.animation.threed;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.baseshape.threed.Model;

import java.util.Objects;

public class ModelMorphAnimation3D extends Animation3D {
    private final float[] startVertices;
    private final float[] targetVertices;
    private final AnimationCurve curve;
    private final Model outputModel;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public ModelMorphAnimation3D(Model startModel, Model targetModel, Model outputModel, AnimationCurve curve) {
        super(outputModel);
        Objects.requireNonNull(startModel, "startModel cannot be null");
        Objects.requireNonNull(targetModel, "targetModel cannot be null");
        this.outputModel = Objects.requireNonNull(outputModel, "outputModel cannot be null");
        this.curve = Objects.requireNonNull(curve, "curve cannot be null");

        this.startVertices = startModel.localVertices();
        this.targetVertices = targetModel.localVertices();

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
        outputModel.setVertexPositions(current);

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
            outputModel.setVertexPositions(targetVertices.clone());
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
