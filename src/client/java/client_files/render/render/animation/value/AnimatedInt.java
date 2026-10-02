package client_files.render.render.animation.value;

import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;

public final class AnimatedInt {
    private int value;
    private AnimationHandle currentHandle;

    public AnimatedInt(int initialValue) {
        this.value = initialValue;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        this.value = value;
    }

    public void animateTo(int target, AnimationCurve curve) {
        if (currentHandle != null && !currentHandle.isFinished()) {
            currentHandle.cancel();
        }
        currentHandle = AnimationEngine.get().animate(() -> (float) value, v -> value = Math.round(v), value, target, curve);
    }
}
