package client_files.render.render.animation;

import java.util.Objects;

public final class AnimationHandle {
    private final Animation animation;

    public AnimationHandle(Animation animation) {
        this.animation = Objects.requireNonNull(animation, "animation cannot be null");
    }

    public void cancel() {
        animation.cancel();
    }

    public void pause() {
        animation.pause();
    }

    public void resume() {
        animation.resume();
    }

    public boolean isFinished() {
        return animation.isFinished();
    }

    public AnimationState state() {
        return animation.state();
    }
}
