package client_files.render.render.animation.value;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;
import client_files.render.render.animation.AnimationState;
import org.joml.Vector3f;

public final class AnimatedVec3 {
    private final Vector3f value = new Vector3f();
    private AnimationHandle currentHandle;

    public AnimatedVec3(float x, float y, float z) {
        this.value.set(x, y, z);
    }

    public Vector3f get() {
        return new Vector3f(value);
    }

    public float getX() { return value.x; }
    public float getY() { return value.y; }
    public float getZ() { return value.z; }

    public void set(float x, float y, float z) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        this.value.set(x, y, z);
    }

    public void animateTo(float targetX, float targetY, float targetZ, AnimationCurve curve) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        final float startX = value.x;
        final float startY = value.y;
        final float startZ = value.z;

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
                value.z = startZ + (targetZ - startZ) * easedProgress;

                notifyUpdate(rawProgress);

                if (elapsed >= duration) {
                    value.set(targetX, targetY, targetZ);
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
