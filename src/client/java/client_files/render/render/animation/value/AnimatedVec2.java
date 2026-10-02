package client_files.render.render.animation.value;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;
import client_files.render.render.animation.AnimationState;
import org.joml.Vector2f;

public final class AnimatedVec2 {
    private final Vector2f value = new Vector2f();
    private AnimationHandle currentHandle;

    public AnimatedVec2(float x, float y) {
        this.value.set(x, y);
    }

    public Vector2f get() {
        return new Vector2f(value);
    }

    public float getX() { return value.x; }
    public float getY() { return value.y; }

    public void set(float x, float y) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        this.value.set(x, y);
    }

    public void animateTo(float targetX, float targetY, AnimationCurve curve) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        final float startX = value.x;
        final float startY = value.y;

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

                value.x = startX + (targetX - startX) * easedProgress;
                value.y = startY + (targetY - startY) * easedProgress;

                notifyUpdate(rawProgress);

                if (elapsed >= duration) {
                    value.set(targetX, targetY);
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
