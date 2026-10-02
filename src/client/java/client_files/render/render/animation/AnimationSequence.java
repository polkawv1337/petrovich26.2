package client_files.render.render.animation;

import java.util.ArrayList;
import java.util.List;

public class AnimationSequence extends Animation {
    private final List<Animation> animations = new ArrayList<>();
    private int currentIndex = 0;
    private AnimationState state = AnimationState.PENDING;

    public AnimationSequence(List<Animation> animations) {
        if (animations != null) {
            this.animations.addAll(animations);
        }
    }

    public void add(Animation animation) {
        if (animation != null) {
            animations.add(animation);
        }
    }

    @Override
    public void tick(float deltaSeconds) {
        if (state == AnimationState.FINISHED || state == AnimationState.PAUSED) return;

        if (state == AnimationState.PENDING) {
            state = AnimationState.RUNNING;
            notifyStart();
        }

        while (currentIndex < animations.size()) {
            Animation current = animations.get(currentIndex);
            if (!current.isFinished()) {
                current.tick(deltaSeconds);
                if (!current.isFinished()) {
                    return;
                }
            }
            currentIndex++;
        }

        state = AnimationState.FINISHED;
        notifyFinish();
    }

    @Override
    public AnimationState state() {
        return state;
    }

    @Override
    public void pause() {
        state = AnimationState.PAUSED;
        if (currentIndex < animations.size()) {
            animations.get(currentIndex).pause();
        }
    }

    @Override
    public void resume() {
        state = AnimationState.RUNNING;
        if (currentIndex < animations.size()) {
            animations.get(currentIndex).resume();
        }
    }

    @Override
    public void cancel() {
        state = AnimationState.FINISHED;
        for (Animation anim : animations) {
            anim.cancel();
        }
        notifyFinish();
    }
}
