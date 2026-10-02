package client_files.render.render.animation;

public class PropertyAnimator extends Animation {
    private final AnimatableProperty property;
    private final float from;
    private final float to;
    private final AnimationCurve curve;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public PropertyAnimator(AnimatableProperty property, float from, float to, AnimationCurve curve) {
        this.property = property;
        this.from = from;
        this.to = to;
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
        float easedProgress = curve.getInterpolator().apply(rawProgress);

        float currentVal = from + (to - from) * easedProgress;
        if (property != null) {
            property.apply(currentVal);
        }

        notifyUpdate(rawProgress);

        if (elapsed >= duration) {
            state = AnimationState.FINISHED;
            if (property != null) {
                property.apply(to);
            }
            notifyFinish();
        }
    }

    @Override
    public AnimationState state() {
        return state;
    }

    @Override
    public void pause() {
        if (state == AnimationState.RUNNING) state = AnimationState.PAUSED;
    }

    @Override
    public void resume() {
        if (state == AnimationState.PAUSED) state = AnimationState.RUNNING;
    }

    @Override
    public void cancel() {
        state = AnimationState.FINISHED;
        notifyFinish();
    }
}
