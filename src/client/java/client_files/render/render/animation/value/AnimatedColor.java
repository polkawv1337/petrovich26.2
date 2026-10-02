package client_files.render.render.animation.value;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.animation.EasingType;
import client_files.render.render.animation.Interpolator;
import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.ColorUtils;

public final class AnimatedColor {
    private ColorRGBA value;
    private AnimationHandle currentHandle;
    private AnimationCurve defaultCurve = AnimationCurve.of(EasingType.EASE_OUT_CUBIC, 0.25f);

    public AnimatedColor(ColorRGBA initialValue) {
        this.value = initialValue != null ? initialValue : ColorRGBA.WHITE;
    }

    public ColorRGBA get() {
        return value;
    }

    public ColorRGBA getValue() {
        return value;
    }

    public void set(ColorRGBA value) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        this.value = value != null ? value : ColorRGBA.WHITE;
    }

    public void setValue(ColorRGBA value) {
        set(value);
    }

    public void setDurationSeconds(float duration) {
        this.defaultCurve = new AnimationCurve(defaultCurve.getInterpolator(), duration);
    }

    public void setEasing(Interpolator interpolator) {
        this.defaultCurve = new AnimationCurve(interpolator, defaultCurve.getDurationSeconds());
    }

    public void animateTo(ColorRGBA target) {
        animateTo(target, defaultCurve);
    }

    public void animateTo(ColorRGBA target, AnimationCurve curve) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        final ColorRGBA fromColor = this.value;
        final ColorRGBA toColor = target != null ? target : ColorRGBA.WHITE;

        Animation anim = new Animation() {
            private float elapsed = 0f;
            private AnimationState state = AnimationState.PENDING;

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

                value = ColorUtils.lerp(fromColor, toColor, easedProgress);
                notifyUpdate(rawProgress);

                if (elapsed >= duration) {
                    value = toColor;
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
        };

        currentHandle = AnimationEngine.get().register(anim);
    }
}
