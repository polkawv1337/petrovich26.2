package client_files.render.render.animation.value;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;
import client_files.render.render.animation.EasingType;
import client_files.render.render.animation.Interpolator;

public final class AnimatedFloat {
    private float value;
    private AnimationHandle currentHandle;
    private AnimationCurve defaultCurve = AnimationCurve.of(EasingType.EASE_OUT_CUBIC, 0.25f);

    public AnimatedFloat(float initialValue) {
        this.value = initialValue;
    }

    public float get() {
        return value;
    }

    public float getValue() {
        return value;
    }

    public void set(float value) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        this.value = value;
    }

    public void setValue(float value) {
        set(value);
    }

    public void setDurationSeconds(float duration) {
        this.defaultCurve = new AnimationCurve(defaultCurve.getInterpolator(), duration);
    }

    public void setEasing(Interpolator interpolator) {
        this.defaultCurve = new AnimationCurve(interpolator, defaultCurve.getDurationSeconds());
    }

    public void animateTo(float target) {
        animateTo(target, defaultCurve);
    }

    public void animateTo(float target, AnimationCurve curve) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        currentHandle = AnimationEngine.get().animate(() -> value, v -> value = v, value, target, curve);
    }
}
