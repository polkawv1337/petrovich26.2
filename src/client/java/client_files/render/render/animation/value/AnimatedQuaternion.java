package client_files.render.render.animation.value;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;
import client_files.render.render.animation.AnimationState;
import org.joml.Quaternionf;

public final class AnimatedQuaternion {
    private final Quaternionf value = new Quaternionf();
    private AnimationHandle currentHandle;

    public AnimatedQuaternion() {
        this.value.identity();
    }

    public AnimatedQuaternion(Quaternionf initial) {
        if (initial != null) {
            this.value.set(initial);
        }
    }

    public Quaternionf get() {
        return new Quaternionf(value);
    }

    public void set(Quaternionf q) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        if (q != null) {
            this.value.set(q);
        }
    }

    public void animateTo(Quaternionf target, AnimationCurve curve) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        final Quaternionf start = new Quaternionf(value);
        final Quaternionf end = new Quaternionf(target);

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

                start.slerp(end, easedProgress, value);
                notifyUpdate(rawProgress);

                if (elapsed >= duration) {
                    value.set(end);
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
